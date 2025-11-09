package com.example.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.io.UncheckedIOException;

@Component
public class OrderPlacementListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderPlacementListener.class);

    private final ObjectMapper objectMapper;
    private final OrderPlacementService orderPlacementService;

    @Autowired
    public OrderPlacementListener(ObjectMapper objectMapper, OrderPlacementService orderPlacementService) {
        this.objectMapper = objectMapper;
        this.orderPlacementService = orderPlacementService;
    }

    @SqsListener(value = "${queues.order-placement}")
    public void listen(Message<String> message) {

        PlaceOrderRequest request = getPlaceOrderRequest(message);

        var orderBuilder = Order.builder().amount(request.getAmount()).customerId(request.getCustomerId());
        var order = orderPlacementService.place(orderBuilder);

        logger.info("Successfully place new Order {}", order.getId());
    }

    private PlaceOrderRequest getPlaceOrderRequest(Message<String> message) {
        try {
            return objectMapper.readValue(message.getPayload(), PlaceOrderRequest.class);
        } catch (JsonProcessingException e) {
            throw new UncheckedIOException(e);
        }
    }
}
