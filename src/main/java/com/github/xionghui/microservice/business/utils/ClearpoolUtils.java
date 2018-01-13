package com.github.xionghui.microservice.business.utils;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DatabaseDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.xionghuicoder.clearpool.core.ClearpoolDataSource;

@Configuration
public class ClearpoolUtils {

  @ConditionalOnClass(ClearpoolDataSource.class)
  @ConditionalOnProperty(name = "spring.datasource.type",
      havingValue = "com.github.xionghuicoder.clearpool.core.ClearpoolDataSource",
      matchIfMissing = true)
  private static class Clearpool {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.clearpool")
    public DataSource dataSource(DataSourceProperties properties) {
      ClearpoolDataSource dataSource = (ClearpoolDataSource) properties
          .initializeDataSourceBuilder().type(ClearpoolDataSource.class).build();
      String name = properties.getName();
      if (name != null) {
        dataSource.setName(name);
      }
      DatabaseDriver databaseDriver = DatabaseDriver.fromJdbcUrl(properties.determineUrl());
      String validationQuery = databaseDriver.getValidationQuery();
      if (validationQuery != null) {
        dataSource.setTestBeforeUse(true);
        dataSource.setTestQuerySql(validationQuery);
      }
      return dataSource;
    }
  }
}
