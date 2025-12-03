package com.sw3.barber_microservice.controller;

import com.sw3.barber_microservice.dto.WorkShiftDTO;
import com.sw3.barber_microservice.service.WorkShiftService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
public class WorkShiftController {

    private final WorkShiftService workShiftService;

    public WorkShiftController(WorkShiftService workShiftService) {
        this.workShiftService = workShiftService;
    }
    
    @GetMapping("/admin/workshifts")
    public ResponseEntity<List<WorkShiftDTO>> getAll() {
        List<WorkShiftDTO> workShifts = workShiftService.findAll();
        if (workShifts.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(workShifts);
    }

    //6-Ver horario laboral barbero por Id
    @GetMapping("/admin/workshifts/{id}")
    public ResponseEntity<WorkShiftDTO> getById(@PathVariable Long id) {
        return workShiftService.findById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
    //Crea un horario de trabajo con los datos del WorkShiftDTO (incluyendo el ID del barbero asociado)
    /*
    @PostMapping("/admin/workshifts")
    public ResponseEntity<WorkShiftDTO> create(@Valid @RequestBody WorkShiftDTO workShiftDto) {
        WorkShiftDTO saved = workShiftService.save(workShiftDto);
        if (saved != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }*/
    //7- Asignarle un nuevo horario de trabajo a un barbero existente
    @PutMapping("/admin/workshifts/{id}/horarios")
    public ResponseEntity<WorkShiftDTO> update(@PathVariable String id, @RequestBody WorkShiftDTO workShiftDto) {
        WorkShiftDTO updated = workShiftService.save(workShiftDto,id);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/admin/workshifts/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workShiftService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/admin/workshifts/{id}/horarios")
    public ResponseEntity<List<WorkShiftDTO>> getByBarberId(@PathVariable String id) {
        List<WorkShiftDTO> workShifts = workShiftService.findByBarberId(id);
        if (workShifts.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(workShifts);
        }
    }
    
}
