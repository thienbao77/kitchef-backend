package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "OrderDetails")
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_detail_id")
    private Integer orderDetailId;

    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price", nullable = false, precision = 18, scale = 2)
    private BigDecimal price;


    // Nhiều chi tiết thuộc về một Đơn hàng chính
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore // Dòng này cực kỳ quan trọng để cắt vòng lặp
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    // Nhiều chi tiết hóa đơn của các khách khác nhau có thể mua cùng một Product
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"cartDetails", "orderDetails", "reviews"}) // Bỏ qua các list con của Product
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
}
