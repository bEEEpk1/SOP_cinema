package edu.rutmiit.demo.cinemaloyalty.service;

import edu.rutmiit.demo.cinemagrpc.loyalty.ApplyPointsOnPaymentRequest;
import edu.rutmiit.demo.cinemagrpc.loyalty.ApplyPointsOnPaymentResponse;
import edu.rutmiit.demo.cinemagrpc.loyalty.EarnPointsForBookingRequest;
import edu.rutmiit.demo.cinemagrpc.loyalty.EarnPointsForBookingResponse;
import edu.rutmiit.demo.cinemagrpc.loyalty.GetBalanceRequest;
import edu.rutmiit.demo.cinemagrpc.loyalty.GetBalanceResponse;
import edu.rutmiit.demo.cinemagrpc.loyalty.LoyaltyServiceGrpc;
import edu.rutmiit.demo.cinemagrpc.loyalty.NormalizeRequestedPointsRequest;
import edu.rutmiit.demo.cinemagrpc.loyalty.NormalizeRequestedPointsResponse;
import edu.rutmiit.demo.cinemagrpc.loyalty.RegisterCustomerRequest;
import edu.rutmiit.demo.cinemagrpc.loyalty.RegisterCustomerResponse;
import edu.rutmiit.demo.cinemagrpc.loyalty.RollbackBookingPointsRequest;
import edu.rutmiit.demo.cinemagrpc.loyalty.RollbackBookingPointsResponse;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
@Service
public class LoyaltyGrpcService extends LoyaltyServiceGrpc.LoyaltyServiceImplBase {

    private static final int INITIAL_REGISTERED_POINTS = 100;

    private final Map<Long, Integer> balances = new ConcurrentHashMap<>();
    private final Map<Long, BookingLoyaltyOperation> bookingOperations = new ConcurrentHashMap<>();

    @PostConstruct
    void preloadSeedRegisteredCustomers() {
        // Seed data in cinema-core creates registered users directly through SQL,
        // so they do not pass through CustomerService.registerCustomer().
        // These ids match the registered demo customers from data.sql.
        for (Long customerId : java.util.List.of(2L, 3L, 4L, 5L, 7L)) {
            balances.putIfAbsent(customerId, INITIAL_REGISTERED_POINTS);
        }
    }

    @Override
    public synchronized void registerCustomer(
            RegisterCustomerRequest request,
            StreamObserver<RegisterCustomerResponse> responseObserver
    ) {
        int initialPoints = Math.max(0, request.getInitialPoints());
        int current = balances.merge(request.getCustomerId(), initialPoints, Math::max);

        RegisterCustomerResponse response = RegisterCustomerResponse.newBuilder()
                .setSuccess(true)
                .setCurrentPointsBalance(current)
                .setMessage("Registered customer loyalty account created")
                .build();

        log.info("registered loyalty account: customerId={} balance={}", request.getCustomerId(), current);

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public synchronized void normalizeRequestedPoints(
            NormalizeRequestedPointsRequest request,
            StreamObserver<NormalizeRequestedPointsResponse> responseObserver
    ) {
        int available = balance(request.getCustomerId());
        int requested = Math.max(0, request.getRequestedPoints());
        int normalized = Math.min(requested, available);

        NormalizeRequestedPointsResponse response = NormalizeRequestedPointsResponse.newBuilder()
                .setNormalizedPoints(normalized)
                .setAvailablePoints(available)
                .build();

        log.info(
                "normalized loyalty points: customerId={} requested={} normalized={} available={}",
                request.getCustomerId(), requested, normalized, available
        );

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public synchronized void applyPointsOnPayment(
            ApplyPointsOnPaymentRequest request,
            StreamObserver<ApplyPointsOnPaymentResponse> responseObserver
    ) {
        int points = Math.max(0, request.getPointsUsed());
        int before = balance(request.getCustomerId());
        BookingLoyaltyOperation operation = operationFor(request.getBookingId(), request.getCustomerId());

        if (operation.rollbackApplied) {
            ApplyPointsOnPaymentResponse response = ApplyPointsOnPaymentResponse.newBuilder()
                    .setSuccess(false)
                    .setRemainingPoints(before)
                    .setMessage("Booking loyalty operation is already rolled back")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        // Idempotency: repeated payment event/call must not spend the same points again.
        if (operation.spentApplied) {
            ApplyPointsOnPaymentResponse response = ApplyPointsOnPaymentResponse.newBuilder()
                    .setSuccess(true)
                    .setRemainingPoints(before)
                    .setMessage("Points were already applied for this booking")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        if (points > before) {
            ApplyPointsOnPaymentResponse response = ApplyPointsOnPaymentResponse.newBuilder()
                    .setSuccess(false)
                    .setRemainingPoints(before)
                    .setMessage("Not enough loyalty points")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        int remaining = before - points;
        balances.put(request.getCustomerId(), remaining);
        operation.spentApplied = true;
        operation.spentPoints = points;

        ApplyPointsOnPaymentResponse response = ApplyPointsOnPaymentResponse.newBuilder()
                .setSuccess(true)
                .setRemainingPoints(remaining)
                .setMessage("Points applied")
                .build();

        log.info(
                "applied loyalty points: customerId={} bookingId={} used={} remaining={}",
                request.getCustomerId(), request.getBookingId(), points, remaining
        );

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public synchronized void getBalance(GetBalanceRequest request, StreamObserver<GetBalanceResponse> responseObserver) {
        GetBalanceResponse response = GetBalanceResponse.newBuilder()
                .setCustomerId(request.getCustomerId())
                .setPointsBalance(balance(request.getCustomerId()))
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public synchronized void earnPointsForBooking(
            EarnPointsForBookingRequest request,
            StreamObserver<EarnPointsForBookingResponse> responseObserver
    ) {
        BookingLoyaltyOperation operation = operationFor(request.getBookingId(), request.getCustomerId());
        int current = balance(request.getCustomerId());

        // Refund could be processed before async earning arrives. In that case
        // earning is intentionally ignored so refunded bookings cannot generate points.
        if (operation.rollbackApplied) {
            EarnPointsForBookingResponse response = EarnPointsForBookingResponse.newBuilder()
                    .setSuccess(true)
                    .setEarnedPoints(0)
                    .setCurrentPointsBalance(current)
                    .setMessage("Booking is already refunded; earning skipped")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info(
                    "skip earning for refunded booking: customerId={} bookingId={} balance={}",
                    request.getCustomerId(), request.getBookingId(), current
            );
            return;
        }

        // Idempotency: duplicate booking.paid delivery must not credit or publish twice.
        if (operation.earnedApplied) {
            EarnPointsForBookingResponse response = EarnPointsForBookingResponse.newBuilder()
                    .setSuccess(true)
                    .setEarnedPoints(0)
                    .setCurrentPointsBalance(current)
                    .setMessage("Points were already earned for this booking")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        BigDecimal finalPrice = parsePrice(request.getFinalPrice());
        int earnedPoints = calculateEarnedPoints(finalPrice);
        int currentBalance = current + earnedPoints;
        balances.put(request.getCustomerId(), currentBalance);
        operation.earnedApplied = true;
        operation.earnedPoints = earnedPoints;

        EarnPointsForBookingResponse response = EarnPointsForBookingResponse.newBuilder()
                .setSuccess(true)
                .setEarnedPoints(earnedPoints)
                .setCurrentPointsBalance(currentBalance)
                .setMessage("Points earned asynchronously after paid booking")
                .build();

        log.info(
                "earned loyalty points: customerId={} bookingId={} finalPrice={} {} earned={} balance={}",
                request.getCustomerId(),
                request.getBookingId(),
                request.getFinalPrice(),
                request.getCurrency(),
                earnedPoints,
                currentBalance
        );

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public synchronized void rollbackBookingPoints(
            RollbackBookingPointsRequest request,
            StreamObserver<RollbackBookingPointsResponse> responseObserver
    ) {
        BookingLoyaltyOperation operation = operationFor(request.getBookingId(), request.getCustomerId());
        int current = balance(request.getCustomerId());

        if (operation.rollbackApplied) {
            RollbackBookingPointsResponse response = RollbackBookingPointsResponse.newBuilder()
                    .setSuccess(true)
                    .setRestoredSpentPoints(0)
                    .setDeductedEarnedPoints(0)
                    .setCurrentPointsBalance(current)
                    .setMessage("Booking loyalty rollback was already applied")
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }

        int restoredSpentPoints = operation.spentApplied ? operation.spentPoints : 0;
        int deductedEarnedPoints = operation.earnedApplied ? operation.earnedPoints : 0;
        int afterRestore = current + restoredSpentPoints;
        int finalBalance = Math.max(0, afterRestore - deductedEarnedPoints);
        balances.put(request.getCustomerId(), finalBalance);
        operation.rollbackApplied = true;

        RollbackBookingPointsResponse response = RollbackBookingPointsResponse.newBuilder()
                .setSuccess(true)
                .setRestoredSpentPoints(restoredSpentPoints)
                .setDeductedEarnedPoints(deductedEarnedPoints)
                .setCurrentPointsBalance(finalBalance)
                .setMessage("Booking loyalty operations rolled back")
                .build();

        log.info(
                "rolled back loyalty points: customerId={} bookingId={} restored={} deducted={} balance={}",
                request.getCustomerId(), request.getBookingId(), restoredSpentPoints, deductedEarnedPoints, finalBalance
        );

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private BookingLoyaltyOperation operationFor(long bookingId, long customerId) {
        BookingLoyaltyOperation operation = bookingOperations.computeIfAbsent(bookingId, ignored -> new BookingLoyaltyOperation());
        operation.customerId = customerId;
        return operation;
    }

    private BigDecimal parsePrice(String value) {
        try {
            return new BigDecimal(value == null || value.isBlank() ? "0" : value);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private int calculateEarnedPoints(BigDecimal finalPrice) {
        int points = finalPrice
                .max(BigDecimal.ZERO)
                .divide(BigDecimal.valueOf(20), 0, RoundingMode.DOWN)
                .intValue();
        return finalPrice.signum() > 0 ? Math.max(points, 1) : 0;
    }

    private int balance(Long customerId) {
        return balances.getOrDefault(customerId, 0);
    }

    private static final class BookingLoyaltyOperation {
        private long customerId;
        private int spentPoints;
        private int earnedPoints;
        private boolean spentApplied;
        private boolean earnedApplied;
        private boolean rollbackApplied;
    }
}
