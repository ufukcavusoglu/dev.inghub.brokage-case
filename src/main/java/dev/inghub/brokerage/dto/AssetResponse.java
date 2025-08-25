package dev.inghub.brokerage.dto;

import java.math.BigDecimal;

public record AssetResponse(
        Long id,
        String customerId,
        String assetName,
        BigDecimal size,
        BigDecimal usableSize
) {}
