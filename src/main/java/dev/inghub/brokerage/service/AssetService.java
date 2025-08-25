package dev.inghub.brokerage.service;

import dev.inghub.brokerage.entity.Asset;
import dev.inghub.brokerage.repo.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AssetService {
    private final AssetRepository assetRepository;

    @Transactional(readOnly = true)
    public List<Asset> listAssets(String customerId) {
        return assetRepository.findByCustomerId(customerId);
    }

    @Transactional
    public Asset requireAsset(String customerId, String assetName) {
        return assetRepository.findWithLockByCustomerIdAndAssetName(customerId, assetName)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found: " + assetName + " for customer " + customerId));
    }

    @Transactional
    public Asset getOrCreateAsset(String customerId, String assetName) {
        return assetRepository.findWithLockByCustomerIdAndAssetName(customerId, assetName)
                .orElseGet(() -> assetRepository.save(Asset.builder()
                        .customerId(customerId).assetName(assetName)
                        .size(BigDecimal.ZERO).usableSize(BigDecimal.ZERO).build()));
    }
}
