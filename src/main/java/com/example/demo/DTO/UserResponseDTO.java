package com.example.demo.DTO;

import java.util.Set;

public class UserResponseDTO {
    public Integer userId;
    public String fullName;
    public String email;
    public String phoneNumber;
    public Set<String> roles; // Chỉ chứa tên quyền (ví dụ: ["ROLE_ADMIN"])
    public Boolean isActive;
    // Trong UserResponseDTO.java
    public String password; // Thêm dòng này để chứa mật khẩu
}