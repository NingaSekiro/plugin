package org.aopbuddy.plugin.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class DbSyncServiceTest {

    @Test
    void getTime() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy:MM:dd:HH:mm"));
        assertNotNull(time);
    }
}