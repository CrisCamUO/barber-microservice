package com.sw3.barber_microservice.service;

import com.sw3.barber_microservice.dto.BarberDTO;

public interface KeycloakService {
    /**
     * Create a Keycloak user for the barber and return the Keycloak user id.
     * If a user with the same email already exists, returns the existing id.
     */
    String createUserForBarber(BarberDTO barberDto);
}
