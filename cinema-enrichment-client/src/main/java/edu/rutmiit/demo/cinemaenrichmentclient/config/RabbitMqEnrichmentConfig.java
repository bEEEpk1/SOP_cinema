package edu.rutmiit.demo.cinemaenrichmentclient.config;

import edu.rutmiit.demo.cinemaeventscontract.RoutingKeys;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;


@Configuration
public class RabbitMqEnrichmentConfig {

    public static final String TICKET_ENRICHMENT_QUEUE = "q.enrichment.ticket-created";
    public static final String TICKET_ENRICHMENT_DLQ = "q.enrichment.ticket-created.dlq";

    public static final String LOYALTY_EARNING_QUEUE = "q.enrichment.booking-paid";
    public static final String LOYALTY_EARNING_DLQ = "q.enrichment.booking-paid.dlq";

    public static final String ENRICHMENT_DLX = RoutingKeys.EXCHANGE + ".enrichment.dlx";

    @Bean
    public MessageConverter jsonMessageConverter(JsonMapper jsonMapper) {
        return new JacksonJsonMessageConverter(jsonMapper);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setConcurrentConsumers(1);
        factory.setMaxConcurrentConsumers(3);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }

    @Bean
    public TopicExchange eventsExchange() {
        return ExchangeBuilder
                .topicExchange(RoutingKeys.EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange enrichmentDeadLetterExchange() {
        return ExchangeBuilder
                .directExchange(ENRICHMENT_DLX)
                .durable(true)
                .build();
    }

    @Bean
    public Queue ticketEnrichmentQueue() {
        return QueueBuilder
                .durable(TICKET_ENRICHMENT_QUEUE)
                .deadLetterExchange(ENRICHMENT_DLX)
                .deadLetterRoutingKey(TICKET_ENRICHMENT_DLQ)
                .build();
    }

    @Bean
    public Queue ticketEnrichmentDeadLetterQueue() {
        return QueueBuilder.durable(TICKET_ENRICHMENT_DLQ).build();
    }

    @Bean
    public Queue loyaltyEarningQueue() {
        return QueueBuilder
                .durable(LOYALTY_EARNING_QUEUE)
                .deadLetterExchange(ENRICHMENT_DLX)
                .deadLetterRoutingKey(LOYALTY_EARNING_DLQ)
                .build();
    }

    @Bean
    public Queue loyaltyEarningDeadLetterQueue() {
        return QueueBuilder.durable(LOYALTY_EARNING_DLQ).build();
    }

    @Bean
    public Binding ticketEnrichmentBinding(Queue ticketEnrichmentQueue, TopicExchange eventsExchange) {
        return BindingBuilder
                .bind(ticketEnrichmentQueue)
                .to(eventsExchange)
                .with(RoutingKeys.TICKET_CREATED);
    }

    @Bean
    public Binding loyaltyEarningBinding(Queue loyaltyEarningQueue, TopicExchange eventsExchange) {
        return BindingBuilder
                .bind(loyaltyEarningQueue)
                .to(eventsExchange)
                .with(RoutingKeys.BOOKING_PAID);
    }

    @Bean
    public Binding ticketEnrichmentDlqBinding(
            Queue ticketEnrichmentDeadLetterQueue,
            DirectExchange enrichmentDeadLetterExchange
    ) {
        return BindingBuilder
                .bind(ticketEnrichmentDeadLetterQueue)
                .to(enrichmentDeadLetterExchange)
                .with(TICKET_ENRICHMENT_DLQ);
    }

    @Bean
    public Binding loyaltyEarningDlqBinding(
            Queue loyaltyEarningDeadLetterQueue,
            DirectExchange enrichmentDeadLetterExchange
    ) {
        return BindingBuilder
                .bind(loyaltyEarningDeadLetterQueue)
                .to(enrichmentDeadLetterExchange)
                .with(LOYALTY_EARNING_DLQ);
    }
}
