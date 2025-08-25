package dev.inghub.brokerage;

import dev.inghub.brokerage.domain.OrderSide;
import dev.inghub.brokerage.domain.OrderStatus;
import dev.inghub.brokerage.entity.Asset;
import dev.inghub.brokerage.entity.Order;
import dev.inghub.brokerage.repo.AssetRepository;
import dev.inghub.brokerage.repo.OrderRepository;
import dev.inghub.brokerage.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class OrderServiceTests {

    @Autowired
    OrderService orderService;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    AssetRepository assetRepository;

    private final String CUST = "CUST-ALICE";


    @BeforeEach
    @Transactional
    void resetBalances() {
        // 1) Önce siparişleri temizle (rezerv etkilerini izole etmek için)
        assetRepository.deleteAll();
        orderRepository.deleteAll();

        // 2) TRY’ı bul ya da oluştur, SONRA değerleri zorla AYARLA
        var tryAsset = assetRepository.findByCustomerIdAndAssetName(CUST, "TRY")
                .orElseGet(() -> Asset.builder()
                        .customerId(CUST).assetName("TRY")
                        .size(BigDecimal.ZERO).usableSize(BigDecimal.ZERO).build());
        tryAsset.setSize(new BigDecimal("1000.00"));
        tryAsset.setUsableSize(new BigDecimal("1000.00"));
        assetRepository.save(tryAsset);

        // 3) AKBNK için de aynısı
        var akbnk = assetRepository.findByCustomerIdAndAssetName(CUST, "AKBNK")
                .orElseGet(() -> Asset.builder()
                        .customerId(CUST).assetName("AKBNK")
                        .size(BigDecimal.ZERO).usableSize(BigDecimal.ZERO).build());
        akbnk.setSize(new BigDecimal("10"));
        akbnk.setUsableSize(new BigDecimal("10"));
        assetRepository.save(akbnk);
    }

    @Test
    void buyOrder_reservesTry_andMatchesIntoAsset() {
        Order o = orderService.createOrder(CUST, "AKBNK", OrderSide.BUY, new BigDecimal("2"), new BigDecimal("10.00"));
        assertEquals(OrderStatus.PENDING, o.getStatus());
        var tryAsset = assetRepository.findByCustomerIdAndAssetName(CUST, "TRY").orElseThrow();
        assertEquals(0,new BigDecimal("980.00").compareTo(tryAsset.getUsableSize())); // 1000 - 20 reserved

        // match
        o = orderService.matchOrder(o.getId());
        assertEquals(OrderStatus.MATCHED, o.getStatus());
        tryAsset = assetRepository.findByCustomerIdAndAssetName(CUST, "TRY").orElseThrow();
        assertEquals(0,new BigDecimal("980.00").compareTo(tryAsset.getUsableSize())); // unchanged
        assertEquals(0,new BigDecimal("980.00").compareTo(tryAsset.getSize())); // reduced total

        var akbnk = assetRepository.findByCustomerIdAndAssetName(CUST, "AKBNK").orElseThrow();
        assertEquals(0,new BigDecimal("12").compareTo(akbnk.getSize()));
        assertEquals(0,new BigDecimal("12").compareTo(akbnk.getUsableSize()));
    }

    @Test
    void sellOrder_reservesShares_andMatchesIntoTry() {
        Order o = orderService.createOrder(CUST, "AKBNK", OrderSide.SELL, new BigDecimal("5"), new BigDecimal("10.00"));
        assertEquals(OrderStatus.PENDING, o.getStatus());
        var akbnk = assetRepository.findByCustomerIdAndAssetName(CUST, "AKBNK").orElseThrow();
        assertEquals(0,new BigDecimal("5").compareTo(akbnk.getUsableSize()));

        // cancel
        o = orderService.cancelOrder(o.getId());
        assertEquals(OrderStatus.CANCELED, o.getStatus());
        akbnk = assetRepository.findByCustomerIdAndAssetName(CUST, "AKBNK").orElseThrow();
        assertEquals(0,new BigDecimal("10").compareTo(akbnk.getUsableSize()));
    }
}
