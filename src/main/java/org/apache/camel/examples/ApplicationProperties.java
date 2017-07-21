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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {

  private FileConfiguration file;
  private JmsConfiguration jms;
  private SqlConfiguration sql;

  public FileConfiguration getFile() {
    return file;
  }

  public void setFile(FileConfiguration file) {
    this.file = file;
  }

  public JmsConfiguration getJms() {
    return jms;
  }

  public void setJms(JmsConfiguration jms) {
    this.jms = jms;
  }

  public SqlConfiguration getSql() {
    return sql;
  }

  public void setSql(SqlConfiguration sql) {
    this.sql = sql;
  }
  
  public static class FileConfiguration {
    
    private String messagesDirectory;

    public String getMessagesDirectory() {
      return messagesDirectory;
    }

    public void setMessagesDirectory(String messagesDirectory) {
      this.messagesDirectory = messagesDirectory;
    }
  }
  
  public static class JmsConfiguration {
    
    private String messagesQueue;

    public String getMessagesQueue() {
      return messagesQueue;
    }

    public void setMessagesQueue(String messagesQueue) {
      this.messagesQueue = messagesQueue;
    }
  }
  
  public static class SqlConfiguration {
    
    private String messagesTable;

    public String getMessagesTable() {
      return messagesTable;
    }

    public void setMessagesTable(String messagesTable) {
      this.messagesTable = messagesTable;
    }
  }
}
