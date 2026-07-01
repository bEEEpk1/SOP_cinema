package edu.rutmiit.demo.auditservice.config;

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
public class RabbitMqAuditConfig {

    public static final String AUDIT_QUEUE = "q.audit.events";
    public static final String AUDIT_DLQ = "q.audit.events.dlq";
    public static final String AUDIT_DLX = RoutingKeys.EXCHANGE + ".audit.dlx";

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
    public DirectExchange auditDeadLetterExchange() {
        return ExchangeBuilder
                .directExchange(AUDIT_DLX)
                .durable(true)
                .build();
    }

    @Bean
    public Queue auditQueue() {
        return QueueBuilder
                .durable(AUDIT_QUEUE)
                .deadLetterExchange(AUDIT_DLX)
                .deadLetterRoutingKey(AUDIT_DLQ)
                .build();
    }

    @Bean
    public Queue auditDeadLetterQueue() {
        return QueueBuilder
                .durable(AUDIT_DLQ)
                .build();
    }

    @Bean
    public Binding auditBinding(Queue auditQueue, TopicExchange eventsExchange) {
        return BindingBuilder
                .bind(auditQueue)
                .to(eventsExchange)
                .with(RoutingKeys.ALL_EVENTS);
    }

    @Bean
    public Binding auditDlqBinding(Queue auditDeadLetterQueue, DirectExchange auditDeadLetterExchange) {
        return BindingBuilder
                .bind(auditDeadLetterQueue)
                .to(auditDeadLetterExchange)
                .with(AUDIT_DLQ);
    }
}
