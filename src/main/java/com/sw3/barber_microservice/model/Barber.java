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

@Entity
@Table(name = "barbers")
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean isAvailability() {
        return availability;
    }

    public void setAvailability(Boolean availability) {
        this.availability = availability;
    }

    public Boolean isContract() {
        return contract;
    }

    public void setContract(Boolean contract) {
        this.contract = contract;
    }

    public List<WorkShift> getWorkShifts() {
        return workShifts;
    }

    public void setWorkShifts(List<WorkShift> workShifts) {
        this.workShifts = workShifts;
    }

    public List<BarberService> getBarberServices() {
        return barberServices;
    }

    public void setBarberServices(List<BarberService> barberServices) {
        this.barberServices = barberServices;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
