package com.ailleron.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext
public class CamelApplicationTests extends CamelTestSupport {

    @Autowired
    private CamelContext camelContext;

    @EndpointInject(uri = "direct:splitAndAggregate")
    private ProducerTemplate producerTemplate;
    
    
    @Override
    protected CamelContext createCamelContext() throws Exception {
        return camelContext;
    }

    @Override
    public String isMockEndpoints() {
        return "log:*";
    }

    @Override
    public String isMockEndpointsAndSkip() {
        return "activemq:*";
    }

//    @Test
//    public void testProvisioning() {
//        assertNotNull(camelContext);
//    }

    @Test
    public void testSplitAndAggregate() throws Exception {
        camelContext.getRouteDefinition("splitAndAggregate")
                .adviceWith(camelContext, new AdviceWithRouteBuilder() {
                    @Override
                    public void configure() throws Exception {
                        replaceFromWith("direct:splitAndAggregate");
                    }
                });
        MockEndpoint mockEndpoint = getMockEndpoint("mock:log:csvOrder");
        mockEndpoint.expectedMessageCount(3);
        producerTemplate.sendBody("1|2\n1|2\n1|2\n1|2\n");
        mockEndpoint.assertIsSatisfied();
    }

}
