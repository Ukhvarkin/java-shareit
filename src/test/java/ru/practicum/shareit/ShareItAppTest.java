package ru.practicum.shareit;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class ShareItAppTest {
    @Test
    void mainTest() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));

        assertDoesNotThrow(() -> ShareItApp.main(new String[] {}));

        System.setOut(originalOut);
    }
}