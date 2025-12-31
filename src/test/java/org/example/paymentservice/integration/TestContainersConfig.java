package org.example.paymentservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.paymentservice.security.JwtFilter;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestContainersConfig {
        @Bean
        @ServiceConnection
        public MongoDBContainer mongoDBContainer() {
            return new MongoDBContainer("mongo:6.0");
        }

        @Bean
        @ServiceConnection
        public KafkaContainer kafkaContainer() {
            return new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));
        }


}