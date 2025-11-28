package com.sw3.barber_microservice.service.impl;

import com.sw3.barber_microservice.dto.BarberDTO;
import com.sw3.barber_microservice.messaging.BarberEventPublisher;
import com.sw3.barber_microservice.model.Barber;
import com.sw3.barber_microservice.service.BarberService;
//import com.sw3.barber_microservice.model.BarberService;
import com.sw3.barber_microservice.repository.BarberRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BarberServiceImpl implements BarberService {

    @Autowired
    private final BarberRepository barberRepository;
    @Autowired
    private final ModelMapper modelMapper;
    @Autowired
    private final BarberEventPublisher barberEventPublisher;

    public BarberServiceImpl(BarberRepository barberRepository, ModelMapper modelMapper, BarberEventPublisher barberEventPublisher) {
        this.barberRepository = barberRepository;
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
    public Optional<BarberDTO> findById(Long id) {
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
        if (saved.getBarberServices() != null) {
            serviceIds = saved.getBarberServices().stream()
                    .map(bs -> bs.getService().getId())
                    .collect(Collectors.toList());
        }

        BarberDTO savedDto = modelMapper.map(saved, BarberDTO.class);

        // 5. Publicación del Evento (Con lista de servicios)
        if (esNuevo) {
            barberEventPublisher.publishBarberCreated(savedDto, serviceIds);
        } else {
            barberEventPublisher.publishBarberUpdated(savedDto, serviceIds);
        }
        
        return savedDto;
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        // 1. Avisar al otro microservicio primero (para que limpie sus referencias)
        barberEventPublisher.publishBarberInactivated(id);

        // 2. Borrado físico local
        barberRepository.deleteById(id);
    }
}