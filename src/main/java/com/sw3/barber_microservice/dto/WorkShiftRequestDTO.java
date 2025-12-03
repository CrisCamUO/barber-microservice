package com.sw3.barber_microservice.dto;

import lombok.Data;

@Data
public class WorkShiftRequestDTO {
    private Long id; // opcional
    private String dayOfWeek; // e.g. "LUNES"
    private String startTime; // "HH:mm"
    private String endTime;   // "HH:mm"
    private String barberId;
}
