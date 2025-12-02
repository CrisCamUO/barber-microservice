package com.sw3.barber_microservice.listener;

import com.sw3.barber_microservice.event.BarberCreatedEvent;
import com.sw3.barber_microservice.service.KeycloakService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component

public class BarberCreatedListener {

    @Autowired
    private KeycloakService keycloakService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBarberCreated(BarberCreatedEvent event) {
        try {
            keycloakService.createUserForBarber(event.getBarber());
        } catch (Exception e) {
            // Log and let monitoring handle retries; do not rethrow to avoid breaking flow
            System.err.println("Keycloak sync failed for barber " + event.getBarber().getId() + ": " + e.getMessage());
        }
    }
}
