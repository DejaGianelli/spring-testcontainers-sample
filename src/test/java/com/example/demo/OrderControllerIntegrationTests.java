package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import(IntegrationTestsConfiguration.class)
@AutoConfigureWebTestClient
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/sql/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class OrderControllerIntegrationTests {

    @Autowired
    WebTestClient client;

    @Autowired
    OrderPlacementService orderPlacementService;

    @Autowired
    OrderRepository orderRepository;


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
        final Order.Builder order = OrderDataBuilder.create();
        Order created = orderPlacementService.place(order);

        // Act, Assert
        client.get().uri("/orders/1")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(OrderResponse.class)
                .consumeWith(result -> {
                    assertEquals(created.getId(), result.getResponseBody().getId());
                    assertEquals(created.getAmount(), result.getResponseBody().getAmount());
                    assertEquals(created.getCustomerId(), result.getResponseBody().getCustomerId());
                    assertEquals(created.getStatus(), result.getResponseBody().getStatus());
                    assertNotNull(result.getResponseBody());
                });
    }

    @Test
    void return_201_when_order_is_placed_successfully() {

        // Arrange
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setAmount(100.00);
        request.setCustomerId(2);

        // Act, Assert
        client.post().uri("/orders")
                .bodyValue(request)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON);

        long total = orderRepository.count();
        var createdOrder = orderRepository.findById(1L).orElseThrow();

        assertEquals(1L, total);
        assertEquals(1L, createdOrder.getId());
        assertEquals(OrderStatus.PENDING, createdOrder.getStatus());
        assertEquals(2, createdOrder.getCustomerId());
        assertEquals(100.00, createdOrder.getAmount());
    }
}