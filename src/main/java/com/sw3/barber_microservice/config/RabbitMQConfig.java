package com.sw3.barber_microservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }

    public static final String SERVICE_CREATED_QUEUE = "service.created.queue";

    @Bean
    public Queue serviceCreatedQueue() {
        return new Queue(SERVICE_CREATED_QUEUE);
    }

    public static final String BARBER_CREATED_QUEUE = "barber.created.queue";

    @Bean
    public Queue barberCreatedQueue() {
        return new Queue(BARBER_CREATED_QUEUE);
    }

    public static final String WORKSHIFT_CREATED_QUEUE = "workshift.created.queue";

    @Bean
    public Queue workshiftCreatedQueue() {
        return new Queue(WORKSHIFT_CREATED_QUEUE);
    }

    public static final String SERVICE_EXCHANGE = "service.exchange";

    @Bean
    public Queue serviceExchange() {
        return new Queue(SERVICE_EXCHANGE);
    }

}
