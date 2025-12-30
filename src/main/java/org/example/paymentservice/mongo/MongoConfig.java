package org.example.paymentservice.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import org.bson.UuidRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

//@Configuration
//public class MongoConfig extends AbstractMongoClientConfiguration {
//
//    @Value("${spring.data.mongodb.uri}")
//    private String mongoUri;
//
//    @Override
//    protected String getDatabaseName() {
//        return "payment_db";
//    }
//
//    @Override
//    public MongoClientSettings mongoClientSettings() {
//        return MongoClientSettings.builder()
//                .applyConnectionString(new ConnectionString(mongoUri))
//                .uuidRepresentation(UuidRepresentation.STANDARD)
//                .build();
//    }
//}