package com.sw3.barber_microservice.dto;

public class ServiceDTO {

    private Long id;

    private String name;


    public ServiceDTO() {}

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

}
