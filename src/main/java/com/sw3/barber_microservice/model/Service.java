package com.sw3.barber_microservice.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "services")
public class Service {

    @Id
    // El ID debe ser idéntico al del Microservicio de Servicios (Réplica Manual).
    private Long id;

    private String name;

    // Nuevo campo para saber si el servicio está activo o fue eliminado lógicamente
    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean active = true;

    @OneToMany(mappedBy = "service", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BarberService> barberServices = new ArrayList<>();

    public Service() {}
}