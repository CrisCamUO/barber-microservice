package com.sw3.barber_microservice.messaging;

import com.sw3.barber_microservice.config.RabbitMQConfig;
import com.sw3.barber_microservice.dto.BarberDTO;
import com.sw3.barber_microservice.dto.event.BarberEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BarberEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publica el evento de creación.
     * @param dto El barbero creado.
     * @param serviceIds Lista de IDs de servicios asociados (si los hay).
     */
    public void publishBarberCreated(BarberDTO dto, List<Long> serviceIds) {
        BarberEventDTO event = mapToEvent(dto, serviceIds);
        
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.BARBER_EXCHANGE, 
                "barber.created", 
                event
        );
        log.info("Evento enviado: barber.created -> ID: {}", dto.getId());
    }

    public void publishBarberUpdated(BarberDTO dto, List<Long> serviceIds) {
        BarberEventDTO event = mapToEvent(dto, serviceIds);
        
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.BARBER_EXCHANGE, 
                "barber.updated", 
                event
        );
        log.info("Evento enviado: barber.updated -> ID: {}", dto.getId());
    }

    public void publishBarberInactivated(String barberId) {
        // Evento mínimo para inactivar
        BarberEventDTO event = BarberEventDTO.builder()
                .id(barberId)
                .active(false) // Forzamos inactivo
                .serviceIds(Collections.emptyList())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.BARBER_EXCHANGE, 
                "barber.inactivated", 
                event
        );
        log.info("Evento enviado: barber.inactivated -> ID: {}", barberId);
    }

    // Método helper para convertir tu DTO local al DTO del evento
    private BarberEventDTO mapToEvent(BarberDTO dto, List<Long> serviceIds) {
        // Concatenamos Nombre y Apellido para el sistema externo
        String fullName = dto.getName();
        if (dto.getLastName() != null) {
            fullName += " " + dto.getLastName();
        }

        // Mapeamos 'contract' a 'active'. Si contract es null, asumimos false.
        boolean isActive = Boolean.TRUE.equals(dto.getContract());

        return BarberEventDTO.builder()
                .id(dto.getId())
                .name(fullName)
                .email(dto.getEmail())
                .active(isActive)
                .serviceIds(serviceIds != null ? serviceIds : Collections.emptyList())
                .build();
    }
}