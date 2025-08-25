package dev.inghub.brokerage.repo;

import dev.inghub.brokerage.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUsername(String username);
    Optional<Customer> findByCustomerId(String customerId);
}
