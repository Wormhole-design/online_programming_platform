package com.szu.lox;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoxTest {
    private final InputStream systemIn = System.in;
    private final PrintStream systemOut = System.out;

    private ByteArrayInputStream testIn;
    private ByteArrayOutputStream testOut;

    @BeforeEach
    void setUpOutput() {
        testOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOut));
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
    }

    @Test
    void testSuccessfulExpressionEvaluation() throws Exception {
        String input = "print 1 + 2 * 3;\r\n";
        String expectedOutput = "> 7\r\n> ";

        provideInput(input);
        Lox.main(new String[]{});
        assertEquals(expectedOutput, getOutput());
    }

    @Test
    void testRuntimeError() throws Exception {
        String input = "print 10 / (5 - 5);\r\n";
        String expectedOutput = "> Infinity\r\n> ";

        provideInput(input);
        Lox.main(new String[]{});
        assertEquals(expectedOutput, getOutput());
    }

    @Test
    void testStatement() throws IOException {
        String Path = "src/main/resources/test/stmt.lox";
        String expectedOutput =
                "inner a\r\n" +
                "outer b\r\n" +
                "global c\r\n" +
                "outer a\r\n" +
                "outer b\r\n" +
                "global c\r\n" +
                "global a\r\n" +
                "global b\r\n" +
                "global c\r\n";
        Lox.main(new String[]{Path});
        assertEquals(expectedOutput, getOutput());
    }

    @Test
    void testIfElse() throws IOException {
        String Path = "src/main/resources/test/if-else.lox";
        String expectedOutput =
                "1\r\n" +
                "0\r\n";
        Lox.main(new String[]{Path});
        assertEquals(expectedOutput, getOutput());
    }

    @Test
    void testLogic() throws IOException {
        String Path = "src/main/resources/test/logic.lox";
        String expectedOutput =
                "hi\r\n" +
                "yes\r\n";
        Lox.main(new String[]{Path});
        assertEquals(expectedOutput, getOutput());
    }

    @Test
    void testLoop() throws IOException {
        String Path = "src/main/resources/test/loop.lox";
        String expectedOutput =
                "0\r\n" +
                "1\r\n" +
                "1\r\n" +
                "2\r\n" +
                "3\r\n" +
                "5\r\n" +
                "8\r\n" +
                "13\r\n" +
                "21\r\n" +
                "34\r\n" +
                "55\r\n" +
                "89\r\n" +
                "144\r\n" +
                "233\r\n" +
                "377\r\n" +
                "610\r\n" +
                "987\r\n" +
                "1597\r\n" +
                "2584\r\n" +
                "4181\r\n" +
                "6765\r\n";
        Lox.main(new String[]{Path});
        assertEquals(expectedOutput, getOutput());
    }

    @Test
    void testFun() throws IOException {
        String Path = "src/main/resources/test/fun.lox";
        String expectedOutput =
                "Hi, Dear Reader!\r\n" +
                "0\r\n" +
                "1\r\n" +
                "1\r\n" +
                "2\r\n" +
                "3\r\n" +
                "5\r\n" +
                "8\r\n" +
                "13\r\n" +
                "21\r\n" +
                "34\r\n" +
                "55\r\n" +
                "89\r\n" +
                "144\r\n" +
                "233\r\n" +
                "377\r\n" +
                "610\r\n" +
                "987\r\n" +
                "1597\r\n" +
                "2584\r\n" +
                "4181\r\n" +
                "1\r\n" +
                "2\r\n";
        Lox.main(new String[]{Path});
        assertEquals(expectedOutput, getOutput());
    }

    @Test
    void testClass() throws IOException {
        String Path = "src/main/resources/test/class.lox";
        String expectedOutput =
                "DevonshireCream\r\n" +
                "Bagel instance\r\n" +
                "Crunch crunch crunch!\r\n" +
                "The German chocolate cake is delicious!\r\n";
        Lox.main(new String[]{Path});
        assertEquals(expectedOutput, getOutput());
    }
}
