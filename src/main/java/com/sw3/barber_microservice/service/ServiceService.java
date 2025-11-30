package com.sw3.barber_microservice.service;

import com.sw3.barber_microservice.dto.ServiceDTO;

import java.util.List;
import java.util.Optional;

public interface ServiceService {

    List<ServiceDTO> findAll();

    Optional<ServiceDTO> findById(String id);

    ServiceDTO save(ServiceDTO serviceDto);

    void deleteById(String id);

}
