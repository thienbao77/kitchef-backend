package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String password_hash;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // --- ĐÂY LÀ PHẦN XỬ LÝ BẢNG TRUNG GIAN USERROLES ---
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "UserRoles", // Tên bảng trung gian trong SQL
            joinColumns = @JoinColumn(name = "user_id"), // Khóa ngoại liên kết tới bảng Users
            inverseJoinColumns = @JoinColumn(name = "role_id") // Khóa ngoại liên kết tới bảng Roles
    )
    private Set<Roles> roles;// Tập hợp các quyền của User này

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<UserAddress> addresses;

    // Thêm vào trong file User.java để kết nối đảo ngược 1-1 với giỏ hàng
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Cart cart;

    // Đơn hàng mà User này đã mua (Đóng vai trò Khách hàng)
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Order> boughtOrders;

    // Đơn hàng mà User này xử lý duyệt (Đóng vai trò Nhân viên)
    @OneToMany(mappedBy = "staff", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Order> managedOrders;

    // Danh sách các review mà User này đã viết
    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Review> reviews;

    // Danh sách các form liên hệ mà nhân viên này chịu trách nhiệm trả lời
    @OneToMany(mappedBy = "staff", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Contact> handledContacts;

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
