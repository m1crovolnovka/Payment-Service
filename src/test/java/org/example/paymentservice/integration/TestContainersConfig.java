package org.example.paymentservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.paymentservice.security.JwtFilter;
import org.springframework.boot.test.context.TestConfiguration;
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

    // Запускаем контейнеры статически, чтобы они жили всё время работы тестов
    public static final MongoDBContainer MONGO_CONTAINER;
    public static final KafkaContainer KAFKA_CONTAINER;

    static {
        MONGO_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:6.0"));

        // Используем замену для совместимости, как обсуждали ранее
        KAFKA_CONTAINER = new KafkaContainer(
                DockerImageName.parse("confluentinc/cp-kafka:7.4.0")
                        .asCompatibleSubstituteFor("apache/kafka")
        );

        MONGO_CONTAINER.start();
        KAFKA_CONTAINER.start();

        // Вручную прописываем системные свойства, чтобы Spring их подхватил
        System.setProperty("spring.data.mongodb.uri", MONGO_CONTAINER.getReplicaSetUrl());
        System.setProperty("spring.kafka.bootstrap-servers", KAFKA_CONTAINER.getBootstrapServers());
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // Регистрируем модули для работы с Java 8 датами (если нужно)
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        return mapper;
    }


}