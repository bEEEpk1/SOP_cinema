package edu.rutmiit.demo.cinemapricing.service;

import edu.rutmiit.demo.cinemagrpc.pricing.CalculateFinalPriceRequest;
import edu.rutmiit.demo.cinemagrpc.pricing.CalculateFinalPriceResponse;
import edu.rutmiit.demo.cinemagrpc.pricing.PricingServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Formula:
 * finalPrice = (basePrice + seatTypeSurcharge)
 *              * hallTypeMultiplier
 *              * timeOfDayMultiplier
 *              * dayOfWeekMultiplier
 *              * occupancyMultiplier
 */
@Slf4j
@Service
public class PricingGrpcService extends PricingServiceGrpc.PricingServiceImplBase {

    @Override
    public void calculateFinalPrice(
            CalculateFinalPriceRequest request,
            StreamObserver<CalculateFinalPriceResponse> responseObserver
    ) {
        try {
            BigDecimal basePrice = new BigDecimal(request.getBasePrice());
            BigDecimal seatTypeSurcharge = seatTypeSurcharge(request.getSeatType());
            BigDecimal hallTypeMultiplier = hallTypeMultiplier(request.getHallType());
            BigDecimal timeOfDayMultiplier = timeOfDayMultiplier(request.getStartTime());
            BigDecimal dayOfWeekMultiplier = dayOfWeekMultiplier(request.getStartTime());
            double occupancyRate = occupancyRate(request.getOccupiedSeats(), request.getTotalSeats());
            BigDecimal occupancyMultiplier = occupancyMultiplier(occupancyRate);

            BigDecimal rawFinalPrice = basePrice
                    .add(seatTypeSurcharge)
                    .multiply(hallTypeMultiplier)
                    .multiply(timeOfDayMultiplier)
                    .multiply(dayOfWeekMultiplier)
                    .multiply(occupancyMultiplier);

            BigDecimal finalPrice = roundToNearestTen(rawFinalPrice);

            List<String> factors = new ArrayList<>();
            factors.add("base=" + money(basePrice));
            factors.add("seatSurcharge=" + money(seatTypeSurcharge) + " for " + safe(request.getSeatType()));
            factors.add("hallType=" + safe(request.getHallType()) + " x" + hallTypeMultiplier);
            factors.add("timeOfDay x" + timeOfDayMultiplier);
            factors.add("dayOfWeek x" + dayOfWeekMultiplier);
            factors.add("occupancy=" + request.getOccupiedSeats() + "/" + request.getTotalSeats()
                    + " (" + Math.round(occupancyRate * 100) + "%) x" + occupancyMultiplier);
            factors.add("rounded=" + money(finalPrice));

            CalculateFinalPriceResponse response = CalculateFinalPriceResponse.newBuilder()
                    .setFinalPrice(finalPrice.toPlainString())
                    .setCurrency(request.getCurrency())
                    .setExplanation(String.join("; ", factors))
                    .setBasePrice(basePrice.toPlainString())
                    .setSeatTypeSurcharge(seatTypeSurcharge.toPlainString())
                    .setHallTypeMultiplier(hallTypeMultiplier.toPlainString())
                    .setTimeOfDayMultiplier(timeOfDayMultiplier.toPlainString())
                    .setDayOfWeekMultiplier(dayOfWeekMultiplier.toPlainString())
                    .setOccupancyMultiplier(occupancyMultiplier.toPlainString())
                    .setOccupancyRate(occupancyRate)
                    .build();

            log.info(
                    "calculated dynamic price: showId={} seatId={} customerId={} finalPrice={} {} factors={}",
                    request.getShowId(),
                    request.getSeatId(),
                    request.getCustomerId(),
                    response.getFinalPrice(),
                    response.getCurrency(),
                    response.getExplanation()
            );

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    private BigDecimal roundToNearestTen(BigDecimal value) {
        return value
                .divide(BigDecimal.TEN, 0, RoundingMode.HALF_UP)
                .multiply(BigDecimal.TEN)
                .setScale(0, RoundingMode.UNNECESSARY);
    }

    private BigDecimal hallTypeMultiplier(String hallType) {
        return switch (normalize(hallType)) {
            case "VIP" -> new BigDecimal("1.30");
            case "IMAX" -> new BigDecimal("1.50");
            case "DOLBY_ATMOS", "DOLBY" -> new BigDecimal("1.40");
            default -> BigDecimal.ONE;
        };
    }

    private BigDecimal seatTypeSurcharge(String seatType) {
        return switch (normalize(seatType)) {
            case "VIP" -> new BigDecimal("250");
            case "SOFA" -> new BigDecimal("350");
            default -> BigDecimal.ZERO;
        };
    }

    private BigDecimal timeOfDayMultiplier(String startTime) {
        OffsetDateTime dateTime = parseStartTime(startTime);
        if (dateTime == null) {
            return BigDecimal.ONE;
        }

        int hour = dateTime.getHour();
        if (hour < 12) {
            return new BigDecimal("0.85");
        }
        if (hour < 18) {
            return BigDecimal.ONE;
        }
        if (hour < 23) {
            return new BigDecimal("1.20");
        }
        return new BigDecimal("0.95");
    }

    private BigDecimal dayOfWeekMultiplier(String startTime) {
        OffsetDateTime dateTime = parseStartTime(startTime);
        if (dateTime == null) {
            return BigDecimal.ONE;
        }

        DayOfWeek day = dateTime.getDayOfWeek();
        if (day == DayOfWeek.FRIDAY) {
            return new BigDecimal("1.10");
        }
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            return new BigDecimal("1.20");
        }
        return BigDecimal.ONE;
    }

    private BigDecimal occupancyMultiplier(double occupancyRate) {
        if (occupancyRate >= 0.80) {
            return new BigDecimal("1.25");
        }
        if (occupancyRate >= 0.60) {
            return new BigDecimal("1.15");
        }
        if (occupancyRate >= 0.30) {
            return new BigDecimal("1.05");
        }
        return BigDecimal.ONE;
    }

    private double occupancyRate(int occupiedSeats, int totalSeats) {
        if (totalSeats <= 0) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(1.0, (double) occupiedSeats / (double) totalSeats));
    }

    private OffsetDateTime parseStartTime(String startTime) {
        if (startTime == null || startTime.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(startTime);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "UNKNOWN" : value;
    }

    private String money(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }
}
