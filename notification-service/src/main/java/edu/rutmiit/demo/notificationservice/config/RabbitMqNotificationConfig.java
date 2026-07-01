package edu.rutmiit.demo.notificationservice.config;

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
public class RabbitMqNotificationConfig {

    public static final String NOTIFICATIONS_QUEUE = "q.notifications.all";
    public static final String NOTIFICATIONS_DLQ = "q.notifications.all.dlq";
    public static final String NOTIFICATIONS_DLX = RoutingKeys.EXCHANGE + ".notifications.dlx";

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
    public DirectExchange notificationsDeadLetterExchange() {
        return ExchangeBuilder
                .directExchange(NOTIFICATIONS_DLX)
                .durable(true)
                .build();
    }

    @Bean
    public Queue notificationsQueue() {
        return QueueBuilder
                .durable(NOTIFICATIONS_QUEUE)
                .deadLetterExchange(NOTIFICATIONS_DLX)
                .deadLetterRoutingKey(NOTIFICATIONS_DLQ)
                .build();
    }

    @Bean
    public Queue notificationsDeadLetterQueue() {
        return QueueBuilder
                .durable(NOTIFICATIONS_DLQ)
                .build();
    }

    @Bean
    public Binding notificationsBinding(Queue notificationsQueue, TopicExchange eventsExchange) {
        return BindingBuilder
                .bind(notificationsQueue)
                .to(eventsExchange)
                .with(RoutingKeys.ALL_EVENTS);
    }

    @Bean
    public Binding notificationsDlqBinding(
            Queue notificationsDeadLetterQueue,
            DirectExchange notificationsDeadLetterExchange
    ) {
        return BindingBuilder
                .bind(notificationsDeadLetterQueue)
                .to(notificationsDeadLetterExchange)
                .with(NOTIFICATIONS_DLQ);
    }
}
