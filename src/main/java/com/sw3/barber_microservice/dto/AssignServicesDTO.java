package com.sw3.barber_microservice.dto;

import java.util.List;

public class AssignServicesDTO {

    private List<String> serviceIds;

    public AssignServicesDTO() {}

    public List<String> getServiceIds() {
        return serviceIds;
    }

    public void setServiceIds(List<String> serviceIds) {
        this.serviceIds = serviceIds;
    }
}
