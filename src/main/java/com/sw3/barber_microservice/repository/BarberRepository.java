package com.sw3.barber_microservice.repository;

import com.sw3.barber_microservice.model.Barber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BarberRepository extends JpaRepository<Barber, String> {
    public boolean existsByEmail(String email);
}
