package com.minicommerce.repository;

import com.minicommerce.entity.Product;
import java.util.List;
import java.util.Optional;

// [Session 3 - OOP/SOLID] Abstraction: service chỉ biết interface này, không biết dữ liệu lưu ở đâu (RAM/DB/file).
// [Session 3 - SOLID] L (Liskov): mọi implementation (InMemory..., sau này Jdbc...) thay thế nhau được mà service không đổi.
// [Session 4 - Generic] Optional<Product>: kiểu generic báo rõ "có thể không tìm thấy" thay vì trả null.
public interface ProductRepository {
    Optional<Product> findById(String id);
    Optional<Product> findBySku(String sku);
    List<Product> findAll();
    void save(Product product);
    void deleteById(String id);
}
