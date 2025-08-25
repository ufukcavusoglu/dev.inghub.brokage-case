package dev.inghub.brokerage.dto;

import dev.inghub.brokerage.domain.OrderSide;
import dev.inghub.brokerage.domain.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        String id,
        String customerId,
        String assetName,
        OrderSide side,
        BigDecimal size,
        BigDecimal price,
        OrderStatus status,
        Instant createDate
) {}
