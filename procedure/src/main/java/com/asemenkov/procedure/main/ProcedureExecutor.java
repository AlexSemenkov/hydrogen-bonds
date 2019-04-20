package com.asemenkov.procedure.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author asemenkov
 * @since Aug 31, 2018
 */
@SpringBootApplication(scanBasePackages = "com.asemenkov.gromacs")
public class ProcedureExecutor {


    private static void process(Process process) {
        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
        try {
            String line;
            while ((line = input.readLine()) != null) System.out.println(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Hello from gromacs-java");
        Process p = Runtime.getRuntime().exec("gmx -version");
        new Thread(() -> process(p)).start();
        SpringApplication.run(ProcedureExecutor.class, args);
    }
}
