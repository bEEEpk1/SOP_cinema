package edu.rutmiit.demo.cinemapricing.service;

import edu.rutmiit.demo.cinemagrpc.pricing.CalculateFinalPriceRequest;
import edu.rutmiit.demo.cinemagrpc.pricing.CalculateFinalPriceResponse;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PricingGrpcServiceTest {

    private final PricingGrpcService pricingGrpcService = new PricingGrpcService();

    @Test
    void calculateFinalPriceShouldApplyVipSeatAndVipHallMultiplier() {
        // arrange — подготавливаем входные данные
        CalculateFinalPriceRequest request = CalculateFinalPriceRequest.newBuilder()
                .setShowId(1L)
                .setSeatId(10L)
                .setCustomerId(100L)
                .setBasePrice("1000")
                .setCurrency("RUB")
                .setHallType("VIP")
                .setSeatType("VIP")
                .setStartTime("2026-07-06T14:00:00+03:00")
                .setTotalSeats(100)
                .setOccupiedSeats(10)
                .build();

        TestResponseObserver observer = new TestResponseObserver();

        // act — вызываем тестируемый метод
        pricingGrpcService.calculateFinalPrice(request, observer);

        // assert — проверяем результат
        assertNull(observer.error);
        assertTrue(observer.completed);
        assertNotNull(observer.response);

        assertEquals("1630", observer.response.getFinalPrice());
        assertEquals("RUB", observer.response.getCurrency());

        assertEquals("1000", observer.response.getBasePrice());
        assertEquals("250", observer.response.getSeatTypeSurcharge());
        assertEquals("1.30", observer.response.getHallTypeMultiplier());
        assertEquals("1", observer.response.getTimeOfDayMultiplier());
        assertEquals("1", observer.response.getDayOfWeekMultiplier());
        assertEquals("1", observer.response.getOccupancyMultiplier());
        assertEquals(0.10, observer.response.getOccupancyRate(), 0.0001);
    }

    @Test
    void calculateFinalPriceShouldApplyWeekendEveningAndHighOccupancyMultipliers() {
        // arrange
        CalculateFinalPriceRequest request = CalculateFinalPriceRequest.newBuilder()
                .setShowId(2L)
                .setSeatId(20L)
                .setCustomerId(200L)
                .setBasePrice("1000")
                .setCurrency("RUB")
                .setHallType("IMAX")
                .setSeatType("STANDARD")
                .setStartTime("2026-07-04T20:00:00+03:00")
                .setTotalSeats(100)
                .setOccupiedSeats(80)
                .build();

        TestResponseObserver observer = new TestResponseObserver();

        // act
        pricingGrpcService.calculateFinalPrice(request, observer);

        // assert
        assertNull(observer.error);
        assertTrue(observer.completed);
        assertNotNull(observer.response);

        assertEquals("2700", observer.response.getFinalPrice());
        assertEquals("RUB", observer.response.getCurrency());

        assertEquals("1000", observer.response.getBasePrice());
        assertEquals("0", observer.response.getSeatTypeSurcharge());
        assertEquals("1.50", observer.response.getHallTypeMultiplier());
        assertEquals("1.20", observer.response.getTimeOfDayMultiplier());
        assertEquals("1.20", observer.response.getDayOfWeekMultiplier());
        assertEquals("1.25", observer.response.getOccupancyMultiplier());
        assertEquals(0.80, observer.response.getOccupancyRate(), 0.0001);
    }

    private static final class TestResponseObserver implements StreamObserver<CalculateFinalPriceResponse> {

        private CalculateFinalPriceResponse response;
        private Throwable error;
        private boolean completed;

        @Override
        public void onNext(CalculateFinalPriceResponse response) {
            this.response = response;
        }

        @Override
        public void onError(Throwable error) {
            this.error = error;
        }

        @Override
        public void onCompleted() {
            this.completed = true;
        }
    }
}

