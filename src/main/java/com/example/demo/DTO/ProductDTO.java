package com.example.demo.DTO;

import java.math.BigDecimal;
import java.util.List;

public class ProductDTO {
    public String productName;
    public String slug;
    public BigDecimal price;
    public Integer stockQuantity;
    public String description;
    public String imageUrl; // Ảnh đại diện
    public Integer categoryId;
    public Boolean isActive;
    public List<String> extraImageUrls; // Danh sách các đường dẫn ảnh phụ
}
