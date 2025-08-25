package dev.inghub.brokerage.dto;

import dev.inghub.brokerage.domain.OrderSide;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateOrderRequest(
        @NotBlank String customerId,
        @NotBlank String assetName,
        @NotNull OrderSide side,
        @NotNull @DecimalMin(value = "0.0001") BigDecimal size,
        @NotNull @DecimalMin(value = "0.0001") BigDecimal price
) {}
