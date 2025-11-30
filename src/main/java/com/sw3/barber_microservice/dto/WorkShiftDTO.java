package com.sw3.barber_microservice.dto;

import com.sw3.barber_microservice.model.DayOfWeekEnum;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class WorkShiftDTO {
    
    private String id;
    @NotNull
    private DayOfWeekEnum dayOfWeek;
    @NotNull
    private LocalTime startTime;
    @NotNull
    private LocalTime endTime;
    @NotNull
    private String barberId;

    public WorkShiftDTO() {
    }

}
