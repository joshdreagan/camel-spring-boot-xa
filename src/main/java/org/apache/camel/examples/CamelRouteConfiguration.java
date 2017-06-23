package org.apache.camel.examples;

import java.util.UUID;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spring.spi.SpringTransactionPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
public class CamelRouteConfiguration extends RouteBuilder {

  @Override
  public void configure() {
    from("file:{{application.file.messagesDirectory}}")
      .routeId("fileConsumerRoute")
      .convertBodyTo(String.class)
      .to("jms:queue:{{application.jms.messagesQueue}}?connectionFactory=#nonXaJmsConnectionFactory")
    ;
    
    from("jms:queue:{{application.jms.messagesQueue}}?connectionFactory=#xaJmsConnectionFactory&transacted=true&transactionManager=#transactionManager")
      .routeId("messageProcessorRoute")
      .transacted("requiredTransactionPolicy")
      .to("sql:insert into {{application.sql.messagesTable}} values (:#${ref:uuid},:#${body},:#${date:now})?dataSource=#dataSource")
      //.throwException(new java.lang.Exception())
    ;
  }
  
  @Bean
  SpringTransactionPolicy requiredTransactionPolicy(PlatformTransactionManager transactionManager) {
    SpringTransactionPolicy policy = new SpringTransactionPolicy(transactionManager);
    policy.setPropagationBehaviorName("PROPAGATION_REQUIRED");
    return policy;
  }
  
  @Bean
  @Scope("prototype")
  String uuid() {
    return UUID.randomUUID().toString();
  }
}
