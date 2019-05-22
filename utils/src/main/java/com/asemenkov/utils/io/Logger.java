package com.asemenkov.utils.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

/**
 * @author asemenkov
 * @since Feb 4, 2018
 */
public class Logger {

    public static void log(Object message) {
        log(message.toString(), "INFO");
    }

    public static void logGmxOutput(Process process) {
        InputStream error = process.getErrorStream();
        InputStream input = process.getInputStream();
        new Thread(getReaderForStream(error, "[gmx-info] <<< ")).start();
        new Thread(getReaderForStream(input, "[gmx-error] <<< ")).start();
        try {
            process.waitFor();
        } catch (InterruptedException exception) {
            throw new RuntimeException(exception);
        }
    }

    public static void warn(Object message) {
        log(message.toString(), "WARNING");
    }

    public static void error(Object message) {
        log(message.toString(), "ERROR");
    }

    // ======== SUPPORT METHODS ========

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

    private static Runnable getReaderForStream(InputStream inputStream, String prefix) {
        return () -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    System.out.println(prefix);
                    System.out.print(line);
                    TimeUnit.MILLISECONDS.sleep(1);
                }
                inputStream.close();
            } catch (IOException | InterruptedException exception) {
                throw new RuntimeException(exception);
            }
        };
    }

}
