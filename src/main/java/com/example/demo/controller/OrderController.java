package com.example.demo.controller;

import com.example.demo.DTO.OrderDTO;
import com.example.demo.DTO.OrderDetailDTO;
import com.example.demo.DTO.OrderResponseDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired private OrderDetailRepository orderDetailRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private CartDetailRepository cartDetailRepository;
    @Autowired private OrderStatusRepository statusRepository; // Bảng OrderStatuses
    @Autowired private ProductRepository productRepository;

    @PostMapping("/{userId}/checkout")
    @Transactional
    public ResponseEntity<?> checkout(@PathVariable Integer userId, @RequestBody OrderDTO dto) {
        try {
            System.out.println("DEBUG: Bắt đầu checkout cho UserID = " + userId);

            // 1. Lấy giỏ hàng
            Cart cart = cartRepository.findByCustomer_UserId(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng của user " + userId));

            System.out.println("DEBUG: Đã tìm thấy CartID = " + cart.getCartId());

            // 2. Lấy chi tiết giỏ hàng
            List<CartDetail> details = cartDetailRepository.findByCartWithAllDetails(cart);
            System.out.println("DEBUG: Số lượng sản phẩm = " + details.size());

            if (details.isEmpty()) {
                return ResponseEntity.badRequest().body("Giỏ hàng trống!");
            }

        // --- SỬA CHỖ NÀY: TỰ TÍNH TỔNG TIỀN ĐỂ ĐẢM BẢO CHÍNH XÁC ---
        BigDecimal subTotal = BigDecimal.ZERO;
        for (CartDetail cd : details) {
            // Giá sản phẩm * số lượng
            BigDecimal itemTotal = cd.getProduct().getPrice().multiply(BigDecimal.valueOf(cd.getQuantity()));
            subTotal = subTotal.add(itemTotal);
        }

        // Tổng tiền = Tạm tính + Phí ship (lấy từ dto)
        BigDecimal ship = (dto.shippingFee != null) ? dto.shippingFee : BigDecimal.valueOf(25000);
        BigDecimal totalAmount = subTotal.add(ship);
        // -----------------------------------------------------------

        // 2. Tạo đơn hàng mới
        Order order = new Order();
        order.setCustomer(cart.getCustomer());
        order.setOrderStatus(statusRepository.findById(1).orElseThrow());

        // Sử dụng giá trị đã tính toán ở trên
        order.setTotalAmount(totalAmount);
        order.setShippingFee(ship);

        order.setReceiverName(dto.receiverName);
        order.setReceiverPhone(dto.receiverPhone);
        order.setReceiverAddress(dto.receiverAddress);
        order.setNote(dto.note);

        Order savedOrder = orderRepository.save(order);

        // 3. Chuyển từ CartDetails sang OrderDetails
        for (CartDetail cd : details) {
            OrderDetail od = new OrderDetail();
            od.setOrder(savedOrder);
            od.setProduct(cd.getProduct());
            od.setProductName(cd.getProduct().getProductName());
            od.setQuantity(cd.getQuantity());
            od.setPrice(cd.getProduct().getPrice());
            orderDetailRepository.save(od);
        }

            cartDetailRepository.deleteByCart(cart); // Xóa sạch tất cả chi tiết thuộc giỏ hàng này
            cartDetailRepository.flush();
            System.out.println("DEBUG: Đã xóa xong giỏ hàng!");

            return ResponseEntity.ok("Đặt hàng thành công!");

        } catch (Exception e) {
            e.printStackTrace(); // IN RA LỖI CỤ THỂ VÀO TERMINAL
            return ResponseEntity.internalServerError().body("Lỗi: " + e.getMessage());
        }
    }
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Integer orderId) {
        return orderRepository.findById(orderId).map(order -> {
            OrderResponseDTO dto = new OrderResponseDTO();
            dto.orderId = order.getOrderId();
            dto.customerName = order.getCustomer().getFullName(); // Lấy tên từ User
            dto.receiverName = order.getReceiverName();
            dto.receiverPhone = order.getReceiverPhone();
            dto.receiverAddress = order.getReceiverAddress();
            dto.totalAmount = order.getTotalAmount();
            // Map orderDetails từ order.getOrderDetails() tương tự như getAllOrders
            return ResponseEntity.ok(dto);
        }).orElse(ResponseEntity.notFound().build());
    }

    // Lấy tất cả đơn hàng cho Admin
    @GetMapping("/all")
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        List<Order> orders = orderRepository.findAll();

        List<OrderResponseDTO> dtos = orders.stream().map(order -> {
            OrderResponseDTO dto = new OrderResponseDTO();
            dto.orderId = order.getOrderId();
            dto.receiverName = order.getReceiverName();
            dto.receiverPhone = order.getReceiverPhone();
            dto.receiverAddress = order.getReceiverAddress();
            dto.note = order.getNote();
            dto.shippingFee = order.getShippingFee();
            dto.totalAmount = order.getTotalAmount();
            dto.createdAt = order.getCreatedAt();
            dto.customerName = order.getCustomer().getFullName();

            // Map Status
            if(order.getOrderStatus() != null) dto.statusName = order.getOrderStatus().getStatusName();
            if(order.getOrderStatus() != null) {
                dto.statusId = order.getOrderStatus().getStatusId(); // Map ID status
                dto.statusName = order.getOrderStatus().getStatusName();
            }

            // Map OrderDetails sang OrderDetailDTO
            dto.orderDetails = order.getOrderDetails().stream().map(od -> {
                OrderDetailDTO detailDto = new OrderDetailDTO();
                detailDto.productId = od.getProduct().getProductId();
                detailDto.productName = od.getProductName();
                detailDto.quantity = od.getQuantity();
                detailDto.price = od.getPrice();
                return detailDto;
            }).toList();

            return dto;
        }).toList();

        return ResponseEntity.ok(dtos);
    }

    // Cập nhật trạng thái đơn hàng
// Trong OrderController.java

    @PutMapping("/update-status/{orderId}")
    @Transactional
    public ResponseEntity<?> updateStatus(@PathVariable Integer orderId, @RequestParam Integer statusId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        Integer oldStatusId = order.getOrderStatus().getStatusId();
        var newStatus = statusRepository.findById(statusId)
                .orElseThrow(() -> new RuntimeException("Trạng thái không hợp lệ"));

        // Logic xử lý tồn kho
        for (OrderDetail od : order.getOrderDetails()) {
            Product p = od.getProduct();

            // 1. Khi chuyển từ bất kỳ trạng thái nào sang "Đã xác nhận" (statusId = 2)
            // Lưu ý: Chỉ trừ kho nếu đơn hàng trước đó chưa từng trừ
            if (statusId == 2 && oldStatusId == 1) {
                if (p.getStockQuantity() < od.getQuantity()) {
                    return ResponseEntity.badRequest().body("Sản phẩm " + p.getProductName() + " không đủ hàng!");
                }
                p.setStockQuantity(p.getStockQuantity() - od.getQuantity());
            }

            // 2. Khi giao thất bại (5) hoặc Hủy (6) thì cộng lại kho
            else if ((statusId == 5 || statusId == 6) && oldStatusId != 5 && oldStatusId != 6) {
                p.setStockQuantity(p.getStockQuantity() + od.getQuantity());
            }

            productRepository.save(p);
        }

        order.setOrderStatus(newStatus);
        orderRepository.save(order);
        return ResponseEntity.ok("Cập nhật thành công");
    }

    @DeleteMapping("/delete/{orderId}")
    @Transactional
    public ResponseEntity<?> deleteOrder(@PathVariable Integer orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        // Xóa chi tiết đơn hàng trước (quan trọng vì khóa ngoại)
        orderDetailRepository.deleteAll(order.getOrderDetails());
        // Xóa đơn hàng
        orderRepository.delete(order);

        return ResponseEntity.ok("Đã xóa đơn hàng thành công");
    }
}
