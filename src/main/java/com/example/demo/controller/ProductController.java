package com.example.demo.controller;

import com.example.demo.DTO.ProductDTO;
import com.example.demo.entity.Categories;
import com.example.demo.entity.Product;
import com.example.demo.entity.ProductImage;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductImageRepository;
import com.example.demo.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController // 1. CHÚ Ý: Đổi từ @Controller thành @RestController để trả về JSON thay vì trang HTML
@RequestMapping("/api/products") // 2. Đặt chuẩn đường dẫn API là /api/...
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    // 1. Lấy danh sách tất cả sản phẩm (Vue.js gọi GET)
    // URL: GET http://localhost:8080/api/products
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> list = productRepository.findAll();
        return ResponseEntity.ok(list); // Trả về thẳng List dưới dạng chuỗi JSON
    }

    // 2. Lấy chi tiết 1 sản phẩm theo ID để hiển thị hoặc đưa vào form sửa bên Vue
    // URL: GET http://localhost:8080/api/products/1
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable("id") Integer id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            return ResponseEntity.ok(product.get());
        } else {
            return ResponseEntity.notFound().build(); // Trả về lỗi 404 nếu không tìm thấy
        }
    }

    // 3. Thêm mới sản phẩm (Vue.js gửi dữ liệu form dạng JSON qua POST body)
    // URL: POST http://localhost:8080/api/products
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody ProductDTO dto) {
        // 1. Tìm Category từ ID
        Categories category = categoryRepository.findById(dto.categoryId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục!"));

        // 2. Lưu sản phẩm chính
        Product product = new Product();
        product.setProductName(dto.productName);
        product.setSlug(dto.slug);
        product.setPrice(dto.price);
        product.setStockQuantity(dto.stockQuantity);
        product.setDescription(dto.description);
        product.setImageUrl(dto.imageUrl);
        product.setIsActive(dto.isActive);
        product.setCategory(category); // <--- BẮT BUỘC PHẢI CÓ DÒNG NÀY

        productRepository.save(product);

        // 3. Lưu ảnh phụ
        if (dto.extraImageUrls != null) {
            for (String url : dto.extraImageUrls) {
                ProductImage img = new ProductImage();
                img.setProduct(product);
                img.setImageUrl(url);
                productImageRepository.save(img);
            }
        }
        return ResponseEntity.ok(product);
    }

    // 4. Cập nhật sản phẩm (Vue.js gửi PATCH hoặc PUT kèm theo ID)
    // URL: PUT http://localhost:8080/api/products/1
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable("id") Integer id, @RequestBody ProductDTO dto) {
        System.out.println("DEBUG: Category ID nhận được từ Vue là: " + dto.categoryId);
        return productRepository.findById(id).map(product -> {
            // Cập nhật thủ công từng trường từ DTO
            product.setProductName(dto.productName);
            product.setSlug(dto.slug);
            product.setPrice(dto.price);
            product.setStockQuantity(dto.stockQuantity);
            product.setDescription(dto.description);
            product.setImageUrl(dto.imageUrl);
            product.setIsActive(dto.isActive);

            // Tìm và set lại category
            Categories category = categoryRepository.findById(dto.categoryId)
                    .orElseThrow(() -> new RuntimeException("Category không tồn tại với ID: " + dto.categoryId));
            product.setCategory(category);

            return ResponseEntity.ok(productRepository.save(product));
        }).orElse(ResponseEntity.notFound().build());
    }

    // 5. Xóa sản phẩm (Vue.js gọi DELETE)
    // URL: DELETE http://localhost:8080/api/products/1
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable("id") Integer id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return ResponseEntity.ok("Xóa sản phẩm thành công!");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}