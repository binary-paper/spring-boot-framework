package net.binarypaper.springbootframework;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

/**
 * Main class to bootstrap Spring Boot application
 *
 * @author <a href="mailto:willy.gadney@binarypaper.net">Willy Gadney</a>
 */
// Spring annotations
@SpringBootApplication
@EnableJms
public class Application {

    /**
     * The main method to start the Spring Boot application
     *
     * @param args Command line arguments with witch to start Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
