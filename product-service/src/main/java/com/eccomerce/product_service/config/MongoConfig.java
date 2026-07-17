package com.eccomerce.product_service.config;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.util.StringUtils;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.host}")
    private String host;

    @Value("${spring.data.mongodb.port}")
    private int port;

    @Value("${spring.data.mongodb.database}")
    private String database;

    @Value("${spring.data.mongodb.username}")
    private String username;

    @Value("${spring.data.mongodb.password}")
    private String password;

    @Value("${spring.data.mongodb.authentication-database}")
    private String authDatabase;

    @Override
    protected String getDatabaseName() {
        return database;
    }

    @Override
    @Bean
    public MongoClient mongoClient() {
        String connectionString = String.format("mongodb://%s:%d", host, port);

        MongoClientSettings.Builder settingsBuilder = MongoClientSettings.builder()
                .applyConnectionString(new com.mongodb.ConnectionString(connectionString));

        if (StringUtils.hasText(username)) {
            settingsBuilder.credential(MongoCredential.createCredential(
                    username,
                    authDatabase,
                    password != null ? password.toCharArray() : new char[0]
            ));
        }

        MongoClientSettings settings = settingsBuilder.build();

        return MongoClients.create(settings);
    }
}
