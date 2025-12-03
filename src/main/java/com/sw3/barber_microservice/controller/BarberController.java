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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
public class BarberController {
    private static final Logger log = LoggerFactory.getLogger(BarberController.class);
    @Autowired
    private final BarberService barberService;

    @Autowired
    private final ServiceService serviceService;

    public BarberController(BarberService barberService, ServiceService serviceService) {
        this.barberService = barberService;
        this.serviceService = serviceService;
    }
    //Obtener todos los barberos
    @GetMapping("/public/barbers")
    public List<BarberDTO> getAll() {
        return barberService.findAll();
    }
    //1. Obtener barbero por Id
    @GetMapping("/public/barbers/{id}")
    public ResponseEntity<BarberDTO> getById(@PathVariable String id) {
        return barberService.findById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
    //2- Crea un barbero con los datos del BarberDTO (sin el horario de trabajo)
    @PostMapping (path = "/admin/barbers",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BarberDTO> create(
            @RequestPart("name") String name,
            @RequestPart(value = "lastName", required = false) String lastName,
            @RequestPart("email") String email,
            @RequestPart(value = "phone", required = false) String phone,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart("password") String password,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

        BarberDTO barberDto = new BarberDTO();
        barberDto.setName(name);
        barberDto.setLastName(lastName);
        barberDto.setEmail(email);
        barberDto.setPhone(phone);
        barberDto.setDescription(description);
        barberDto.setPassword(password);
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
    //3- Actualizar datos barbero por Id
    @PutMapping(value = "/admin/barbers/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BarberDTO> update(
            @PathVariable String id,
            @RequestPart("name") String name,
            @RequestPart(value = "lastName", required = false) String lastName,
            @RequestPart("email") String email,
            @RequestPart(value = "phone", required = false) String phone,
            @RequestPart(value = "description", required = false) String description,
            @RequestPart(value = "password", required = false) String password,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

        return barberService.findById(id)
                .map(existing -> {
                    BarberDTO barberDto = new BarberDTO();
                    barberDto.setId(id);
                    barberDto.setName(name);
                    barberDto.setLastName(lastName);
                    barberDto.setEmail(email);
                    barberDto.setPhone(phone);
                    barberDto.setDescription(description);
                    barberDto.setPassword(password);
                    if (image != null && !image.isEmpty()) {
                        try {
                            Path uploadDir = Paths.get("uploads").toAbsolutePath();
                            if (!Files.exists(uploadDir)) {
                                Files.createDirectories(uploadDir);
                            }
                            String original = image.getOriginalFilename();
                            String ext = "";
                            if (original != null && original.contains(".")) {
                                ext = original.substring(original.lastIndexOf('.'));
                            }
                            String filename = UUID.randomUUID().toString() + ext;
                            Path target = uploadDir.resolve(filename);
                            image.transferTo(target.toFile());
                            barberDto.setImage("uploads/" + filename);
                        } catch (IOException e) {
                            log.error("Error saving image for barber {}: {}", id, e.getMessage());
                            throw new RuntimeException(e);
                        }
                    }
                    System.out.println("Password received in update: " + password);
                    BarberDTO saved = barberService.save(barberDto);
                    if (saved == null) {
                        log.error("Error: No se pudo actualizar el barbero con ID {}", id);
                    }
                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    //Eliminar barbero por Id - No se utiliza
    /*
    //@DeleteMapping("barberos/admin/barbers/{id}")
    @DeleteMapping("/barbers/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        barberService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    */
   /*
    //Asignar un servicio a barbero - No se utiliza
    @PostMapping("/admin/barbers/{barberId}/services/{serviceId}")
    //@PostMapping("/{barberId}/services/{serviceId}")
    public ResponseEntity<ServiceDTO> assignService(@PathVariable String barberId, @PathVariable Long serviceId) {
        ServiceDTO dto = barberService.assignServiceToBarber(barberId, serviceId);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
    */
    /*
    @DeleteMapping("admin/barbers/{barberId}/services/{serviceId}")
    //@DeleteMapping("/{barberId}/services/{serviceId}")
    public ResponseEntity<Void> unassignService(@PathVariable String barberId, @PathVariable Long serviceId) {
        barberService.unassignServiceFromBarber(barberId, serviceId);
        return ResponseEntity.noContent().build();
    }*/

    //4- Lista los servicios de un barbero por Id
    @GetMapping("/public/barbers/{barberId}/servicios")
    //@GetMapping("/{barberId}/services")
    public ResponseEntity<List<ServiceDTO>> listServicesByBarber(@PathVariable String barberId) {
        List<ServiceDTO> services = barberService.getServicesByBarber(barberId);
        return ResponseEntity.ok(services);
    }
    //5- Desactiva el contrato de un barbero por Id
    @DeleteMapping("/admin/barbers/{id}")
    public ResponseEntity<BarberDTO> deactivateContract(@PathVariable String id) {
        try {
            BarberDTO updated = barberService.setContractFalse(id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @PatchMapping("/admin/barbers/{id}/availability")
    public ResponseEntity<BarberDTO> setAvailability(@PathVariable String id, @RequestParam boolean available) {
        try {
            BarberDTO updated = barberService.setAvailability(id, available);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    //5- Asignar varios servicios a un barbero
    @PostMapping("/admin/barbers/{barberId}/servicios")
    public ResponseEntity<List<ServiceDTO>> assignServicesBulk(@PathVariable String barberId, @RequestBody AssignServicesDTO dto) {
        List<ServiceDTO> assigned = barberService.assignServicesToBarber(barberId, dto.getServiceIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(assigned);
    }
    //@GetMapping("barberos/public/services")
    @GetMapping("/barbers/services")
    public ResponseEntity<List<ServiceDTO>> getServices() {
        List<ServiceDTO> services = serviceService.findAll();
        return ResponseEntity.ok(services);
    }
    
}
