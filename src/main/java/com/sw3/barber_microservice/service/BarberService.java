package com.sw3.barber_microservice.service;

import com.sw3.barber_microservice.dto.BarberDTO;

import java.util.List;
import java.util.Optional;

public interface BarberService {
    List<BarberDTO> findAll();
    Optional<BarberDTO> findById(Long id);
    BarberDTO save(BarberDTO barberDto);
    void deleteById(Long id);
}
