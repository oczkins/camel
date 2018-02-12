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
                
                from("jetty:http://0.0.0.0:8181/routeStart")
                        .to("log:fromJetty?showAll=true")
                        .to("direct:callJsonTest")
                        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("404"));
                
                from("jetty:http://0.0.0.0:8181/routeStart2")
                        .to("direct:callJsonTest");
                
            }
        };
    }
}
