package com.bigbasket;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, String> {
    List<OrderEntity> findByUserEmailOrderByCreatedAtDesc(String email);
    Optional<OrderEntity> findByIdAndUserEmail(String id, String email);
}
