package com.example.demo.repository;

import com.example.demo.entity.Cart;
import com.example.demo.entity.CartDetail;
import com.example.demo.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartDetailRepository extends JpaRepository<CartDetail, Integer> {
    Optional<CartDetail> findByCartAndProduct(Cart cart, Product product);
    List<CartDetail> findByCart(Cart cart);

    @Query("SELECT cd FROM CartDetail cd " +
            "JOIN FETCH cd.product p " +
            "LEFT JOIN FETCH p.images " +
            "LEFT JOIN FETCH p.category " +
            "WHERE cd.cart = :cart")
    List<CartDetail> findByCartWithAllDetails(@Param("cart") Cart cart);

    @Modifying
    @Query("DELETE FROM CartDetail cd WHERE cd.cart = :cart")
    void deleteByCart(@Param("cart") Cart cart);
}
