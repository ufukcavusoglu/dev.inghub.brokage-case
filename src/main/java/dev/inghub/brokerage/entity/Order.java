package dev.inghub.brokerage.entity;

import dev.inghub.brokerage.domain.OrderSide;
import dev.inghub.brokerage.domain.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders", indexes = {
        @Index(columnList = "customerId, createDate"),
        @Index(columnList = "status")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {
    @Id
    private String id;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String assetName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderSide orderSide;

    @Column(nullable = false, precision = 22, scale = 4)
    private BigDecimal size;

    @Column(nullable = false, precision = 22, scale = 4)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private Instant createDate;

    public static Order newPending(String customerId, String assetName, OrderSide side,
                                   BigDecimal size, BigDecimal price) {
        return Order.builder()
                .id(UUID.randomUUID().toString())
                .customerId(customerId)
                .assetName(assetName)
                .orderSide(side)
                .size(size)
                .price(price)
                .status(OrderStatus.PENDING)
                .createDate(Instant.now())
                .build();
    }
}
