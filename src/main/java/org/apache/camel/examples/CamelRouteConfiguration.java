/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
      .filter(simple("${body} contains 'error'"))
        .throwException(new java.lang.Exception())
    ;
    
    from("jms:queue:DLQ?connectionFactory=#nonXaJmsConnectionFactory")
      .routeId("deadMessageProcessorRoute")
      .log("DLQ Message: [${body}]")
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
