package com.example.surveysystembackend.service.log;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

@Service
public class LogService {

    private final ByteArrayOutputStream consoleOutput = new ByteArrayOutputStream();
    private final PrintStream customPrintStream = new PrintStream(consoleOutput);
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    public LogService() {
        // Print logs to the original console before redirecting
        originalOut.println("Redirecting console output to customPrintStream");
        originalErr.println("Redirecting console error to customPrintStream");

        // Redirect standard output and standard error to customPrintStream
        System.setOut(new CombinedPrintStream(originalOut, customPrintStream));
        System.setErr(new CombinedPrintStream(originalErr, customPrintStream));
    }

    public String getConsoleLogs() {
        return consoleOutput.toString();
    }

    public void resetCustomPrintStream() {
        // Reset the ByteArrayOutputStream
        consoleOutput.reset();
    }

    // Restore the original standard output and standard error streams
    public void restoreOriginalStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    // A custom PrintStream that writes to multiple streams
    private static class CombinedPrintStream extends PrintStream {
        private final PrintStream second;

        public CombinedPrintStream(PrintStream first, PrintStream second) {
            super(first);
            this.second = second;
        }

        @Override
        public void write(byte[] buf, int off, int len) {
            super.write(buf, off, len);
            second.write(buf, off, len);
        }

        @Override
        public void write(int b) {
            super.write(b);
            second.write(b);
        }
    }
}
