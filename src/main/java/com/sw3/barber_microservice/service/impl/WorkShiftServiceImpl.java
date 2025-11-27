package com.sw3.barber_microservice.service.impl;

import com.sw3.barber_microservice.dto.WorkShiftDTO;
import com.sw3.barber_microservice.model.Barber;
import com.sw3.barber_microservice.model.DayOfWeekEnum;
import com.sw3.barber_microservice.model.WorkShift;
import com.sw3.barber_microservice.repository.BarberRepository;
import com.sw3.barber_microservice.repository.WorkShiftRepository;
import com.sw3.barber_microservice.service.WorkShiftService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class WorkShiftServiceImpl implements WorkShiftService {
    @Autowired
    private final WorkShiftRepository workShiftRepository;
    @Autowired
    private final BarberRepository barberRepository;
    @Autowired
    private final ModelMapper modelMapper;

    public WorkShiftServiceImpl(WorkShiftRepository workShiftRepository, BarberRepository barberRepository, ModelMapper modelMapper) {
        this.workShiftRepository = workShiftRepository;
        this.barberRepository = barberRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<WorkShiftDTO> findAll() {
        return workShiftRepository.findAll().stream().map(ws -> {
            WorkShiftDTO dto = modelMapper.map(ws, WorkShiftDTO.class);
            dto.setBarberId(ws.getBarber() != null ? ws.getBarber().getId() : null);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public Optional<WorkShiftDTO> findById(Long id) {
        return workShiftRepository.findById(id).map(ws -> {
            WorkShiftDTO dto = modelMapper.map(ws, WorkShiftDTO.class);
            dto.setBarberId(ws.getBarber() != null ? ws.getBarber().getId() : null);
            return dto;
        });
    }

    @Override
    public List<WorkShiftDTO> findByBarberId(Long barberId) {
        return workShiftRepository.findByBarberId(barberId).stream().map(ws -> {
            WorkShiftDTO dto = modelMapper.map(ws, WorkShiftDTO.class);
            dto.setBarberId(ws.getBarber() != null ? ws.getBarber().getId() : null);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public WorkShiftDTO save(WorkShiftDTO workShiftDto) {
        WorkShift ws = modelMapper.map(workShiftDto, WorkShift.class);
        //Validamos si el id del barbero en el dto existe y le asignamos una entidad barbero a la entidad worshift
        if (workShiftDto.getBarberId() != null) {
            Barber barber = barberRepository.findById(workShiftDto.getBarberId()).orElse(null);
            ws.setBarber(barber);
        } else {
            System.err.println("Error: No existe el barbero con ID " + workShiftDto.getBarberId());
            return null;
            //ws.setBarber(null);
        }
        //Validamos que no se traslape los horarios de trabajo para el mismo barbero
            //obtener los horarios existentes para el barbero
        List<WorkShift> existingShifts = workShiftRepository.findByBarberId(ws.getBarber().getId());
            //validar traslapes
        for (WorkShift existing : existingShifts) {
            if (ws.getId() != null && ws.getId().equals(existing.getId())) {
                continue; // saltar la misma entrada al actualizar
            }
            boolean overlap = seTraslapa(ws.getDayOfWeek(), existing.getDayOfWeek(), ws.getStartTime(), ws.getEndTime(), existing.getStartTime(), existing.getEndTime());
            if (overlap) {
                System.err.println("Error: El horario de trabajo se traslapa con un horario existente para el barbero con ID " + ws.getBarber().getId());
                return null;
            }
        }
        WorkShift saved = workShiftRepository.save(ws);
        WorkShiftDTO out = modelMapper.map(saved, WorkShiftDTO.class);
        out.setBarberId(saved.getBarber() != null ? saved.getBarber().getId() : null);
        return out;
    }

    @Override
    public void deleteById(Long id) {
        workShiftRepository.deleteById(id);
    }

    

    public boolean seTraslapa(DayOfWeekEnum day1,DayOfWeekEnum day2, LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        return day1 == day2 && start1.isBefore(end2) && start2.isBefore(end1);
    }
}
