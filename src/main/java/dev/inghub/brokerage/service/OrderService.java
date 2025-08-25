package dev.inghub.brokerage.service;


import dev.inghub.brokerage.domain.OrderSide;
import dev.inghub.brokerage.domain.OrderStatus;
import dev.inghub.brokerage.entity.Asset;
import dev.inghub.brokerage.entity.Order;
import dev.inghub.brokerage.repo.AssetRepository;
import dev.inghub.brokerage.repo.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;
    private static final String TRY = "TRY";

    @Transactional
    public Order createOrder(String customerId, String assetName, OrderSide side, BigDecimal size, BigDecimal price) {
        if (size.compareTo(BigDecimal.ZERO) <= 0 || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Size and price must be positive");
        }
        if (TRY.equalsIgnoreCase(assetName)) {
            throw new IllegalArgumentException("Asset to buy/sell cannot be TRY; TRY is the counter asset");
        }

        Order order = Order.newPending(customerId, assetName, side, size, price);
        if (side == OrderSide.BUY) {
            BigDecimal cost = price.multiply(size);
            Asset tryAsset = assetRepository.findWithLockByCustomerIdAndAssetName(customerId, TRY)
                    .orElseThrow(() -> new IllegalArgumentException("No TRY asset for customer " + customerId));
            if (tryAsset.getUsableSize().compareTo(cost) < 0) {
                throw new IllegalStateException("Insufficient TRY usable balance. Needed: " + cost + ", have: " + tryAsset.getUsableSize());
            }
            tryAsset.setUsableSize(tryAsset.getUsableSize().subtract(cost)); // reserve funds
            assetRepository.save(tryAsset);
        } else { // SELL
            Asset asset = assetRepository.findWithLockByCustomerIdAndAssetName(customerId, assetName)
                    .orElseThrow(() -> new IllegalArgumentException("No asset " + assetName + " for customer " + customerId));
            if (asset.getUsableSize().compareTo(size) < 0) {
                throw new IllegalStateException("Insufficient asset usable size to sell. Needed: " + size + ", have: " + asset.getUsableSize());
            }
            asset.setUsableSize(asset.getUsableSize().subtract(size)); // reserve shares
            assetRepository.save(asset);
        }
        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public List<Order> listOrders(String customerId, Instant from, Instant to) {
        return orderRepository.findByCustomerIdAndDateRange(customerId, from, to);
    }

    @Transactional
    public Order cancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be canceled");
        }
        if (order.getOrderSide() == OrderSide.BUY) {
            BigDecimal cost = order.getPrice().multiply(order.getSize());
            Asset tryAsset = assetRepository.findWithLockByCustomerIdAndAssetName(order.getCustomerId(), TRY)
                    .orElseThrow(() -> new IllegalStateException("TRY asset missing during cancel"));
            tryAsset.setUsableSize(tryAsset.getUsableSize().add(cost)); // release funds
            assetRepository.save(tryAsset);
        } else {
            Asset asset = assetRepository.findWithLockByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName())
                    .orElseThrow(() -> new IllegalStateException("Asset missing during cancel"));
            asset.setUsableSize(asset.getUsableSize().add(order.getSize())); // release shares
            assetRepository.save(asset);
        }
        order.setStatus(OrderStatus.CANCELED);
        return orderRepository.save(order);
    }

    @Transactional
    public Order matchOrder(String orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be matched");
        }
        if (order.getOrderSide() == OrderSide.BUY) {
            BigDecimal cost = order.getPrice().multiply(order.getSize());
            // consume reserved TRY (usable already reduced), now reduce total
            Asset tryAsset = assetRepository.findWithLockByCustomerIdAndAssetName(order.getCustomerId(), TRY)
                    .orElseThrow(() -> new IllegalStateException("TRY asset missing during match"));
            if (tryAsset.getSize().compareTo(cost) < 0) {
                throw new IllegalStateException("Insufficient TRY total balance to settle buy. Needed: " + cost + ", have: " + tryAsset.getSize());
            }
            tryAsset.setSize(tryAsset.getSize().subtract(cost));
            assetRepository.save(tryAsset);

            // add bought asset to holdings (both total and usable)
            Asset asset = assetRepository.findWithLockByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName())
                    .orElse(null);
            if (asset == null) {
                asset = Asset.builder()
                        .customerId(order.getCustomerId())
                        .assetName(order.getAssetName())
                        .size(order.getSize())
                        .usableSize(order.getSize())
                        .build();
            } else {
                asset.setSize(asset.getSize().add(order.getSize()));
                asset.setUsableSize(asset.getUsableSize().add(order.getSize()));
            }
            assetRepository.save(asset);
        } else { // SELL
            BigDecimal proceeds = order.getPrice().multiply(order.getSize());
            // reduce total of sold asset (usable was already reduced on create)
            Asset asset = assetRepository.findWithLockByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName())
                    .orElseThrow(() -> new IllegalStateException("Asset missing during match"));
            if (asset.getSize().compareTo(order.getSize()) < 0) {
                throw new IllegalStateException("Insufficient asset total size to settle sell");
            }
            asset.setSize(asset.getSize().subtract(order.getSize()));
            assetRepository.save(asset);

            // add TRY proceeds to both size and usable
            Asset tryAsset = assetRepository.findWithLockByCustomerIdAndAssetName(order.getCustomerId(), TRY)
                    .orElseGet(() -> Asset.builder().customerId(order.getCustomerId())
                            .assetName(TRY).size(BigDecimal.ZERO).usableSize(BigDecimal.ZERO).build());
            tryAsset.setSize(tryAsset.getSize().add(proceeds));
            tryAsset.setUsableSize(tryAsset.getUsableSize().add(proceeds));
            assetRepository.save(tryAsset);
        }
        order.setStatus(OrderStatus.MATCHED);
        return orderRepository.save(order);
    }
}
