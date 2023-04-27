package com.szu.platform.service.impl;

import com.szu.lox.Lox;
import com.szu.platform.service.ILoxService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

@Service
public class LoxServiceImpl implements ILoxService {

    private final PrintStream systemOut = System.out;
    private final PrintStream systemErr = System.err;

    @Override
    public synchronized String run(String code) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(result);
        System.setOut(printStream);
        System.setErr(printStream);

        try {
            Lox.runApi(code);
        } catch(IOException e){
            return e.toString();
        }

        System.setOut(systemOut);
        System.setErr(systemErr);

        return result.toString();
    }
}
