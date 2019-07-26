package com.eappcat.sync.es;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan
@EnableJpaRepositories(basePackages = "com.eappcat.sync.es.dao.jpa")
@EnableScheduling
@EnableConfigurationProperties(SyncConfigProperties.class)
@EnableElasticsearchRepositories(basePackages = "com.eappcat.sync.es.dao.es")
@EnableJpaAuditing
public class MysqlEsApplication {
    public static void main(String[] args) {
        SpringApplication.run(MysqlEsApplication.class, args);
    }
}
