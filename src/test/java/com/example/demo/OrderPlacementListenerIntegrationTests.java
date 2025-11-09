package com.example.demo;

import com.example.demo.TestcontainersConfiguration.TestMessageInterceptor;
import io.awspring.cloud.sqs.operations.SendResult;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.localstack.LocalStackContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;

@Import(TestcontainersConfiguration.class)
@AutoConfigureWebTestClient
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class OrderPlacementListenerIntegrationTests {

    static final String QUEUE_NAME = "order-placement";

    @Autowired
    SqsTemplate sqsTemplate;

    @Autowired
    OrderRepository orderRepository;

    @Container
    static LocalStackContainer localstack = new LocalStackContainer("localstack/localstack:3.5.0")
            .withServices("sqs");

    @Autowired
    TestMessageInterceptor<Object> testMessageInterceptor;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.cloud.aws.sqs.endpoint", localstack::getEndpoint);
        registry.add("spring.cloud.aws.credentials.access-key", localstack::getAccessKey);
        registry.add("spring.cloud.aws.credentials.secret-key", localstack::getSecretKey);
        registry.add("spring.cloud.aws.region.static", localstack::getRegion);
    }

    @BeforeAll
    static void setupQueues() {
        try (SqsClient sqsClient = SqsClient.builder()
                .endpointOverride(localstack.getEndpoint())
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(localstack.getAccessKey(), localstack.getSecretKey())
                        )
                )
                .region(Region.of(localstack.getRegion()))
                .build()) {
            CreateQueueRequest createQueueRequest = CreateQueueRequest.builder()
                    .queueName(QUEUE_NAME)
                    .build();
            sqsClient.createQueue(createQueueRequest);
        }
    }

    @Test
    void should_place_order() {
        // Act
        SendResult<Object> sendResult = sqsTemplate.send(to -> to.queue(QUEUE_NAME)
                .payload("{\"amount\":999.99,\"customerId\":42}"));

        testMessageInterceptor.waitForMessageProcessed(sendResult.messageId().toString());

        // Assert
        Order order = orderRepository.findById(1L).orElseThrow();
        Assertions.assertEquals(1, order.getId());
        Assertions.assertEquals(999.99, order.getAmount());
        Assertions.assertEquals(42, order.getCustomerId());
        Assertions.assertEquals(OrderStatus.PENDING, order.getStatus());
        Assertions.assertEquals(1, orderRepository.count());
    }
}
