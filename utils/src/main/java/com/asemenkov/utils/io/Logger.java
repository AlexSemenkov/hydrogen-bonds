package com.asemenkov.utils.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalTime;

/**
 * @author asemenkov
 * @since Feb 4, 2018
 */
public class Logger {

    public static void log(Object message) {
        log(message.toString(), "INFO");
    }

    public static void logGmxOutput(Process process) {
        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
        try {
            for (String line = input.readLine(); line != null; line = input.readLine()) //
                System.out.println("<<< " + line);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void warn(Object message) {
        log(message.toString(), "WARNING");
    }

    public static void error(Object message) {
        log(message.toString(), "ERROR");
    }

    private static void log(String message, String infoOrWarning) {
        Exception e = new Exception();
        System.out.println(new StringBuilder(LocalTime.now().toString()) //
                .append(" [") //
                .append(Thread.currentThread().getName()).append("] [") //
                .append(infoOrWarning) //
                .append("] ") //
                .append(e.getStackTrace()[2].getClassName().replaceAll("\\w+\\.", ""))//
                .append(":") //
                .append(e.getStackTrace()[2].getLineNumber()) //
                .append(" - ") //
                .append(message));
    }
}
