package com.asemenkov.utils;

import java.time.LocalTime;

/**
 * @author asemenkov
 * @since Feb 4, 2018
 */
public class Logger {

    public static void log(Object message) {
        log(message.toString(), "INFO");
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
