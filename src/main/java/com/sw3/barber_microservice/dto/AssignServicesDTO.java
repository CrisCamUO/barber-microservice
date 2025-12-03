package com.sw3.barber_microservice.dto;

import java.util.List;

public class AssignServicesDTO {

    private List<Long> serviceIds;

    public AssignServicesDTO() {}

    public List<Long> getServiceIds() {
        return serviceIds;
    }

    public void setServiceIds(List<Long> serviceIds) {
        this.serviceIds = serviceIds;
    }
}
