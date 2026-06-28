package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Reviews",
        uniqueConstraints = {
                @UniqueConstraint(name = "UQ_Review", columnNames = {"customer_id", "product_id", "order_id"})
        })
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Integer reviewId;

    @Column(name = "rating_stars", nullable = false)
    private Integer ratingStars;

    @Column(name = "comment", columnDefinition = "NVARCHAR(MAX)")
    private String comment;

    @Column(name = "is_approved")
    private Boolean isApproved = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Nhiều đánh giá thuộc về một khách hàng (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    // Nhiều đánh giá có thể trỏ chung về một Sản phẩm
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Mỗi đánh giá phải gắn liền với một Đơn hàng cụ thể để xác thực đã mua hàng
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}