/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ailleron.camel;

import org.apache.camel.Body;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 *
 * @author Wojciech Oczkowski
 */
@Component
@Qualifier(value = "myProcessor")
public class MyProcessor {

    @EndpointInject(uri = "log:loggerZProcessora")
    ProducerTemplate producer;

    public void myProcess(Exchange exchange,  Message message, CamelContext context, @Body String body) throws Exception {
        producer.sendBody("MOje Body:" + body);
        String paramKey
                = context.resolvePropertyPlaceholders(
                        "{{param.key}}");
        String serviceURL
                = context.resolvePropertyPlaceholders(
                        "{{" + paramKey + "}}");
        message.setHeader(Exchange.HTTP_URI, serviceURL);
    }

}
