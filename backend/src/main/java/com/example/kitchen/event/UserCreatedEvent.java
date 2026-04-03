package com.example.kitchen.event;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class UserCreatedEvent extends ApplicationEvent {
    private UUID userId;

    public UserCreatedEvent(Object publisher, UUID userId) {
        super(publisher);
        this.userId = userId;
    }
    public UUID getUserId(){
        return userId;
    }
}
