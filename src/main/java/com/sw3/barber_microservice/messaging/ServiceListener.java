package com.sw3.barber_microservice.messaging;

import com.sw3.barber_microservice.config.RabbitMQConfig;
import com.sw3.barber_microservice.dto.event.ServiceEventDTO;
import com.sw3.barber_microservice.model.Barber;
import com.sw3.barber_microservice.model.Service;
import com.sw3.barber_microservice.repository.BarberRepository;
import com.sw3.barber_microservice.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceListener {

    private final ServiceRepository serviceRepository;
    private final BarberRepository barberRepository;

    @RabbitListener(queues = RabbitMQConfig.SERVICE_LISTENER_QUEUE)
    @Transactional // Vital para manejar la lista de relaciones (Lazy Loading / Cascade)
    public void handleServiceEvent(ServiceEventDTO event) {
        log.info("📩 Recibido evento de Servicio: ID={}, Status={}", event.getId(), event.getSystemStatus());

        try {
            // 1. Buscar o Crear el Servicio Local (Réplica)
            Service serviceLocal = serviceRepository.findById(event.getId()).orElse(new Service());
            
            // Asignación Manual del ID (Debe coincidir con el otro MS)
            serviceLocal.setId(event.getId());
            serviceLocal.setName(event.getName());

            // Mapeo de estado: "Activo" -> true, cualquier otra cosa -> false
            boolean isActive = "Activo".equalsIgnoreCase(event.getSystemStatus());
            serviceLocal.setActive(isActive);

            // 2. SINCRONIZACIÓN DE RELACIONES (BarberServices)
            // Si el evento trae la lista de barberos autorizados, actualizamos nuestra tabla intermedia
            if (event.getBarberIds() != null) {
                // Estrategia: Limpiar y re-asignar. 
                // Como 'cascade = CascadeType.ALL' y 'orphanRemoval = true' (asumido/recomendado), 
                // limpiar la lista borra las filas en barber_services.
                // switch to ManyToMany: clear set of barbers and add found barbers
                serviceLocal.getBarbers().clear();

                List<Barber> barbers = barberRepository.findAllById(event.getBarberIds());
                serviceLocal.getBarbers().addAll(barbers);
            }

            // 3. Guardar (Upsert)
            serviceRepository.save(serviceLocal);
            log.info("✅ Servicio sincronizado localmente con {} barberos asociados.", 
                    event.getBarberIds() != null ? event.getBarberIds().size() : 0);

        } catch (Exception e) {
            log.error("❌ Error al procesar evento de servicio: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}