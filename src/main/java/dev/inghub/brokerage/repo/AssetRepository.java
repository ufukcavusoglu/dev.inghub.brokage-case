package dev.inghub.brokerage.repo;

import dev.inghub.brokerage.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    Optional<Asset> findByCustomerIdAndAssetName(String customerId, String assetName);
    List<Asset> findByCustomerId(String customerId);

    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    Optional<Asset> findWithLockByCustomerIdAndAssetName(String customerId, String assetName);
}
