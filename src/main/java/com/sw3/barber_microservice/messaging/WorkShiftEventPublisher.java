package com.sw3.barber_microservice.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.sw3.barber_microservice.config.RabbitMQConfig;
import com.sw3.barber_microservice.dto.WorkShiftDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkShiftEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishWorkShiftCreated(WorkShiftDTO workShiftDto) {
        //Convertir al dto del evento si es necesario
        
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.WORKSHIFT_EXCHANGE, 
                "workshift.created", 
                workShiftDto
        );
        log.info("Evento enviado: workshift.created -> WorkShift ID: {}, Barber ID: {}", workShiftDto.getId(), workShiftDto.getBarberId());
    }
    
}
