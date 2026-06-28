package com.example.demo.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponseDTO {
    public Integer orderId;
    public String receiverName;
    public String receiverPhone;
    public String receiverAddress;
    public String note;
    public BigDecimal shippingFee;
    public BigDecimal totalAmount;
    public String statusName; // Chỉ lấy tên trạng thái
    public LocalDateTime createdAt;
    public List<OrderDetailDTO> orderDetails; // Dùng DTO con ở đây
    public Integer statusId;
    public String customerName;
}