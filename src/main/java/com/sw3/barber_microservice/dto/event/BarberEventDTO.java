package com.sw3.barber_microservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BarberEventDTO {
    private String id;
    private String name;
    private String email; 
    private Boolean active; // Mapearemos 'contract' aquí
    
    // Lista de servicios que este barbero realiza (para sincronizar la relación allá)
    private List<String> serviceIds; 
}