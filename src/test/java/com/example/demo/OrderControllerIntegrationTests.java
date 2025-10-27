package com.example.demo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

@Import(TestcontainersConfiguration.class)
@AutoConfigureWebTestClient
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerIntegrationTests {

    @Autowired
    WebTestClient client;

    @Autowired
    OrderCreationService orderCreationService;

    @Test
    void return_404_when_no_order_found() {
        // Act, Assert
        client.get().uri("/orders/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);
    }

    @Test
    void return_200_when_order_is_found() {
        // Arrange
        final Order order = OrderDataBuilder.create();
        orderCreationService.create(order);

        // Act, Assert
        client.get().uri("/orders/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(OrderResponse.class)
                .consumeWith(result -> {
                    Assertions.assertEquals(order.getId(), result.getResponseBody().getId());
                    Assertions.assertEquals(order.getAmount(), result.getResponseBody().getAmount());
                    Assertions.assertEquals(order.getCustomerId(), result.getResponseBody().getCustomerId());
                    Assertions.assertEquals(order.getStatus(), result.getResponseBody().getStatus());
                    Assertions.assertNotNull(result.getResponseBody());
                });
    }
}