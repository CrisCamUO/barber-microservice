package com.sw3.barber_microservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // -------------------------------------------------------------------
    // CONSTANTES
    // -------------------------------------------------------------------
    
    // 1. LO QUE PUBLICAMOS (Nuestro Exchange propio)
    public static final String BARBER_EXCHANGE = "barber.exchange";

    // 2. LO QUE ESCUCHAMOS (Exchange del otro microservicio)
    public static final String SERVICE_EXCHANGE = "service.exchange"; // Debe coincidir EXACTAMENTE con el otro MS

    // 3. NUESTRA COLA (El buzón donde llegarán los avisos de servicios)
    public static final String SERVICE_LISTENER_QUEUE = "barber.service.listener.queue";

    // -------------------------------------------------------------------
    // CONVERTIDOR JSON (Obligatorio)
    // -------------------------------------------------------------------
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    // -------------------------------------------------------------------
    // CONFIGURACIÓN PRODUCER (Para publicar Barberos)
    // -------------------------------------------------------------------
    @Bean
    public TopicExchange barberExchange() {
        return new TopicExchange(BARBER_EXCHANGE);
    }

    // -------------------------------------------------------------------
    // CONFIGURACIÓN CONSUMER (Para escuchar Servicios)
    // -------------------------------------------------------------------
    @Bean
    public Queue serviceListenerQueue() {
        return new Queue(SERVICE_LISTENER_QUEUE, true);
    }

    @Bean
    public TopicExchange serviceExchange() {
        return new TopicExchange(SERVICE_EXCHANGE);
    }

    @Bean
    public Binding bindingServiceEvents(Queue serviceListenerQueue, TopicExchange serviceExchange) {
        // Escuchamos todo lo que venga de servicios (created, updated, inactivated)
        return BindingBuilder.bind(serviceListenerQueue).to(serviceExchange).with("service.#");
    }
}