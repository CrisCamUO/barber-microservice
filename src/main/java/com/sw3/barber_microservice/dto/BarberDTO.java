package com.sw3.barber_microservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BarberDTO {
    
    private String id;
    @NotNull
    private String name;
    @NotNull
    private String lastName;
    @NotNull
    private String email;

    private String phone;

    private Boolean availability;

    private Boolean contract;

    private String description;

    private String image;

    private String keycloakId;
    @NotNull
    private String password;

    public BarberDTO() {
    }

   
}
