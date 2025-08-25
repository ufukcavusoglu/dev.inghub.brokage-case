package dev.inghub.brokerage.controller;

import dev.inghub.brokerage.dto.CreateOrderRequest;
import dev.inghub.brokerage.dto.OrderResponse;
import dev.inghub.brokerage.mapper.Mappers;
import dev.inghub.brokerage.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest req) {
        var order = orderService.createOrder(req.customerId(), req.assetName(), req.side(), req.size(), req.price());
        return ResponseEntity.ok(Mappers.toDto(order));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponse>> list(
            @RequestParam String customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        var list = orderService.listOrders(customerId, from, to).stream().map(Mappers::toDto).toList();
        return ResponseEntity.ok(list);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<OrderResponse> cancel(@PathVariable String orderId) {
        var updated = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(Mappers.toDto(updated));
    }

    // Bonus 2: Match endpoint (admin only)
    @PostMapping("/{orderId}/match")
    public ResponseEntity<OrderResponse> match(@PathVariable String orderId) {
        var updated = orderService.matchOrder(orderId);
        return ResponseEntity.ok(Mappers.toDto(updated));
    }
}
