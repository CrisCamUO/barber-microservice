package com.sw3.barber_microservice.controller;

import com.sw3.barber_microservice.dto.BarberDTO;
import com.sw3.barber_microservice.service.BarberService;
import com.sw3.barber_microservice.service.ServiceService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import com.sw3.barber_microservice.dto.ServiceDTO;
import com.sw3.barber_microservice.dto.AssignServicesDTO;

import org.springframework.web.multipart.MultipartFile;


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
    public ResponseEntity<BarberDTO> getById(@PathVariable String id) {
        return barberService.findById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
    //Crea un barbero con los datos del BarberDTO (sin el horario de trabajo)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BarberDTO> create(
            @RequestPart("name") String name,
            @RequestPart(value = "lastName", required = false) String lastName,
            @RequestPart("email") String email,
            @RequestPart(value = "phone", required = false) String phone,
            @RequestPart(value = "description", required = false) String specialty,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

        BarberDTO barberDto = new BarberDTO();
        barberDto.setName(name);
        barberDto.setEmail(email);
        barberDto.setPhone(phone);
        if (image != null && !image.isEmpty()) {
            // Carpeta uploads en la raíz del proyecto (puedes cambiarla a una ruta configurable)
            Path uploadDir = Paths.get("uploads").toAbsolutePath();
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // Generar nombre único para evitar colisiones
            String original = image.getOriginalFilename();
            String ext = "";
            if (original != null && original.contains(".")) {
                ext = original.substring(original.lastIndexOf('.'));
            }
            String filename = UUID.randomUUID().toString() + ext;
            Path target = uploadDir.resolve(filename);

            // Guardar archivo
            image.transferTo(target.toFile());
            // Guardar la ruta relativa o absoluta según prefieras. Aquí guardamos ruta relativa.
            barberDto.setImage("uploads/" + filename);
        }
        BarberDTO saved = barberService.save(barberDto);
        if (saved == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BarberDTO> update(@PathVariable String id, @RequestBody BarberDTO barberDto) {
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
    public ResponseEntity<Void> delete(@PathVariable String id) {
        barberService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/{barberId}/services/{serviceId}")
    public ResponseEntity<ServiceDTO> assignService(@PathVariable String barberId, @PathVariable String serviceId) {
        ServiceDTO dto = barberService.assignServiceToBarber(barberId, serviceId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @DeleteMapping("/{barberId}/services/{serviceId}")
    public ResponseEntity<Void> unassignService(@PathVariable String barberId, @PathVariable String serviceId) {
        barberService.unassignServiceFromBarber(barberId, serviceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{barberId}/services")
    public ResponseEntity<List<ServiceDTO>> listServicesByBarber(@PathVariable String barberId) {
        List<ServiceDTO> services = barberService.getServicesByBarber(barberId);
        return ResponseEntity.ok(services);
    }

    @PostMapping("/{barberId}/services/bulk")
    public ResponseEntity<List<ServiceDTO>> assignServicesBulk(@PathVariable String barberId, @RequestBody AssignServicesDTO dto) {
        List<ServiceDTO> assigned = barberService.assignServicesToBarber(barberId, dto.getServiceIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(assigned);
    }

    @GetMapping("/services")
    public ResponseEntity<List<ServiceDTO>> getServices() {
        List<ServiceDTO> services = serviceService.findAll();
        return ResponseEntity.ok(services);
    }
    
}
