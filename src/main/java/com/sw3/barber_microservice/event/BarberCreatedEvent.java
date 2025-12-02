package com.sw3.barber_microservice.event;

import com.sw3.barber_microservice.dto.BarberDTO;
import org.springframework.context.ApplicationEvent;

public class BarberCreatedEvent extends ApplicationEvent {
    private final BarberDTO barber;

    public BarberCreatedEvent(Object source, BarberDTO barber) {
        super(source);
        this.barber = barber;
    }

    public BarberDTO getBarber() {
        return barber;
    }
}
