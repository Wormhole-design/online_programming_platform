package com.szu.lox;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class test {
    private final InputStream systemIn = System.in;
    private final PrintStream systemOut = System.out;
    private final PrintStream systemErr = System.err;

    private ByteArrayInputStream testIn;
    private ByteArrayOutputStream testOut;

    @BeforeEach
    void setUpOutput() {
        testOut = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(testOut);
        System.setOut(printStream);
        System.setErr(printStream);
    }

    private void provideInput(String data) {
        testIn = new ByteArrayInputStream(data.getBytes());
        System.setIn(testIn);
    }

    private String getOutput() {
        return testOut.toString();
    }

    @AfterEach
    void restoreSystemInputOutput() {
        System.setIn(systemIn);
        System.setOut(systemOut);
        System.setErr(systemErr);
    }

    @Test
    void test1() throws Exception {
        String input = "var a = 1;\r\nprint a;\r\n";
        String expectedOutput = "> > 1\r\n> ";

        provideInput(input);
        Lox.main(new String[]{});
        assertEquals(expectedOutput, getOutput());
    }

    @Test
    void test2() throws Exception {
        String input = "print 1;\r\nprint a;\r\n";
        String expectedOutput = "> > 1\r\n ";

        provideInput(input);
        Lox.main(new String[]{});
        assertEquals(expectedOutput, getOutput());
    }
}
