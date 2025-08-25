package dev.inghub.brokerage.controller;

import dev.inghub.brokerage.domain.OrderSide;
import dev.inghub.brokerage.dto.AssetResponse;
import dev.inghub.brokerage.dto.OrderResponse;
import dev.inghub.brokerage.entity.Order;
import dev.inghub.brokerage.mapper.Mappers;
import dev.inghub.brokerage.service.AssetService;
import dev.inghub.brokerage.service.OrderService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

    private final OrderService orderService;
    private final AssetService assetService;

    private String customerIdFromAuth(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaimAsString("customerId");
    }

    @GetMapping("/assets")
    public ResponseEntity<List<AssetResponse>> myAssets(Authentication authentication) {
        String customerId = customerIdFromAuth(authentication);
        var list = assetService.listAssets(customerId).stream().map(Mappers::toDto).toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderResponse>> myOrders(
            Authentication authentication,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        String customerId = customerIdFromAuth(authentication);
        var list = orderService.listOrders(customerId, from, to).stream().map(Mappers::toDto).toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/orders/buy")
    public ResponseEntity<OrderResponse> buy(Authentication authentication,
                                                 @RequestParam @NotBlank String asset,
                                                 @RequestParam BigDecimal size,
                                                 @RequestParam BigDecimal price) {
        String customerId = customerIdFromAuth(authentication);
        Order o = orderService.createOrder(customerId, asset, OrderSide.BUY, size, price);
        return ResponseEntity.ok(Mappers.toDto(o));
    }

    @PostMapping("/orders/sell")
    public ResponseEntity<OrderResponse> sell(Authentication authentication,
                                              @RequestParam @NotBlank String asset,
                                              @RequestParam BigDecimal size,
                                              @RequestParam BigDecimal price) {
        String customerId = customerIdFromAuth(authentication);
        Order o = orderService.createOrder(customerId, asset, OrderSide.SELL, size, price);
        return ResponseEntity.ok(Mappers.toDto(o));
    }
}
