package com.ailleron.camel;

import org.apache.camel.spring.boot.CamelSpringBootApplicationController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class CamelApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(CamelApplication.class, args);
        CamelSpringBootApplicationController controller = 
                context.getBean(CamelSpringBootApplicationController.class);
        controller.run();
    }

}
