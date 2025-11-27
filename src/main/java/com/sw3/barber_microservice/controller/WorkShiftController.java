package com.sw3.barber_microservice.controller;

import com.sw3.barber_microservice.dto.WorkShiftDTO;
import com.sw3.barber_microservice.service.WorkShiftService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/workshifts")
public class WorkShiftController {

    private final WorkShiftService workShiftService;

    public WorkShiftController(WorkShiftService workShiftService) {
        this.workShiftService = workShiftService;
    }

    @GetMapping
    public ResponseEntity<List<WorkShiftDTO>> getAll() {
        List<WorkShiftDTO> workShifts = workShiftService.findAll();
        if (workShifts.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(workShifts);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkShiftDTO> getById(@PathVariable Long id) {
        return workShiftService.findById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
    //Crea un horario de trabajo con los datos del WorkShiftDTO (incluyendo el ID del barbero asociado)
    @PostMapping
    public ResponseEntity<WorkShiftDTO> create(@Valid @RequestBody WorkShiftDTO workShiftDto) {
        WorkShiftDTO saved = workShiftService.save(workShiftDto);
        if (saved != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkShiftDTO> update(@PathVariable Long id, @RequestBody WorkShiftDTO workShiftDto) {
        return workShiftService.findById(id)
                .map(existing -> {
                    // ensure DTO id matches path id
                    workShiftDto.setId(id);
                    WorkShiftDTO saved = workShiftService.save(workShiftDto);//save crea o actualiza
                    return ResponseEntity.ok(saved);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workShiftService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/barberId/{id}")
    public ResponseEntity<List<WorkShiftDTO>> getByBarberId(@PathVariable Long id) {
        List<WorkShiftDTO> workShifts = workShiftService.findByBarberId(id);
        if (workShifts.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(workShifts);
        }
    }
    
}
