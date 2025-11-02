package com.example.demo;

import java.time.LocalDateTime;

public class OrderDataBuilder {
    public static Order.Builder create() {
        return Order.builder()
                .amount(100.00)
                .customerId(2)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now());
    }
}
