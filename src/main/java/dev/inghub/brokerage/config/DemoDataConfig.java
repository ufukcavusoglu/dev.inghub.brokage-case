package dev.inghub.brokerage.config;

import dev.inghub.brokerage.entity.Asset;
import dev.inghub.brokerage.entity.Customer;
import dev.inghub.brokerage.repo.AssetRepository;
import dev.inghub.brokerage.repo.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DemoDataConfig {
    @Bean
    CommandLineRunner seed(AssetRepository assets, CustomerRepository customers, PasswordEncoder encoder) {
        return args -> {
            if (customers.findByUsername("alice").isEmpty()) {
                customers.save(Customer.builder()
                        .username("alice")
                        .passwordHash(encoder.encode("alice123"))
                        .customerId("CUST-ALICE")
                        .role("CUSTOMER")
                        .build());
                assets.save(Asset.builder().customerId("CUST-ALICE").assetName("TRY")
                        .size(new BigDecimal("1000.00")).usableSize(new BigDecimal("1000.00")).build());
                assets.save(Asset.builder().customerId("CUST-ALICE").assetName("AKBNK")
                        .size(new BigDecimal("50")).usableSize(new BigDecimal("50")).build());
            }
        };
    }
}
