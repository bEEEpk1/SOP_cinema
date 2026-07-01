package edu.rutmiit.demo.cinemacore.integration;

import edu.rutmiit.demo.cinemacore.entity.CustomerEntity;
import edu.rutmiit.demo.cinemacore.entity.HallEntity;
import edu.rutmiit.demo.cinemacore.entity.SeatEntity;
import edu.rutmiit.demo.cinemacore.entity.ShowEntity;
import edu.rutmiit.demo.cinemagrpc.pricing.CalculateFinalPriceRequest;
import edu.rutmiit.demo.cinemagrpc.pricing.CalculateFinalPriceResponse;
import edu.rutmiit.demo.cinemagrpc.pricing.PricingServiceGrpc;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


@Slf4j
@Component
@RequiredArgsConstructor
public class PricingClient {

    private final PricingServiceGrpc.PricingServiceBlockingStub pricingStub;

    @Value("${cinema.grpc.fail-open:true}")
    private boolean failOpen;

    public BigDecimal calculateFinalPrice(ShowEntity show, SeatEntity seat, HallEntity hall, CustomerEntity customer, long occupiedSeats) {
        CalculateFinalPriceRequest request = CalculateFinalPriceRequest.newBuilder()
                .setShowId(show.getId())
                .setSeatId(seat.getId())
                .setCustomerId(customer.getId())
                .setBasePrice(show.getBasePrice().toPlainString())
                .setCurrency(show.getCurrency())
                .setHallType(hall.getHallType().name())
                .setSeatType(seat.getSeatType().name())
                .setStartTime(show.getStartTime().toString())
                .setTotalSeats(hall.getCapacity() == null ? 0 : hall.getCapacity())
                .setOccupiedSeats(Math.toIntExact(occupiedSeats))
                .build();

        try {
            CalculateFinalPriceResponse response = pricingStub.calculateFinalPrice(request);
            BigDecimal finalPrice = new BigDecimal(response.getFinalPrice());
            log.info(
                    "pricing gRPC response: showId={} seatId={} hallType={} seatType={} occupiedSeats={} finalPrice={} {} explanation={}",
                    show.getId(), seat.getId(), hall.getHallType(), seat.getSeatType(), occupiedSeats, finalPrice, response.getCurrency(), response.getExplanation()
            );
            return finalPrice;
        } catch (StatusRuntimeException e) {
            if (!failOpen) {
                throw e;
            }
            log.warn(
                    "pricing gRPC unavailable, falling back to base price: showId={} seatId={} hallType={} customerId={} reason={}",
                    show.getId(), seat.getId(), hall.getHallType(), customer.getId(), e.getStatus()
            );
            return show.getBasePrice();
        }
    }
}
