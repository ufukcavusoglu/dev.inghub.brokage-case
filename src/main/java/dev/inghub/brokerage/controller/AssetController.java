package dev.inghub.brokerage.controller;

import dev.inghub.brokerage.dto.AssetResponse;
import dev.inghub.brokerage.mapper.Mappers;
import dev.inghub.brokerage.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @GetMapping
    public ResponseEntity<List<AssetResponse>> list(@RequestParam String customerId) {
        var assets = assetService.listAssets(customerId).stream().map(Mappers::toDto).toList();
        return ResponseEntity.ok(assets);
    }
}
