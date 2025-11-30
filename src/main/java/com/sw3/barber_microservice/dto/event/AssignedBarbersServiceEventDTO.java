package com.sw3.barber_microservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignedBarbersServiceEventDTO {
    private Long serviceId;
    private List<Long> barberIds;
}
