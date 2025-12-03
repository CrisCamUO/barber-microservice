package com.sw3.barber_microservice.config;

import com.sw3.barber_microservice.model.Barber;
import com.sw3.barber_microservice.model.WorkShift;
import com.sw3.barber_microservice.model.DayOfWeekEnum;
import com.sw3.barber_microservice.repository.BarberRepository;
import com.sw3.barber_microservice.repository.ServiceRepository;
import com.sw3.barber_microservice.repository.WorkShiftRepository;
import com.sw3.barber_microservice.service.KeycloakService;
import com.sw3.barber_microservice.messaging.BarberEventPublisher;
import com.sw3.barber_microservice.messaging.WorkShiftEventPublisher;
import com.sw3.barber_microservice.dto.BarberDTO;
import com.sw3.barber_microservice.dto.WorkShiftDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.Collections;

/**
 * DataLoader para inicializar la base de datos con barberos y sus horarios de prueba.
 */
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final BarberRepository barberRepository;
    private final ServiceRepository serviceRepository;
    private final WorkShiftRepository workShiftRepository;
    private final KeycloakService keycloakService;
    private final BarberEventPublisher barberEventPublisher;
    private final WorkShiftEventPublisher workShiftEventPublisher;

    @Override
    public void run(String... args) throws Exception {
        // Solo cargar si la base de datos está vacía
        if (barberRepository.count() > 0) {
            System.out.println("✅ La base de datos ya tiene datos. Omitiendo DataLoader de barberos.");
            return;
        }

        System.out.println("🔄 Inicializando base de datos con barberos de prueba...");

        // ============================================================
        // 1. CREAR BARBEROS CON KEYCLOAK
        // ============================================================

        // Barbero 1: Juan Pérez
        Barber barber1 = createBarber(
            "Juan",
            "Pérez",
            "juan.perez@barbershop.com",
            "3001234567",
            "Barbero profesional con 10 años de experiencia en cortes clásicos y modernos",
            "juan-perez.jpg",
            "password123"
        );

        // Barbero 2: Carlos Rodríguez
        Barber barber2 = createBarber(
            "Carlos",
            "Rodríguez",
            "carlos.rodriguez@barbershop.com",
            "3009876543",
            "Especialista en degradados y cortes modernos para hombres",
            "carlos-rodriguez.jpg",
            "password123"
        );

        // Barbero 3: Miguel Ángel López
        Barber barber3 = createBarber(
            "Miguel",
            "López",
            "miguel.lopez@barbershop.com",
            "3005551234",
            "Experto en barbería tradicional y afeitado con navaja",
            "miguel-lopez.jpg",
            "password123"
        );

        System.out.println("✅ Barberos creados: " + barberRepository.count());

        // ============================================================
        // 2. CREAR HORARIOS DE TRABAJO
        // ============================================================

        // Horario estándar de lunes a viernes (9:00 - 18:00)
        createStandardWeekSchedule(barber1);
        createStandardWeekSchedule(barber2);
        
        // Horario extendido para barber3 (incluye sábado)
        createExtendedSchedule(barber3);

        System.out.println("✅ Horarios creados: " + workShiftRepository.count());

        // ============================================================
        // 3. PUBLICAR EVENTOS DE BARBEROS CREADOS
        // ============================================================
        // Los servicios aún no están en esta BD, pero publicamos los eventos
        // para que Reservations y otros microservicios sepan de estos barberos
        publishBarberEvents(Arrays.asList(barber1, barber2, barber3));

        System.out.println("🎉 Base de datos de barberos inicializada correctamente!");
        System.out.println("⚠️ NOTA: Los servicios se sincronizarán cuando el microservicio de Servicios publique sus eventos.");
    }

    /**
     * Crea un barbero y lo registra en Keycloak
     */
    private Barber createBarber(String name, String lastName, String email, 
                                String phone, String description, String image, String password) {
        try {
            // Crear DTO para Keycloak
            BarberDTO barberDTO = new BarberDTO();
            barberDTO.setName(name);
            barberDTO.setLastName(lastName);
            barberDTO.setEmail(email);
            barberDTO.setPassword(password);
            barberDTO.setPhone(phone);
            barberDTO.setDescription(description);
            barberDTO.setImage(image);
            barberDTO.setAvailability(true);
            barberDTO.setContract(true);

            // Generar ID único
            String barberId = UUID.randomUUID().toString();
            barberDTO.setId(barberId);

            // Crear usuario en Keycloak y obtener keycloakId
            String keycloakId = keycloakService.createUserForBarber(barberDTO);

            // Crear entidad Barber
            Barber barber = new Barber();
            barber.setId(barberId);
            barber.setName(name);
            barber.setLastName(lastName);
            barber.setEmail(email);
            barber.setPhone(phone);
            barber.setDescription(description);
            barber.setImage(image);
            barber.setKeycloakId(keycloakId);
            barber.setAvailability(true);
            barber.setContract(true);

            return barberRepository.save(barber);
        } catch (Exception e) {
            System.err.println("⚠️ Error creando barbero " + name + ": " + e.getMessage());
            // Si falla Keycloak, crear solo en BD local
            Barber barber = new Barber();
            barber.setId(UUID.randomUUID().toString());
            barber.setName(name);
            barber.setLastName(lastName);
            barber.setEmail(email);
            barber.setPhone(phone);
            barber.setDescription(description);
            barber.setImage(image);
            barber.setAvailability(true);
            barber.setContract(true);
            return barberRepository.save(barber);
        }
    }

    /**
     * Crea horario estándar de lunes a viernes (9:00 - 18:00)
     */
    private void createStandardWeekSchedule(Barber barber) {
        List<DayOfWeekEnum> weekdays = Arrays.asList(
            DayOfWeekEnum.LUNES,
            DayOfWeekEnum.MARTES,
            DayOfWeekEnum.MIERCOLES,
            DayOfWeekEnum.JUEVES,
            DayOfWeekEnum.VIERNES
        );

        for (DayOfWeekEnum day : weekdays) {
            WorkShift shift = new WorkShift();
            shift.setBarber(barber);
            shift.setDayOfWeek(day);
            shift.setStartTime(LocalTime.of(9, 0));
            shift.setEndTime(LocalTime.of(18, 0));
            WorkShift saved = workShiftRepository.save(shift);
            
            // Publicar evento de WorkShift creado
            publishWorkShiftEvent(saved);
        }
    }

    /**
     * Crea horario extendido de lunes a sábado
     */
    private void createExtendedSchedule(Barber barber) {
        // Lunes a viernes: 9:00 - 19:00
        List<DayOfWeekEnum> weekdays = Arrays.asList(
            DayOfWeekEnum.LUNES,
            DayOfWeekEnum.MARTES,
            DayOfWeekEnum.MIERCOLES,
            DayOfWeekEnum.JUEVES,
            DayOfWeekEnum.VIERNES
        );

        for (DayOfWeekEnum day : weekdays) {
            WorkShift shift = new WorkShift();
            shift.setBarber(barber);
            shift.setDayOfWeek(day);
            shift.setStartTime(LocalTime.of(9, 0));
            shift.setEndTime(LocalTime.of(19, 0));
            WorkShift saved = workShiftRepository.save(shift);
            
            // Publicar evento de WorkShift creado
            publishWorkShiftEvent(saved);
        }

        // Sábado: 9:00 - 14:00
        WorkShift saturdayShift = new WorkShift();
        saturdayShift.setBarber(barber);
        saturdayShift.setDayOfWeek(DayOfWeekEnum.SABADO);
        saturdayShift.setStartTime(LocalTime.of(9, 0));
        saturdayShift.setEndTime(LocalTime.of(14, 0));
        WorkShift savedSaturday = workShiftRepository.save(saturdayShift);
        
        // Publicar evento de WorkShift del sábado
        publishWorkShiftEvent(savedSaturday);
    }

    /**
     * Publica eventos de barberos creados para que otros microservicios se enteren
     */
    private void publishBarberEvents(List<Barber> barbers) {
        for (Barber barber : barbers) {
            try {
                BarberDTO dto = new BarberDTO();
                dto.setId(barber.getId());
                dto.setName(barber.getName());
                dto.setLastName(barber.getLastName());
                dto.setEmail(barber.getEmail());
                dto.setPhone(barber.getPhone());
                dto.setDescription(barber.getDescription());
                dto.setImage(barber.getImage());
                dto.setContract(true);
                dto.setAvailability(true);

                // Publicar evento sin servicios (se asignarán cuando lleguen los eventos de servicios)
                barberEventPublisher.publishBarberCreated(dto, Collections.emptyList());
                System.out.println("✅ Evento publicado para barbero: " + barber.getName());
            } catch (Exception e) {
                System.err.println("⚠️ Error publicando evento para barbero " + barber.getName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Publica un evento de WorkShift creado
     */
private void publishWorkShiftEvent(WorkShift workShift) {
    try {
        WorkShiftDTO dto = new WorkShiftDTO();
        dto.setId(workShift.getId());
        dto.setDayOfWeek(workShift.getDayOfWeek());
        dto.setStartTime(workShift.getStartTime());
        dto.setEndTime(workShift.getEndTime());
        dto.setBarberId(workShift.getBarber().getId());
        
        workShiftEventPublisher.publishWorkShiftCreated(dto);
        
        // ⏱️ Pequeño delay para evitar saturar RabbitMQ
        Thread.sleep(50); // 50ms entre eventos
        
        System.out.println("✅ Evento WorkShift publicado: " + workShift.getDayOfWeek());
    } catch (Exception e) {
        System.err.println("⚠️ Error publicando evento de WorkShift: " + e.getMessage());
    }
}
}
