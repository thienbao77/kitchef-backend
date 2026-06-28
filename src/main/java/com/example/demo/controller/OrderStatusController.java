package com.example.demo.controller;

import com.example.demo.entity.OrderStatuses;
import com.example.demo.repository.OrderStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/order-statuses")
public class OrderStatusController {
    @Autowired
    private OrderStatusRepository repository;

    @GetMapping
    public List<OrderStatuses> getAll() {
        return repository.findAll();
    }
}