package edu.rutmiit.demo.cinemacore.integration;

import edu.rutmiit.demo.cinemagrpc.loyalty.ApplyPointsOnPaymentRequest;
import edu.rutmiit.demo.cinemagrpc.loyalty.ApplyPointsOnPaymentResponse;
import edu.rutmiit.demo.cinemagrpc.loyalty.GetBalanceRequest;
import edu.rutmiit.demo.cinemagrpc.loyalty.GetBalanceResponse;
import edu.rutmiit.demo.cinemagrpc.loyalty.LoyaltyServiceGrpc;
import edu.rutmiit.demo.cinemagrpc.loyalty.NormalizeRequestedPointsRequest;
import edu.rutmiit.demo.cinemagrpc.loyalty.NormalizeRequestedPointsResponse;
import edu.rutmiit.demo.cinemagrpc.loyalty.RegisterCustomerRequest;
import edu.rutmiit.demo.cinemagrpc.loyalty.RegisterCustomerResponse;
import edu.rutmiit.demo.cinemagrpc.loyalty.RollbackBookingPointsRequest;
import edu.rutmiit.demo.cinemagrpc.loyalty.RollbackBookingPointsResponse;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class LoyaltyClient {

    private static final int INITIAL_REGISTERED_POINTS = 100;

    private final LoyaltyServiceGrpc.LoyaltyServiceBlockingStub loyaltyStub;

    @Value("${cinema.grpc.fail-open:true}")
    private boolean failOpen;

    public void registerCustomer(Long customerId) {
        RegisterCustomerRequest request = RegisterCustomerRequest.newBuilder()
                .setCustomerId(customerId)
                .setInitialPoints(INITIAL_REGISTERED_POINTS)
                .build();

        try {
            RegisterCustomerResponse response = loyaltyStub.registerCustomer(request);
            if (!response.getSuccess()) {
                throw new IllegalStateException("Could not register loyalty account: " + response.getMessage());
            }
            log.info("loyalty gRPC registered customer: customerId={} initialBalance={}",
                    customerId, response.getCurrentPointsBalance());
        } catch (StatusRuntimeException e) {
            if (!failOpen) {
                throw e;
            }
            log.warn("loyalty gRPC unavailable, registered customer is saved without remote loyalty account: customerId={} reason={}",
                    customerId, e.getStatus());
        }
    }


    public int getBalance(Long customerId) {
        GetBalanceRequest request = GetBalanceRequest.newBuilder()
                .setCustomerId(customerId)
                .build();

        try {
            GetBalanceResponse response = loyaltyStub.getBalance(request);
            return response.getPointsBalance();
        } catch (StatusRuntimeException e) {
            if (!failOpen) {
                throw e;
            }
            log.warn("loyalty gRPC unavailable, balance fallback is 0: customerId={} reason={}", customerId, e.getStatus());
            return 0;
        }
    }

    public int normalizeRequestedPoints(Long customerId, Integer requestedPoints) {
        int requested = requestedPoints == null ? 0 : Math.max(requestedPoints, 0);
        if (requested == 0) {
            return 0;
        }

        NormalizeRequestedPointsRequest request = NormalizeRequestedPointsRequest.newBuilder()
                .setCustomerId(customerId)
                .setRequestedPoints(requested)
                .build();

        try {
            NormalizeRequestedPointsResponse response = loyaltyStub.normalizeRequestedPoints(request);
            log.info(
                    "loyalty gRPC normalized points: customerId={} requested={} normalized={} available={}",
                    customerId, requested, response.getNormalizedPoints(), response.getAvailablePoints()
            );
            return response.getNormalizedPoints();
        } catch (StatusRuntimeException e) {
            if (!failOpen) {
                throw e;
            }
            log.warn(
                    "loyalty gRPC unavailable, fallback disables points spending: customerId={} requested={} reason={}",
                    customerId, requested, e.getStatus()
            );
            return 0;
        }
    }

    public void applyPointsOnPayment(Long customerId, Long bookingId, int points) {
        int safePoints = Math.max(points, 0);
        if (safePoints == 0) {
            return;
        }

        ApplyPointsOnPaymentRequest request = ApplyPointsOnPaymentRequest.newBuilder()
                .setCustomerId(customerId)
                .setBookingId(bookingId)
                .setPointsUsed(safePoints)
                .build();

        try {
            ApplyPointsOnPaymentResponse response = loyaltyStub.applyPointsOnPayment(request);
            if (!response.getSuccess()) {
                throw new IllegalStateException("Could not apply loyalty points: " + response.getMessage());
            }
            log.info(
                    "loyalty gRPC applied points: customerId={} bookingId={} used={} remaining={}",
                    customerId, bookingId, safePoints, response.getRemainingPoints()
            );
        } catch (StatusRuntimeException e) {
            if (!failOpen) {
                throw e;
            }
            log.warn(
                    "loyalty gRPC unavailable, payment continues without remote loyalty transaction: customerId={} bookingId={} points={} reason={}",
                    customerId, bookingId, safePoints, e.getStatus()
            );
        }
    }

    public RollbackResult rollbackBookingPoints(Long customerId, Long bookingId) {
        RollbackBookingPointsRequest request = RollbackBookingPointsRequest.newBuilder()
                .setCustomerId(customerId)
                .setBookingId(bookingId)
                .build();

        try {
            RollbackBookingPointsResponse response = loyaltyStub.rollbackBookingPoints(request);
            if (!response.getSuccess()) {
                throw new IllegalStateException("Could not rollback loyalty points: " + response.getMessage());
            }

            RollbackResult result = new RollbackResult(
                    response.getRestoredSpentPoints(),
                    response.getDeductedEarnedPoints(),
                    response.getCurrentPointsBalance()
            );

            log.info(
                    "loyalty gRPC rolled back booking points: customerId={} bookingId={} restored={} deducted={} balance={}",
                    customerId,
                    bookingId,
                    result.restoredSpentPoints(),
                    result.deductedEarnedPoints(),
                    result.currentPointsBalance()
            );
            return result;
        } catch (StatusRuntimeException e) {
            if (!failOpen) {
                throw e;
            }
            log.warn(
                    "loyalty gRPC unavailable, refund continues without remote loyalty rollback: customerId={} bookingId={} reason={}",
                    customerId, bookingId, e.getStatus()
            );
            return null;
        }
    }

    public record RollbackResult(
            int restoredSpentPoints,
            int deductedEarnedPoints,
            int currentPointsBalance
    ) {}
}
