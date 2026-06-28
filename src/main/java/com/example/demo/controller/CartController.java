package com.example.demo.controller;

import com.example.demo.DTO.CartDetailDTO;
import com.example.demo.entity.Cart;
import com.example.demo.entity.CartDetail;
import com.example.demo.entity.Product;
import com.example.demo.repository.CartDetailRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartRepository cartRepository;
    @Autowired private CartDetailRepository cartDetailRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private UserRepository userRepository;

    // 1. Lấy giỏ hàng của user
    @GetMapping("/{userId}")
    public ResponseEntity<Cart> getCartByUserId(@PathVariable Integer userId) {
        Cart cart = cartRepository.findByCustomer_UserId(userId)
                .orElseGet(() -> { // Nếu chưa có giỏ hàng, tự tạo mới
                    Cart newCart = new Cart();
                    newCart.setCustomer(userRepository.findById(userId).orElseThrow());
                    return cartRepository.save(newCart);
                });
        return ResponseEntity.ok(cart);
    }

    // 2. Thêm sản phẩm vào giỏ hàng
    @PostMapping("/{userId}/add")
    public ResponseEntity<?> addToCart(@PathVariable Integer userId, @RequestBody CartDetailDTO dto) {
        Cart cart = cartRepository.findByCustomer_UserId(userId).orElseThrow();
        Product product = productRepository.findById(dto.productId).orElseThrow();

        // Kiểm tra xem sản phẩm đã có trong giỏ chưa
        CartDetail detail = cartDetailRepository.findByCartAndProduct(cart, product)
                .orElse(new CartDetail());

        if (detail.getCartDetailId() == null) {
            detail.setCart(cart);
            detail.setProduct(product);
            detail.setQuantity(dto.quantity);
        } else {
            detail.setQuantity(detail.getQuantity() + dto.quantity);
        }

        cartDetailRepository.save(detail);
        return ResponseEntity.ok("Đã thêm vào giỏ");
    }

    // 3. Cập nhật số lượng
    @PutMapping("/update/{cartDetailId}")
    public ResponseEntity<?> updateQuantity(@PathVariable Integer cartDetailId, @RequestBody Integer quantity) {
        CartDetail detail = cartDetailRepository.findById(cartDetailId).orElseThrow();
        detail.setQuantity(quantity);
        cartDetailRepository.save(detail);
        return ResponseEntity.ok("Đã cập nhật số lượng");
    }

    // 4. Xóa sản phẩm khỏi giỏ hàng
    @DeleteMapping("/remove/{cartDetailId}")
    public ResponseEntity<?> removeFromCart(@PathVariable Integer cartDetailId) {
        cartDetailRepository.deleteById(cartDetailId);
        return ResponseEntity.ok("Đã xóa khỏi giỏ");
    }
}