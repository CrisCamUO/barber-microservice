package com.sw3.barber_microservice.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@Data
@NoArgsConstructor
public class ServiceDTO {

    private Long id;

    private String name;

    private List<Long> barberIds;
}
