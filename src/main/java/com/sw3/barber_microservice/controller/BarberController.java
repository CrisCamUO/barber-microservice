package com.sw3.barber_microservice.controller;

import com.sw3.barber_microservice.dto.BarberDTO;
import com.sw3.barber_microservice.service.BarberService;
import com.sw3.barber_microservice.service.ServiceService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import com.sw3.barber_microservice.dto.ServiceDTO;
import com.sw3.barber_microservice.dto.AssignServicesDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/api/barbers")
public class BarberController {
    @Autowired
    private final BarberService barberService;

    @Autowired
    private final ServiceService serviceService;

    public BarberController(BarberService barberService, ServiceService serviceService) {
        this.barberService = barberService;
        this.serviceService = serviceService;
    }

    @GetMapping
    public List<BarberDTO> getAll() {
        return barberService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BarberDTO> getById(@PathVariable Long id) {
        return barberService.findById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
    //Crea un barbero con los datos del BarberDTO (sin el horario de trabajo)
    @PostMapping
    public ResponseEntity<BarberDTO> create(@Valid @RequestBody BarberDTO barberDto) {
        BarberDTO saved = barberService.save(barberDto);
        if (saved == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BarberDTO> update(@PathVariable Long id, @RequestBody BarberDTO barberDto) {
        return barberService.findById(id)
                .map(existing -> {
                    // ensure DTO id matches path id
                    barberDto.setId(id);
                    BarberDTO saved = barberService.save(barberDto);
                    if (saved == null) {
                        System.err.println("Error: No se pudo actualizar el barbero con ID " + id);
                    }
                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        barberService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{barberId}/services/{serviceId}")
    public ResponseEntity<ServiceDTO> assignService(@PathVariable Long barberId, @PathVariable Long serviceId) {
        ServiceDTO dto = barberService.assignServiceToBarber(barberId, serviceId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @DeleteMapping("/{barberId}/services/{serviceId}")
    public ResponseEntity<Void> unassignService(@PathVariable Long barberId, @PathVariable Long serviceId) {
        barberService.unassignServiceFromBarber(barberId, serviceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{barberId}/services")
    public ResponseEntity<List<ServiceDTO>> listServicesByBarber(@PathVariable Long barberId) {
        List<ServiceDTO> services = barberService.getServicesByBarber(barberId);
        return ResponseEntity.ok(services);
    }

    @PostMapping("/{barberId}/services/bulk")
    public ResponseEntity<List<ServiceDTO>> assignServicesBulk(@PathVariable Long barberId, @RequestBody AssignServicesDTO dto) {
        List<ServiceDTO> assigned = barberService.assignServicesToBarber(barberId, dto.getServiceIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(assigned);
    }

    @GetMapping("/services")
    public ResponseEntity<List<ServiceDTO>> getServices() {
        List<ServiceDTO> services = serviceService.findAll();
        return ResponseEntity.ok(services);
    }
    
}
