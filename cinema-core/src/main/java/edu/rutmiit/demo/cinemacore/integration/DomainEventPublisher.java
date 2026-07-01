package edu.rutmiit.demo.cinemacore.integration;

import edu.rutmiit.demo.cinemaeventscontract.EventEnvelope;
import edu.rutmiit.demo.cinemaeventscontract.RoutingKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private static final String SOURCE = "cinema-core";

    private final RabbitTemplate rabbitTemplate;

    public void publish(String routingKey, Object payload) {
        try {
            EventEnvelope<Object> envelope = EventEnvelope.wrap(payload, SOURCE, routingKey);
            rabbitTemplate.convertAndSend(RoutingKeys.EXCHANGE, routingKey, envelope);
            log.info("domain event published: routingKey={} eventId={}", routingKey, envelope.metadata().eventId());
        } catch (Exception e) {
            log.error("failed to publish domain event routingKey={}: {}", routingKey, e.getMessage(), e);
        }
    }
}
