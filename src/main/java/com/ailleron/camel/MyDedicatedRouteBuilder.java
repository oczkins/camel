/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ailleron.camel;

import org.apache.camel.ExchangePattern;
import org.apache.camel.LoggingLevel;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.stereotype.Component;

/**
 *
 * @author Wojciech Oczkowski
 */
@Component
public class MyDedicatedRouteBuilder extends SpringRouteBuilder {

    @Override
    public void configure() throws Exception {
        from("direct:callJsonTest")
                .streamCaching()
                .to("log:fromJetty?showAll=true")
                .setBody(
                        simple("{\"requestDate\":\"${date:now:yyyyMMdd}\"}"))
                .to("bean:myProcessor?method=myProcess")
                .multicast()
                   .to("http://tojestzlyurl/")
                   .inOnly("seda:callDate")
                .end()
                .log(LoggingLevel.ERROR, "logger.error", "panic! ${body}");
        
                from("seda:callDate")
                .removeHeaders("*")
                .to("http://date.jsontest.com/")
                        .log("afterdate");
                        
                
    }

}
