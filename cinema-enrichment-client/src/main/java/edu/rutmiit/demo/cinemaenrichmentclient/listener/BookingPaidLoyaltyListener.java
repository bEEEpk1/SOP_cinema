package edu.rutmiit.demo.cinemaenrichmentclient.listener;

import edu.rutmiit.demo.cinemaenrichmentclient.config.RabbitMqEnrichmentConfig;
import edu.rutmiit.demo.cinemaenrichmentclient.publisher.EnrichmentEventPublisher;
import edu.rutmiit.demo.cinemaeventscontract.BookingEvent;
import edu.rutmiit.demo.cinemaeventscontract.EventMetadata;
import edu.rutmiit.demo.cinemaeventscontract.LoyaltyEvent;
import edu.rutmiit.demo.cinemaeventscontract.RoutingKeys;
import edu.rutmiit.demo.cinemagrpc.loyalty.EarnPointsForBookingRequest;
import edu.rutmiit.demo.cinemagrpc.loyalty.EarnPointsForBookingResponse;
import edu.rutmiit.demo.cinemagrpc.loyalty.LoyaltyServiceGrpc;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;


@Slf4j
@Component
@RequiredArgsConstructor
public class BookingPaidLoyaltyListener {

    private final JsonMapper jsonMapper;
    private final LoyaltyServiceGrpc.LoyaltyServiceBlockingStub loyaltyStub;
    private final EnrichmentEventPublisher eventPublisher;

    @RabbitListener(queues = RabbitMqEnrichmentConfig.LOYALTY_EARNING_QUEUE)
    public void onBookingPaid(Message message) {
        try {
            JsonNode root = jsonMapper.readTree(message.getBody());
            EventMetadata metadata = jsonMapper.treeToValue(root.get("metadata"), EventMetadata.class);
            BookingEvent.Paid bookingPaid = jsonMapper.treeToValue(root.get("payload"), BookingEvent.Paid.class);

            log.info("received booking.paid for async loyalty earning: eventId={} bookingId={} customerId={} registered={}",
                    metadata.eventId(), bookingPaid.bookingId(), bookingPaid.customerId(), bookingPaid.customerRegistered());

            if (!Boolean.TRUE.equals(bookingPaid.customerRegistered())) {
                log.info("skip async loyalty earning for guest customer: bookingId={} customerId={}",
                        bookingPaid.bookingId(), bookingPaid.customerId());
                return;
            }

            EarnPointsForBookingRequest request = EarnPointsForBookingRequest.newBuilder()
                    .setCustomerId(bookingPaid.customerId())
                    .setBookingId(bookingPaid.bookingId())
                    .setFinalPrice(bookingPaid.finalPrice().toPlainString())
                    .setCurrency(bookingPaid.currency())
                    .build();

            EarnPointsForBookingResponse response = loyaltyStub.earnPointsForBooking(request);

            if (!response.getSuccess()) {
                throw new IllegalStateException("Could not earn loyalty points: " + response.getMessage());
            }

            if (response.getEarnedPoints() <= 0) {
                log.info("skip publishing loyalty.points.earned: bookingId={} customerId={} message={}",
                        bookingPaid.bookingId(), bookingPaid.customerId(), response.getMessage());
                return;
            }

            LoyaltyEvent.PointsEarned pointsEarned = new LoyaltyEvent.PointsEarned(
                    bookingPaid.customerId(),
                    bookingPaid.customerEmail(),
                    bookingPaid.bookingId(),
                    bookingPaid.finalPrice(),
                    bookingPaid.currency(),
                    response.getEarnedPoints(),
                    response.getCurrentPointsBalance(),
                    bookingPaid.movieTitle(),
                    bookingPaid.showStartTime(),
                    bookingPaid.hallName(),
                    bookingPaid.rowNumber(),
                    bookingPaid.seatNumber()
            );

            eventPublisher.publish(RoutingKeys.LOYALTY_POINTS_EARNED, pointsEarned);
        } catch (Exception e) {
            log.error("failed to process async loyalty earning event: {}", e.getMessage(), e);
            throw new RuntimeException("Could not process async loyalty earning event", e);
        }
    }
}
