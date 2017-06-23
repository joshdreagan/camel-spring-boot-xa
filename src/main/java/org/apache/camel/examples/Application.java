package org.apache.camel.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

  public static void main(String[] args) {
    System.setProperty("com.arjuna.ats.jta.xaRecoveryNode", "1");
    SpringApplication.run(Application.class, args);
  }
}
