package com.sw3.barber_microservice.service.impl;

import com.sw3.barber_microservice.dto.BarberDTO;
import com.sw3.barber_microservice.messaging.BarberEventPublisher;
import com.sw3.barber_microservice.model.Barber;
import com.sw3.barber_microservice.model.Service;
import com.sw3.barber_microservice.dto.ServiceDTO;
import com.sw3.barber_microservice.event.BarberCreatedEvent;
import com.sw3.barber_microservice.service.BarberService;
import com.sw3.barber_microservice.service.KeycloakService;
import com.sw3.barber_microservice.repository.BarberRepository;
import com.sw3.barber_microservice.repository.ServiceRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class BarberServiceImpl implements BarberService {

    @Autowired
    private final BarberRepository barberRepository;
    @Autowired
    private final ServiceRepository serviceRepository;
    @Autowired
    private final ModelMapper modelMapper;
    @Autowired
    private final BarberEventPublisher barberEventPublisher;
    @Autowired
    private org.springframework.context.ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private KeycloakService keycloakService;

    public BarberServiceImpl(BarberRepository barberRepository, ServiceRepository serviceRepository, ModelMapper modelMapper, BarberEventPublisher barberEventPublisher) {
        this.barberRepository = barberRepository;
        this.serviceRepository = serviceRepository;
        this.modelMapper = modelMapper;
        this.barberEventPublisher = barberEventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BarberDTO> findAll() {
        return barberRepository.findAll().stream()
                .map(b -> modelMapper.map(b, BarberDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BarberDTO> findById(String id) {
        return barberRepository.findById(id).map(b -> modelMapper.map(b, BarberDTO.class));
    }

    @Override
    @Transactional
    public BarberDTO save(BarberDTO barberDto) {
        // 1. Lógica de Validación de Email (Mejorada para permitir Updates)
        boolean esNuevo = (barberDto.getId() == null);
        
        if (esNuevo) {
            if (barberRepository.existsByEmail(barberDto.getEmail())) {
                System.err.println("Error: El correo " + barberDto.getEmail() + " ya está en uso.");
                return null; // Idealmente lanzar excepción
            }
        } 
        // Nota: Si es update, no validamos existencia porque el email ya es nuestro.

        // 2. Valores por defecto
        if (barberDto.getAvailability() == null) {
            barberDto.setAvailability(true);
        }
        if (barberDto.getContract() == null) {
            barberDto.setContract(true);
        }

        // 3. Mapeo y Guardado
        Barber barber = modelMapper.map(barberDto, Barber.class);
        Barber saved = barberRepository.save(barber);

        // 4. Extracción de IDs de Servicios para sincronización
        // Obtenemos la lista de servicios asociados para enviarla al otro MS
        List<Long> serviceIds = new ArrayList<>();
        if (saved.getServices() != null) {
            serviceIds = saved.getServices().stream()
                .map(svc -> svc.getId())
                .collect(Collectors.toList());
        }

        BarberDTO savedDto = modelMapper.map(saved, BarberDTO.class);
        // Preserve original password (not persisted in the Barber entity) so Keycloak sync can use it
        savedDto.setPassword(barberDto.getPassword());

        // 5. Publicación del Evento (Con lista de servicios)
        if (esNuevo) {
            barberEventPublisher.publishBarberCreated(savedDto, serviceIds);
            // publish application event for after-commit Keycloak sync
            applicationEventPublisher.publishEvent(new BarberCreatedEvent(this, savedDto));
            //keycloakService.createUserForBarber(savedDto);
        } else {
            barberEventPublisher.publishBarberUpdated(savedDto, serviceIds);
        }

        return savedDto;
    }

    @Override
    @Transactional
    public ServiceDTO assignServiceToBarber(String barberId, Long serviceId) {
        Barber barber = barberRepository.findById(barberId).orElseThrow(() -> new IllegalArgumentException("Barber not found"));
        Service service = serviceRepository.findById(serviceId).orElseThrow(() -> new IllegalArgumentException("Service not found"));

        if (!barber.getServices().contains(service)) {
            barber.getServices().add(service);
        }
        if (!service.getBarbers().contains(barber)) {
            service.getBarbers().add(barber);
        }

        barberRepository.save(barber);

        return modelMapper.map(service, ServiceDTO.class);
    }

    @Override
    @Transactional
    public void unassignServiceFromBarber(String barberId, Long serviceId) {
        Barber barber = barberRepository.findById(barberId).orElseThrow(() -> new IllegalArgumentException("Barber not found"));
        Service service = serviceRepository.findById(serviceId).orElseThrow(() -> new IllegalArgumentException("Service not found"));

        barber.getServices().remove(service);
        service.getBarbers().remove(barber);

        barberRepository.save(barber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceDTO> getServicesByBarber(String barberId) {
        Barber barber = barberRepository.findById(barberId).orElseThrow(() -> new IllegalArgumentException("Barber not found"));
        return barber.getServices().stream().map(s -> modelMapper.map(s, ServiceDTO.class)).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<ServiceDTO> assignServicesToBarber(String barberId, List<Long> serviceIds) {
        Barber barber = barberRepository.findById(barberId).orElseThrow(() -> new IllegalArgumentException("Barber not found"));

        List<Service> services = serviceRepository.findAllById(serviceIds);

        // add each service if not present and sync inverse
        for (Service svc : services) {
            if (!barber.getServices().contains(svc)) {
                barber.getServices().add(svc);
            }
            if (!svc.getBarbers().contains(barber)) {
                svc.getBarbers().add(barber);
            }
        }

        barberRepository.save(barber);

        return services.stream().map(s -> modelMapper.map(s, ServiceDTO.class)).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        // 1. Avisar al otro microservicio primero (para que limpie sus referencias)
        barberEventPublisher.publishBarberInactivated(id);

        // 2. Borrado físico local
        barberRepository.deleteById(id);
    }

    @Override
    @Transactional
    public BarberDTO setContractFalse(String barberId) {
        Barber barber = barberRepository.findById(barberId).orElseThrow(() -> new IllegalArgumentException("Barber not found"));
        barber.setContract(false);
        Barber saved = barberRepository.save(barber);

        List<Long> serviceIds = new ArrayList<>();
        if (saved.getServices() != null) {
            serviceIds = saved.getServices().stream().map(s -> s.getId()).collect(Collectors.toList());
        }

        BarberDTO dto = modelMapper.map(saved, BarberDTO.class);
        barberEventPublisher.publishBarberUpdated(dto, serviceIds);
        return dto;
    }

    @Override
    @Transactional
    public BarberDTO setAvailability(String barberId, boolean availability) {
        Barber barber = barberRepository.findById(barberId).orElseThrow(() -> new IllegalArgumentException("Barber not found"));
        barber.setAvailability(availability);
        Barber saved = barberRepository.save(barber);

        List<Long> serviceIds = new ArrayList<>();
        if (saved.getServices() != null) {
            serviceIds = saved.getServices().stream().map(s -> s.getId()).collect(Collectors.toList());
        }

        BarberDTO dto = modelMapper.map(saved, BarberDTO.class);
        barberEventPublisher.publishBarberUpdated(dto, serviceIds);
        return dto;
    }
}