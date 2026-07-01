package edu.rutmiit.demo.cinemaenrichmentclient.publisher;

import edu.rutmiit.demo.cinemaeventscontract.EventEnvelope;
import edu.rutmiit.demo.cinemaeventscontract.RoutingKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EnrichmentEventPublisher {

    private static final String SOURCE = "cinema-enrichment-client";

    private final RabbitTemplate rabbitTemplate;

    public void publish(String routingKey, Object payload) {
        EventEnvelope<Object> envelope = EventEnvelope.wrap(payload, SOURCE, routingKey);
        rabbitTemplate.convertAndSend(RoutingKeys.EXCHANGE, routingKey, envelope);
        log.info("enrichment event published: routingKey={} eventId={}", routingKey, envelope.metadata().eventId());
    }
}
