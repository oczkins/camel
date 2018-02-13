/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ailleron.camel;

import org.apache.activemq.broker.BrokerService;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.spring.SpringRouteBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 * @author Wojciech Oczkowski
 */
@Configuration
public class CamelConfiguration {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public BrokerService broker() throws Exception{
        BrokerService brokerService = new BrokerService();
       
        brokerService.setPersistent(true);
        brokerService.addConnector("vm://localhost");
        brokerService.start();
        return brokerService;
    }
    @Bean
    public RouteBuilder routes() {
        return new SpringRouteBuilder() {
            @Override
            public void configure() throws Exception {

                from("jetty:http://0.0.0.0:8181/routeStart")
                        .to("log:fromJetty?showAll=true")
                        .to("direct:callJsonTest")
                        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant("404"));

                from("jetty:http://0.0.0.0:8181/routeStart2")
                        .to("direct:callJsonTest");
                restConfiguration("jetty").host("0.0.0.0").port(18181)
                        .dataFormatProperty("prettyPrint", "true").
                        apiContextPath("/api-doc").
                        apiProperty("api.title", "Orders API").
                        apiProperty("api.version", "1.0.0").
                        apiProperty("cors", "true");
;

                from("direct:getOrders").to("log:getOrders");
                from("direct:addOrder").to("log:addOrder");
                from("direct:getOrderById").to("log:getOrderById?showAll=true");

                rest("/api")
                        .get("/orders").to("direct:getOrders")
                        .post("/orders").to("direct:addOrder")
                        .get("/orders/{id}").to("direct:getOrderById") ;

            }
        };
    }
}
