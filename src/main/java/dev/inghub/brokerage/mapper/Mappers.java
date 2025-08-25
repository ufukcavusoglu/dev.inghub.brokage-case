package dev.inghub.brokerage.mapper;


import dev.inghub.brokerage.dto.AssetResponse;
import dev.inghub.brokerage.dto.OrderResponse;
import dev.inghub.brokerage.entity.Asset;
import dev.inghub.brokerage.entity.Order;

public class Mappers {
    public static OrderResponse toDto(Order o) {
        return new OrderResponse(
                o.getId(), o.getCustomerId(), o.getAssetName(), o.getOrderSide(),
                o.getSize(), o.getPrice(), o.getStatus(), o.getCreateDate()
        );
    }
    public static AssetResponse toDto(Asset a) {
        return new AssetResponse(a.getId(), a.getCustomerId(), a.getAssetName(), a.getSize(), a.getUsableSize());
    }
}
