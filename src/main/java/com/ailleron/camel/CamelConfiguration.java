/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ailleron.camel;

import java.io.IOException;
import javax.management.RuntimeErrorException;
import org.apache.activemq.broker.BrokerService;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.builder.xml.Namespaces;
import org.apache.camel.model.dataformat.CsvDataFormat;
import org.apache.camel.model.dataformat.JsonDataFormat;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.apache.camel.processor.idempotent.MemoryIdempotentRepository;
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
    public BrokerService broker() throws Exception {
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
               
                from("activemq:doSomethigRetry")
                        .to("direct:doSomething");
                
                from("jetty:http://0.0.0.0:8181/error")
                        .to("log:processing")
                        .to("direct:doSomething");
      
                from("direct:doSomething")
                        .onException(IOException.class)
                            .to("log:ioerror")
                        .onException(Exception.class)
                            .to("log:error")
                 
                        .end()
                        
                        .doTry()
                            .throwException(new RuntimeException())
                        .doCatch(RuntimeErrorException.class)
                            .to("log:error")
                        .end()
                        
                        .to("log:costam");

                
                
                
                MemoryIdempotentRepository repo = new MemoryIdempotentRepository();
                
                from("file://D://input")
                        .split()
                        .xpath("//order")
                        .to("log:order?showAll=true&showStreams=true")
                        .end()
                        .to("log:fromDir?showAll=true&showStreams=true");

                from("file://D://inputcsv")
                        .split()
                        .tokenize("\n")
                        .to("log:fromDir?showAll=true&showStreams=true")
                        .aggregate(new MyAggregate())
                        .constant("1")
                        .completionSize(3)
                        .completionTimeout(1000)
                        .unmarshal(new CsvDataFormat("|"))
                        .to("log:csvOrder?showAll=true&showStreams=true")
                        .log("name: ${body[0][0]}")
                        .end()
                        .end();
                
                from("file://D://inputcsv2")
                        .split()
                            .tokenize("\n")
                            .unmarshal(new CsvDataFormat("|"))
                            .idempotentConsumer(simple("${body[0][0]}"),repo)
                                .skipDuplicate(false)
//                                .filter(exchangeProperty(Exchange.DUPLICATE_MESSAGE).not())
                                .choice()
                                    .when(exchangeProperty(Exchange.DUPLICATE_MESSAGE))
                                        .throwException(new RuntimeException("duplikat"))
                                    .otherwise()
                                        .to("log:csvOrder?showAll=true&showStreams=true")
                                        .log("name: ${body[0][0]}")
                                .endChoice()
                           .end()
                           
                        .end();

                Namespaces ns = new Namespaces("webx", "http://www.webserviceX.NET/")
                        .add("xsd", "http://www.w3.org/2001/XMLSchema");

                from("jetty:http://0.0.0.0:8181/callSoap")
                        .streamCaching()
                        .removeHeaders("*")
                        .setHeader("lengthValue").constant("1")
                        .to("velocity:vm/lengthRequest.vm")
                        .to("cxf:http://www.webservicex.net/length.asmx?wsdlURL=wsdl/service.wsdl&dataFormat=MESSAGE&portName=lengthUnitSoap")
                        .setHeader("soapResult", ns.xpath("//webx:ChangeLengthUnitResult", Integer.class))
                        .filter()
                        .simple("${headers.soapResult} > 1")
                        .to("log:afterSoap?showAll=true&showStreams=true")
                        .to("xslt:xslt/transform.xml")
                        .to("log:afterXSLT?showAll=true&showStreams=true");

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

                from("direct:addOrders")
                        .split()
                        .jsonpath("$..description")
                        .to("log:afterSplit?showAll=true");

                from("direct:addOrder")
                        .setHeader("quantity").jsonpath("$.quantity", Integer.class)
                        .filter().simple("${in.header.quantity} > 0")
                        .to("log:addOrderString?showAll=true&showStreams=true")
                        .unmarshal(new JsonDataFormat(JsonLibrary.Jackson))
                        .to("log:addOrderMap?showAll=true")
                        .setHeader("orderName", simple("${body['name']}"))
                        .to("log:addOrderMapHeader?showAll=true")
                        .to("velocity:vm/response.vm")
                        .end()
                        .to("log:addOrderMapVelocity?showAll=true");

                from("direct:getOrderById").to("log:getOrderById?showAll=true");

                rest("/api")
                        .get("/orders").to("direct:getOrders")
                        .post("/orders").to("direct:addOrders")
                        .get("/orders/{id}").to("direct:getOrderById");

            }
        };
    }

    private static class MyAggregate implements AggregationStrategy {

        @Override
        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
            if (oldExchange == null) {
                return newExchange;
            }
            String body1 = oldExchange.getIn().getBody(String.class);
            String body2 = newExchange.getIn().getBody(String.class);
            oldExchange.getIn().setBody(body1 + body2);
            return oldExchange;
        }

    }

}
