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
    private Long id;

    private String name;

    // Nuevo campo para saber si el servicio está activo o fue eliminado lógicamente
    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean active = true;

    @ManyToMany(mappedBy = "services", fetch = FetchType.LAZY)
    private List<Barber> barbers = new ArrayList<>();

    public Service() {}
}