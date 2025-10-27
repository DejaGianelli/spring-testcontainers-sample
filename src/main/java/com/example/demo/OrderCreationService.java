package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@Service
public class OrderCreationService {

    OrderRepository orderRepository;

    @Autowired
    public OrderCreationService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public void create(@NonNull Order order) {
        orderRepository.save(order);
    }
}
