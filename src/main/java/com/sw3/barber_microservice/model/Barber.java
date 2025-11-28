package com.sw3.barber_microservice.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "barbers")
@Data
public class Barber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String lastName;

    private String email;

    private String phone;

    private String description;

    @OneToMany(mappedBy = "barber", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<WorkShift> workShifts = new ArrayList<>();

    @OneToMany(mappedBy = "barber", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<BarberService> barberServices = new ArrayList<>();

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean availability;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean contract;

    public Barber() {}

}
