package com.example.projects;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AppTest {
    @Test
    void createsExpectedGreeting() {
        assertEquals("Hello from the Java 21 project scaffold.", App.greeting().message());
    }
}
