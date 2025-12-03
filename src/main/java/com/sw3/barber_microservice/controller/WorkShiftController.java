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
    
    // Batch create/update workshifts for efficiency from frontend
    @PostMapping("/admin/workshifts/batch")
    public ResponseEntity<?> createBatch(@RequestBody List<com.sw3.barber_microservice.dto.WorkShiftRequestDTO> requests) {
        java.util.List<WorkShiftDTO> saved = new java.util.ArrayList<>();
        java.util.List<java.util.Map<String, Object>> errors = new java.util.ArrayList<>();

        java.time.format.DateTimeFormatter tf = java.time.format.DateTimeFormatter.ofPattern("HH:mm");

        for (int i = 0; i < requests.size(); i++) {
            com.sw3.barber_microservice.dto.WorkShiftRequestDTO r = requests.get(i);
            try {
                WorkShiftDTO dto = new WorkShiftDTO();
                dto.setId(r.getId());
                // parse dayOfWeek to enum (case-insensitive)
                if (r.getDayOfWeek() == null) throw new IllegalArgumentException("dayOfWeek is required");
                dto.setDayOfWeek(com.sw3.barber_microservice.model.DayOfWeekEnum.valueOf(r.getDayOfWeek().toUpperCase()));
                dto.setStartTime(java.time.LocalTime.parse(r.getStartTime(), tf));
                dto.setEndTime(java.time.LocalTime.parse(r.getEndTime(), tf));
                dto.setBarberId(r.getBarberId());

                WorkShiftDTO out = workShiftService.save(dto, r.getBarberId());
                if (out == null) {
                    java.util.Map<String, Object> err = new java.util.HashMap<>();
                    err.put("index", i);
                    err.put("reason", "Validation failed or overlap");
                    err.put("input", r);
                    errors.add(err);
                } else {
                    saved.add(out);
                }
            } catch (Exception e) {
                java.util.Map<String, Object> err = new java.util.HashMap<>();
                err.put("index", i);
                err.put("reason", e.getMessage());
                err.put("input", r);
                errors.add(err);
            }
        }

        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("saved", saved);
        resp.put("errors", errors);
        return ResponseEntity.ok(resp);
    }
    
}
