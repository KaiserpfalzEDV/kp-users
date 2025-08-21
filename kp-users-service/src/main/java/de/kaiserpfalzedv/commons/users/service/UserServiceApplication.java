package de.kaiserpfalzedv.commons.users.service;


import de.kaiserpfalzedv.commons.users.client.EnableUserClient;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The spring boot application.
 *
 * @author klenkes74 {@literal <rlichti@kaiserpfalz-edv.de>}
 * @since 2025-06-21
 */
@SpringBootApplication
@EnableUserClient
public class UserServiceApplication {
  /**
   * The main method to start the application.
   *
   * @param args The command line arguments.
   */
  public static void main(String[] args) {
    org.springframework.boot.SpringApplication.run(UserServiceApplication.class, args);
  }
}
