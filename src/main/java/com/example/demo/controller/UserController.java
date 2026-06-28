package com.example.demo.controller;

import com.example.demo.DTO.UserRequestDTO;
import com.example.demo.DTO.UserResponseDTO;
import com.example.demo.entity.Roles;
import com.example.demo.entity.User;
import com.example.demo.entity.UserAddress;
import com.example.demo.repository.RolesRepository;
import com.example.demo.repository.UserAddressRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RolesRepository rolesRepository;

    @Autowired
    private UserAddressRepository userAddressRepository;

    // --- 1. LẤY TẤT CẢ (Read) ---
    @GetMapping("/all")
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll().stream().map(user -> {
            UserResponseDTO dto = new UserResponseDTO();
            dto.userId = user.getUserId();
            dto.fullName = user.getFullName();
            dto.email = user.getEmail();
            dto.phoneNumber = user.getPhoneNumber();
            dto.roles = user.getRoles().stream().map(Roles::getRoleName).collect(Collectors.toSet());
            dto.isActive = user.getIsActive();
            // Trong UserController.java - hàm getAllUsers
            dto.password = user.getPassword_hash(); // Gán mật khẩu vào DTO để trả về

            return dto;
        }).collect(Collectors.toList()));
    }

    // --- 2. THÊM MỚI (Create) ---
// --- 2. THÊM MỚI (Create) ---
    @PostMapping("/add")
    @Transactional
    public ResponseEntity<?> createUser(@RequestBody UserRequestDTO dto) {
        if (userRepository.existsByEmail(dto.email)) {
            return ResponseEntity.badRequest().body("Email đã tồn tại!");
        }
        User user = new User();

        user.setFullName(dto.fullName);
        user.setEmail(dto.email);
        user.setPhoneNumber(dto.phoneNumber);
        user.setPassword_hash(dto.password);
        user.setIsActive(dto.isActive);

        // Gán quyền mặc định là ROLE_USER (giả sử role_id = 1 là ROLE_USER)
        Roles defaultRole = rolesRepository.findByRoleName("ROLE_USER").orElseThrow();
        user.setRoles(Set.of(defaultRole)); // Dùng Set.of tạo set chỉ có 1 phần tử

        userRepository.save(user);
        return ResponseEntity.ok("Tạo user thành công");
    }

    // --- 3. CẬP NHẬT (Update) ---
    @PutMapping("/{userId}")
    @Transactional
    public ResponseEntity<?> updateUser(@PathVariable Integer userId, @RequestBody UserRequestDTO dto) {
        System.out.println("DEBUG - Trạng thái nhận được từ Frontend: " + dto.isActive);
        try {
            User user = userRepository.findById(userId).orElseThrow();
            user.setFullName(dto.fullName);
            user.setPhoneNumber(dto.phoneNumber);
            user.setEmail(dto.email);
            user.setIsActive(dto.isActive != null ? dto.isActive : user.getIsActive());
            userRepository.save(user);

            // Cập nhật địa chỉ an toàn hơn
            List<UserAddress> addresses = user.getAddresses();
            if (addresses != null && !addresses.isEmpty()) {
                UserAddress addr = addresses.get(0);
                addr.setAddressLine(dto.address);
                userAddressRepository.save(addr);
            } else if (dto.address != null && !dto.address.isEmpty()) {
                UserAddress newAddr = new UserAddress();
                newAddr.setUser(user);
                newAddr.setAddressLine(dto.address);
                newAddr.setIsDefault(true);
                userAddressRepository.save(newAddr);
            }
            return ResponseEntity.ok("Cập nhật thành công");
        } catch (Exception e) {
            e.printStackTrace(); // IN LỖI RA CONSOLE
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    // --- 4. XÓA (Delete) ---
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer userId) {
        userRepository.deleteById(userId);
        return ResponseEntity.ok("Xóa người dùng thành công");
    }

    // --- 5. PHÂN QUYỀN (Assign Role) ---
    @PutMapping("/{userId}/assign-role")
    @Transactional
    public ResponseEntity<?> assignRole(@PathVariable Integer userId, @RequestParam String roleName) {
        User user = userRepository.findById(userId).orElseThrow();
        Roles role = rolesRepository.findByRoleName(roleName.toUpperCase()).orElseThrow();

        // Xóa hết quyền cũ trước khi gán quyền mới
        user.getRoles().clear();
        user.getRoles().add(role);

        userRepository.save(user);
        return ResponseEntity.ok("Đã cập nhật vai trò duy nhất");
    }

    // Trong UserController.java hoặc AuthController.java
// Trong AuthController.java
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserRequestDTO loginRequest) {
        User user = userRepository.findByEmail(loginRequest.email).orElse(null);

        // Log ra log của server để xem dữ liệu đang so sánh là gì
        System.out.println("Input: " + loginRequest.email + " / " + loginRequest.password);
        if (user != null) {
            System.out.println("DB: " + user.getEmail() + " / " + user.getPassword_hash());
        }

        if (user != null && user.getPassword_hash().equals(loginRequest.password)) {
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.status(401).body("Sai thông tin");
    }
}
