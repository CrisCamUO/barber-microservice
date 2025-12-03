package com.sw3.barber_microservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalTime;

@Entity
@Data
@Table(name = "work_shifts")
public class WorkShift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private DayOfWeekEnum dayOfWeek;

    private LocalTime startTime;

    private LocalTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id")
    private Barber barber;

    public WorkShift() {}

}
