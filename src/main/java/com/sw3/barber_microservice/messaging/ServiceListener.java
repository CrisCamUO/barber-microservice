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
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

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
                // Estrategia idempotente: calcular diffs entre los barberos actuales y los entrantes
                Set<Long> incoming = event.getBarberIds().stream().filter(Objects::nonNull).collect(Collectors.toSet());

                // IDs actuales asociados al servicio
                Set<Long> current = serviceLocal.getBarbers().stream()
                        .map(Barber::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                Set<Long> toAdd = incoming.stream().filter(id -> !current.contains(id)).collect(Collectors.toSet());
                Set<Long> toRemove = current.stream().filter(id -> !incoming.contains(id)).collect(Collectors.toSet());

                // Añadir: traer barberos que faltan y vincular bidireccionalmente
                if (!toAdd.isEmpty()) {
                    List<Barber> barbersToAdd = barberRepository.findAllById(toAdd);
                    // advertencia si faltan IDs
                    if (barbersToAdd.size() != toAdd.size()) {
                        Set<Long> found = barbersToAdd.stream().map(Barber::getId).collect(Collectors.toSet());
                        Set<Long> missing = toAdd.stream().filter(id -> !found.contains(id)).collect(Collectors.toSet());
                        log.warn("Algunos barberos para agregar no existen localmente: {}", missing);
                    }
                    for (Barber b : barbersToAdd) {
                        // asegurar relación inversa
                        if (serviceLocal.getBarbers().stream().noneMatch(sb -> Objects.equals(sb.getId(), b.getId()))) {
                            serviceLocal.getBarbers().add(b);
                        }
                        if (b.getServices().stream().noneMatch(s -> Objects.equals(s.getId(), serviceLocal.getId()))) {
                            b.getServices().add(serviceLocal);
                        }
                    }
                    barberRepository.saveAll(barbersToAdd);
                }

                // Remover: quitar relación en barberos que ya no están
                if (!toRemove.isEmpty()) {
                    List<Barber> barbersToRemove = barberRepository.findAllById(toRemove);
                    for (Barber b : barbersToRemove) {
                        b.getServices().removeIf(s -> Objects.equals(s.getId(), serviceLocal.getId()));
                        serviceLocal.getBarbers().removeIf(bb -> Objects.equals(bb.getId(), b.getId()));
                    }
                    barberRepository.saveAll(barbersToRemove);
                }
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