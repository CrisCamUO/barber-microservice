package com.sw3.barber_microservice.repository;

import com.sw3.barber_microservice.model.WorkShift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkShiftRepository extends JpaRepository<WorkShift, String> {
    public java.util.List<WorkShift> findByBarberId(String barberId);
}
