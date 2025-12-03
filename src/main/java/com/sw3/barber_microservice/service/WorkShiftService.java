package com.sw3.barber_microservice.service;

import com.sw3.barber_microservice.dto.WorkShiftDTO;

import java.util.List;
import java.util.Optional;

public interface WorkShiftService {
    List<WorkShiftDTO> findAll();
    Optional<WorkShiftDTO> findById(Long id);
    WorkShiftDTO save(WorkShiftDTO workShiftDto,String barberId);
    List<WorkShiftDTO> findByBarberId(String barberId);
    void deleteById(Long id);
}
