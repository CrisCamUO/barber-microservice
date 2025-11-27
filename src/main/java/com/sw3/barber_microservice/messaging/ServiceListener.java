package com.sw3.barber_microservice.messaging;

import com.sw3.barber_microservice.config.RabbitMQConfig;
import com.sw3.barber_microservice.dto.ServiceDTO;
import com.sw3.barber_microservice.service.ServiceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceListener {

    private static final Logger logger = LoggerFactory.getLogger(ServiceListener.class);

    @Autowired
    private ServiceService serviceService;

    @RabbitListener(queues = RabbitMQConfig.SERVICE_CREATED_QUEUE)
    public void receive(ServiceDTO serviceDto) {
        logger.info("Received message on queue {}: {}", RabbitMQConfig.SERVICE_CREATED_QUEUE, serviceDto);
        try {
            serviceService.save(serviceDto);
        } catch (Exception e) {
            logger.error("Error processing received service message", e);
        }
    }

    @RabbitListener(queues = RabbitMQConfig.SERVICE_EXCHANGE)
    public void receiveFromExchange(ServiceDTO serviceDto) {
        logger.info("Received message on exchange {}: {}", RabbitMQConfig.SERVICE_EXCHANGE, serviceDto);
        try {
            serviceService.save(serviceDto);
        } catch (Exception e) {
            logger.error("Error processing received service message from exchange", e);
        }
    }


}
