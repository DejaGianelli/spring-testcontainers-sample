package com.example.demo;

import java.time.LocalDateTime;

public class OrderDataBuilder {
    public static Order create() {
        var order = new Order();
        order.setAmount(100.00);
        order.setCustomerId(2);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }
}
