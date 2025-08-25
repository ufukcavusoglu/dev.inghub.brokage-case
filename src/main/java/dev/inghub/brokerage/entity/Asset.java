package dev.inghub.brokerage.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "assets", uniqueConstraints = @UniqueConstraint(columnNames = {"customerId","assetName"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Asset {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String assetName;

    @Column(nullable = false, precision = 22, scale = 4)
    private BigDecimal size;

    @Column(nullable = false, precision = 22, scale = 4)
    private BigDecimal usableSize;

    @Version
    private Long version;
}
