package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
// Đảm bảo tính duy nhất UNIQUE(cart_id, product_id)
@Table(name = "CartDetails",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"cart_id", "product_id"})})
public class CartDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_detail_id")
    private Integer cartDetailId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    // Nhiều chi tiết giỏ hàng thuộc về một giỏ hàng chính (ON DELETE CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    // Nhiều chi tiết giỏ hàng có thể chứa cùng một loại Product
    // Chú ý: Không dùng cascade gỡ bỏ ở đây vì SQL không đặt ON DELETE CASCADE cho product_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"cartDetails", "orderDetails", "reviews"})
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // Trỏ tới Entity Product bạn đã làm ở trước
}
