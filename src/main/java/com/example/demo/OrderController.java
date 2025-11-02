package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/orders")

public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderPlacementService orderPlacementService;

    @Autowired
    public OrderController(OrderRepository orderRepository, OrderPlacementService orderPlacementService) {
        this.orderRepository = orderRepository;
        this.orderPlacementService = orderPlacementService;
    }

    @PostMapping
    public ResponseEntity<Void> place(@RequestBody PlaceOrderRequest request) {
        var orderBuilder = Order.builder().amount(request.getAmount()).customerId(request.getCustomerId());
        var order = orderPlacementService.place(orderBuilder);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        return ResponseEntity.status(201).headers(headers).build();
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<OrderResponse> details(@PathVariable Long id) {

        Optional<Order> optional = orderRepository.findById(id);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        if (optional.isEmpty()) {
            return ResponseEntity.notFound().headers(headers).build();
        }

        Order order = optional.get();

        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setAmount(order.getAmount());
        response.setCreatedAt(order.getCreatedAt());
        response.setStatus(order.getStatus());
        response.setCustomerId(order.getCustomerId());

        return ResponseEntity.ok().headers(headers).body(response);
    }
}
