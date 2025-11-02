package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OrderPlacementService {

    private final OrderRepository orderRepository;

    @Autowired
    public OrderPlacementService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public @NonNull Order place(@NonNull Order.Builder builder) {
        builder.status(OrderStatus.PENDING).createdAt(LocalDateTime.now());
        Order order = builder.build();
        return orderRepository.save(order);
    }
}
