package com.sw3.barber_microservice.messaging;

import com.sw3.barber_microservice.config.RabbitMQConfig;
import com.sw3.barber_microservice.dto.event.AssignedBarbersServiceEventDTO;
import com.sw3.barber_microservice.model.Barber;
import com.sw3.barber_microservice.model.Service;
import com.sw3.barber_microservice.repository.BarberRepository;
import com.sw3.barber_microservice.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceAssignedListener {

    private final ServiceRepository serviceRepository;
    private final BarberRepository barberRepository;

    @RabbitListener(queues = RabbitMQConfig.SERVICE_ASSIGNED_LISTENER_QUEUE)
    @Transactional
    public void handleAssignedEvent(AssignedBarbersServiceEventDTO event) {
        log.info("📩 Recibido evento de servicios asignados: serviceId={}, barberCount={}",
                event.getServiceId(), event.getBarberIds() != null ? event.getBarberIds().size() : 0);

        try {
            Long serviceId = event.getServiceId();
            List<Long> incomingBarberIds = event.getBarberIds() != null ? event.getBarberIds() : Collections.emptyList();

            // 1) Obtener o crear servicio local (réplica)
            Service serviceLocal = serviceRepository.findById(serviceId).orElseGet(() -> {
                Service s = new Service();
                s.setId(serviceId);
                s.setActive(true);
                return s;
            });

            // 2) Determinar barberos actuales asociados al servicio
            Set<Long> currentBarberIds = serviceLocal.getBarbers().stream()
                    .map(Barber::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            Set<Long> incomingSet = new HashSet<>(incomingBarberIds);

            // 3) Barberos a agregar y a remover
            Set<Long> toAdd = incomingSet.stream()
                    .filter(id -> !currentBarberIds.contains(id))
                    .collect(Collectors.toSet());

            Set<Long> toRemove = currentBarberIds.stream()
                    .filter(id -> !incomingSet.contains(id))
                    .collect(Collectors.toSet());

            List<Barber> changedBarbers = new ArrayList<>();

            // 4) Añadir servicio a barberos nuevos
            if (!toAdd.isEmpty()) {
                List<Barber> barbersToAdd = barberRepository.findAllById(new ArrayList<>(toAdd));
                if (barbersToAdd.size() != toAdd.size()) {
                    // Algunos IDs no existen localmente; registrar advertencia
                    Set<Long> found = barbersToAdd.stream().map(Barber::getId).collect(Collectors.toSet());
                    Set<Long> missing = toAdd.stream().filter(id -> !found.contains(id)).collect(Collectors.toSet());
                    log.warn("Algunos barberos a asignar no existen localmente: {}", missing);
                }

                for (Barber b : barbersToAdd) {
                    // asegurar relación bidireccional
                    if (!b.getServices().stream().anyMatch(s -> Objects.equals(s.getId(), serviceId))) {
                        // crear referencia mínima de Service para añadir
                        Service svcRef = serviceLocal;
                        b.getServices().add(svcRef);
                    }
                    serviceLocal.getBarbers().add(b);
                    changedBarbers.add(b);
                }
            }

            // 5) Remover servicio de barberos que ya no están en la lista
            if (!toRemove.isEmpty()) {
                List<Barber> barbersToRemove = barberRepository.findAllById(new ArrayList<>(toRemove));
                for (Barber b : barbersToRemove) {
                    b.getServices().removeIf(s -> Objects.equals(s.getId(), serviceId));
                    serviceLocal.getBarbers().removeIf(bar -> Objects.equals(bar.getId(), b.getId()));
                    changedBarbers.add(b);
                }
            }

            // 6) Persistir cambios
            if (!changedBarbers.isEmpty()) {
                barberRepository.saveAll(changedBarbers);
            }
            serviceRepository.save(serviceLocal);

            log.info("✅ Sincronización de servicios para barberos procesada: added={}, removed={}", toAdd.size(), toRemove.size());

        } catch (Exception ex) {
            log.error("❌ Error procesando AssignedBarbersServiceEvent: {}", ex.getMessage(), ex);
            throw ex;
        }
    }
}
