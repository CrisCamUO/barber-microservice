package com.sw3.barber_microservice.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.JoinTable;
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
    private String id;

    private String name;

    private String lastName;

    private String email;

    private String phone;

    private String description;

    private String image;

    @OneToMany(mappedBy = "barber", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<WorkShift> workShifts = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "barber_service",
        joinColumns = @JoinColumn(name = "barber_id"),
        inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private List<Service> services = new ArrayList<>();

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean availability;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean contract;

    // Relationship Barber <-> Service modelled as ManyToMany with join table `barber_service`.

    public Barber() {}

    @PrePersist
    private void ensureId() {
        if (this.id == null || this.id.isBlank()) {
            this.id = java.util.UUID.randomUUID().toString();
        }
    }

}
