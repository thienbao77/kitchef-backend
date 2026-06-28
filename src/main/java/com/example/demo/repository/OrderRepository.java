package com.example.demo.repository;

import com.example.demo.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    @Modifying
    @Query("DELETE FROM Order o WHERE o.createdAt < :date AND (o.orderStatus.statusId = 4 OR o.orderStatus.statusId = 6)")
    void deleteOldOrders(@Param("date") LocalDateTime date);
}
