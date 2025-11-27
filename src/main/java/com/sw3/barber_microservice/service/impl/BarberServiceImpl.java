package com.sw3.barber_microservice.service.impl;

import com.sw3.barber_microservice.dto.BarberDTO;
import com.sw3.barber_microservice.messaging.BarberEventPublisher;
import com.sw3.barber_microservice.model.Barber;
import com.sw3.barber_microservice.repository.BarberRepository;
import com.sw3.barber_microservice.service.BarberService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public List<BarberDTO> findAll() {
        return barberRepository.findAll().stream()
                .map(b -> modelMapper.map(b, BarberDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<BarberDTO> findById(Long id) {
        return barberRepository.findById(id).map(b -> modelMapper.map(b, BarberDTO.class));
    }

    @Override
    public BarberDTO save(BarberDTO barberDto) {
        //Verificar que el correo sea único antes de guardar
        if (barberRepository.existsByEmail(barberDto.getEmail())) {
            System.err.println("Erro: El correo " + barberDto.getEmail() + " ya está en uso.");
            return null;
        }
        //Al momento de crearlo si en el DTO no vienen los valores de availability y contract, se ponen en true
        if (barberDto.isAvailability() == null) {
            barberDto.setAvailability(true);
        }
        if (barberDto.isContract() == null) {
            barberDto.setContract(true);
        }
        Barber barber = modelMapper.map(barberDto, Barber.class);
 
        Barber saved = barberRepository.save(barber);

        barberEventPublisher.publishBarberCreated(modelMapper.map(saved, BarberDTO.class));
        
        return modelMapper.map(saved, BarberDTO.class);
    }

    @Override
    public void deleteById(Long id) {
        barberRepository.deleteById(id);
    }
}
