/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ailleron.camel;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Wojciech Oczkowski
 */
@Configuration
public class CamelConfiguration {


    @Bean
    public RouteBuilder routes(){
        return new SpringRouteBuilder() {
            @Override
            public void configure() throws Exception {
                
                from("timer:start")
                        
                        .setBody(simple("{\"requestDate\":\"${date:now:yyyyMMdd}\"}"))
                        .to("bean:myProcessor?method=myProcess")
                        .to("http://tojestzlyurl/")
                        .removeHeaders("*")
                        .to("http://date.jsontest.com/")
                        .log(LoggingLevel.ERROR, "logger.error", "panic! ${body}");
                
                
                
            }
        };
    }
}
