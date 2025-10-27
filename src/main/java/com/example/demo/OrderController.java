package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    OrderRepository orderRepository;

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
