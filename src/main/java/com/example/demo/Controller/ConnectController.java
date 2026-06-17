package com.example.demo.Controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class ConnectController {
    @GetMapping("/test")
    public String testConnect() {
        // Trả về một dòng chữ giả lập để thằng Vue hứng
        return "Kết nối thành công rực rỡ! Tổng đài Spring Boot đã nhận tín hiệu từ dự án Đồ Bếp!";
    }
}
