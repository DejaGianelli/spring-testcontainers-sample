package com.example.demo;

import com.example.demo.IntegrationTestsConfiguration.TestMessageInterceptor;
import io.awspring.cloud.sqs.operations.SendResult;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.localstack.LocalStackContainer;

@Import(IntegrationTestsConfiguration.class)
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

    @Autowired
    LocalStackContainer localStackContainer;

    @Autowired
    TestMessageInterceptor<Object> testMessageInterceptor;


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
