package com.sw3.barber_microservice.messaging;

import com.sw3.barber_microservice.config.RabbitMQConfig;
import com.sw3.barber_microservice.dto.BarberDTO;
import com.sw3.barber_microservice.dto.WorkShiftDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class BarberEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(BarberEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public BarberEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishBarberCreated(BarberDTO barberDto) {
        try {
            logger.info("Publishing barber created to queue {}: {}", RabbitMQConfig.BARBER_CREATED_QUEUE, barberDto);
            rabbitTemplate.convertAndSend(RabbitMQConfig.BARBER_CREATED_QUEUE, barberDto);
        } catch (Exception e) {
            logger.error("Failed to publish barber created event", e);
        }
    }

    public void publishWorkShiftCreated(WorkShiftDTO workShiftDto) {
        try {
            logger.info("Publishing workshift created to queue {}: {}", RabbitMQConfig.WORKSHIFT_CREATED_QUEUE, workShiftDto);
            rabbitTemplate.convertAndSend(RabbitMQConfig.WORKSHIFT_CREATED_QUEUE, workShiftDto);
        } catch (Exception e) {
            logger.error("Failed to publish workshift created event", e);
        }
    }

}
