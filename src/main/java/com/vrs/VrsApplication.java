package com.vrs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class VrsApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(VrsApplication.class, args);
        String port = context.getEnvironment().getProperty("server.port", "8081");
        System.out.println("=======================================================");
        System.out.println("Vehicle Registration System is running!");
        System.out.println("Open your browser at: http://localhost:" + port);
        System.out.println("=======================================================");
    }
}
