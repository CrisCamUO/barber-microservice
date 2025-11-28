package com.sw3.barber_microservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceEventDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer duration;
    
    // Lista de barberos asignados a este servicio (para sincronizar la relación aquí)
    private List<Long> barberIds; 
    
    private String availabilityStatus; // "Disponible" | "No Disponible"
    private String systemStatus;       // "Activo" | "Inactivo"
}