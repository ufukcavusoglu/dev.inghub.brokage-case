package dev.inghub.brokerage.repo;

import dev.inghub.brokerage.entity.Order;
import dev.inghub.brokerage.domain.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {
    @Query("select o from Order o where o.customerId = :customerId and o.createDate between :from and :to")
    List<Order> findByCustomerIdAndDateRange(String customerId, Instant from, Instant to);
    List<Order> findByCustomerId(String customerId);
    Optional<Order> findByIdAndCustomerId(String id, String customerId);
    List<Order> findByStatus(OrderStatus status);
}
