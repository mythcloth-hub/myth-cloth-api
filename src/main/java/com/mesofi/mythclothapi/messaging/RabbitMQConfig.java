package com.mesofi.mythclothapi.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static class QueueNames {
        public static final String CRAWLER_QUEUE = "crawler.queue";

        private QueueNames() {
        }
    }

    public static class RoutingKeys {
        public static final String CRAWLER_ROUTING_KEY = "crawler.job";

        private RoutingKeys() {
        }
    }

    public static class ExchangeNames {
        public static final String CRAWLER_EXCHANGE = "crawler.exchange";

        private ExchangeNames() {
        }
    }

    // --- Queues ---
    @Bean
    public Queue crawlerQueue() {
        return QueueBuilder.durable(QueueNames.CRAWLER_QUEUE).build();
    }

    // --- Exchanges ---
    @Bean
    public DirectExchange crawlerExchange() {
        return new DirectExchange(ExchangeNames.CRAWLER_EXCHANGE, true, false);
    }

    // --- Bindings ---
    @Bean
    public Binding crawlerBinding(Queue crawlerQueue, DirectExchange crawlerExchange) {
        return BindingBuilder.bind(crawlerQueue).to(crawlerExchange).with(RoutingKeys.CRAWLER_ROUTING_KEY);
    }

    // --- Message Converter ---
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
