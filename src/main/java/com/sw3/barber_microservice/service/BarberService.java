package com.sw3.barber_microservice.service;

import com.sw3.barber_microservice.dto.BarberDTO;
import com.sw3.barber_microservice.dto.ServiceDTO;

import java.util.List;
import java.util.Optional;

public interface BarberService {
    List<BarberDTO> findAll();
    Optional<BarberDTO> findById(String id);
    BarberDTO save(BarberDTO barberDto);
    void deleteById(String id);

    // Associations
    ServiceDTO assignServiceToBarber(String barberId, Long serviceId);
    void unassignServiceFromBarber(String barberId, Long serviceId);
    List<ServiceDTO> getServicesByBarber(String barberId);

    // Bulk assign
    List<ServiceDTO> assignServicesToBarber(String barberId, java.util.List<Long> serviceIds);
    
    // State changes
    BarberDTO setContractFalse(String barberId);
    BarberDTO setAvailability(String barberId, boolean availability);
}
