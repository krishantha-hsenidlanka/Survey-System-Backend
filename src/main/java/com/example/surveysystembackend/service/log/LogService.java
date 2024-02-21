package com.example.surveysystembackend.service.log;


import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

@Service
public class LogService {

    private final ByteArrayOutputStream consoleOutput = new ByteArrayOutputStream();
    private final PrintStream customPrintStream = new PrintStream(consoleOutput);

    public LogService() {
        System.setOut(customPrintStream);
        System.setErr(customPrintStream);
    }

    public String getConsoleLogs() {
        return consoleOutput.toString();
    }

    public void resetCustomPrintStream() {
        consoleOutput.reset();
    }
}
