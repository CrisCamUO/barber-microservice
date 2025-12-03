package com.sw3.barber_microservice.service.impl;

import com.sw3.barber_microservice.dto.ServiceDTO;
import com.sw3.barber_microservice.repository.ServiceRepository;
import com.sw3.barber_microservice.service.ServiceService;
import com.sw3.barber_microservice.model.Service;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class ServiceServiceImpl implements ServiceService {
    @Autowired
    private final ServiceRepository serviceRepository;
    @Autowired
    private final ModelMapper modelMapper;

    public ServiceServiceImpl(ServiceRepository serviceRepository, ModelMapper modelMapper) {
        this.serviceRepository = serviceRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<ServiceDTO> findAll() {
        return serviceRepository.findAll()
                .stream()
                .map(s -> modelMapper.map(s, ServiceDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ServiceDTO> findById(Long id) {
        return serviceRepository.findById(id).map(s -> modelMapper.map(s, ServiceDTO.class));
    }

    @Override
    public ServiceDTO save(ServiceDTO serviceDto) {
        Service service = modelMapper.map(serviceDto, Service.class);
        Service saved = serviceRepository.save(service);
        return modelMapper.map(saved, ServiceDTO.class);
    }

    @Override
    public void deleteById(Long id) {
        serviceRepository.deleteById(id);
    }

}
