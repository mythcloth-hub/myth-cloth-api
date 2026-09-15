package com.mesofi.mythclothapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the MythCloth API application.
 *
 * <p>
 * This class is annotated with {@link SpringBootApplication}, which enables
 * component scanning, autoconfiguration, and property support. It serves as the
 * starting point for the Spring Boot application.
 * </p>
 */
@SpringBootApplication
public class Application {

    /**
     * Main method to launch the MythCloth API application.
     *
     * <p>
     * This method invokes {@link SpringApplication#run(Class, String...)} to start
     * the Spring Boot application, passing in the {@code Application} class and
     * command-line arguments.
     * </p>
     *
     * @param args
     *            command-line arguments passed to the application
     */
    static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
