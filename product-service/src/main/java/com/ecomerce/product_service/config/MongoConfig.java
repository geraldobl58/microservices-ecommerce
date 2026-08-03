package com.ecomerce.product_service.config;

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

    @Value("${spring.data.mongodb.uri:#{null}}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database:product-db}")
    private String database;

    // Legado (fallback se URI não for fornecida)
    @Value("${spring.data.mongodb.host:#{null}}")
    private String host;

    @Value("${spring.data.mongodb.port:#{null}}")
    private Integer port;

    @Value("${spring.data.mongodb.username:#{null}}")
    private String username;

    @Value("${spring.data.mongodb.password:#{null}}")
    private String password;

    @Value("${spring.data.mongodb.authentication-database:#{null}}")
    private String authDatabase;

    @Override
    protected String getDatabaseName() {
        return database;
    }

    @Override
    @Bean
    public MongoClient mongoClient() {
        // 1. Se mongoUri foi fornecida, usar direto
        if (StringUtils.hasText(mongoUri)) {
            return MongoClients.create(new com.mongodb.ConnectionString(mongoUri));
        }

        // 2. Fallback: construir partir de propriedades legadas (host/port/...)
        if (host != null && port != null) {
            String connectionString = String.format("mongodb://%s:%d", host, port);

            MongoClientSettings.Builder settingsBuilder = MongoClientSettings.builder()
                    .applyConnectionString(new com.mongodb.ConnectionString(connectionString));

            if (StringUtils.hasText(username)) {
                settingsBuilder.credential(MongoCredential.createCredential(
                        username,
                        authDatabase != null ? authDatabase : "admin",
                        password != null ? password.toCharArray() : new char[0]
                ));
            }

            MongoClientSettings settings = settingsBuilder.build();
            return MongoClients.create(settings);
        }

        // 3. Se nada foi configurado, usar default local (desenvolvimento)
        String defaultUri = "mongodb://root:password@localhost:27017/product-db?authSource=admin";
        return MongoClients.create(new com.mongodb.ConnectionString(defaultUri));
    }
}
