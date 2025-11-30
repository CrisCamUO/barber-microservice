package com.sw3.barber_microservice.service;

import com.sw3.barber_microservice.dto.BarberDTO;
import com.sw3.barber_microservice.dto.ServiceDTO;

import java.util.List;
import java.util.Optional;

public interface BarberService {
    List<BarberDTO> findAll();
    Optional<BarberDTO> findById(Long id);
    BarberDTO save(BarberDTO barberDto);
    void deleteById(Long id);

    // Associations
    ServiceDTO assignServiceToBarber(Long barberId, Long serviceId);
    void unassignServiceFromBarber(Long barberId, Long serviceId);
    List<ServiceDTO> getServicesByBarber(Long barberId);

    // Bulk assign
    List<ServiceDTO> assignServicesToBarber(Long barberId, java.util.List<Long> serviceIds);
}
