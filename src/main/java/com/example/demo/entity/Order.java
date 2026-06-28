package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer orderId;

    // Nhiều đơn hàng có chung một trạng thái (Ví dụ: Chờ duyệt, Đã giao...)
    // (Lưu ý: Bạn cần tạo thêm Entity OrderStatus tương ứng bảng OrderStatuses nếu muốn map object)
//    @Column(name = "status_id", nullable = false)
//    private Integer statusId;

    @Column(name = "receiver_name", nullable = false, length = 100)
    private String receiverName;

    @Column(name = "receiver_phone", nullable = false, length = 15)
    private String receiverPhone;

    @Column(name = "receiver_address", nullable = false, length = 255)
    private String receiverAddress;

    @Column(name = "shipping_fee", nullable = false, precision = 18, scale = 2)
    private BigDecimal shippingFee = new BigDecimal("25000.00");

    @Column(name = "discount_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    // Một đơn hàng có nhiều chi tiết sản phẩm (Cấu hình CASCADE xóa theo SQL)
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<OrderDetail> orderDetails;

    // Quan hệ 1-1 với thông tin giao dịch thanh toán
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Payment payment;

    // Nhiều đơn hàng thuộc về một khách hàng (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"orders", "orderDetails", "cart"}) // Thêm "cart" nếu User có quan hệ với Cart
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    // Nhiều đơn hàng có thể được xử lý bởi một nhân viên (User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"orders", "orderDetails"})
    @JoinColumn(name = "staff_id")
    private User staff;

    // Nhiều đơn hàng có thể áp dụng cùng một mã Voucher
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"orders", "orderDetails"})
    @JoinColumn(name = "voucher_id")
    private Vouchers voucher;

    // Danh sách các review xuất phát từ đơn hàng này
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Review> reviews;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"orders", "orderDetails"})
    @JoinColumn(name = "status_id")
    private OrderStatuses orderStatus;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
