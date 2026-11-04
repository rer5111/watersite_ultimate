package com.waltercraft.donation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "donation_types")
public class DonationType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "price_rub", nullable = false, precision = 12, scale = 2)
    private BigDecimal priceRub;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private boolean active;

    @Version
    @Column(nullable = false)
    private long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected DonationType() {
    }

    public DonationType(String code, String name, String description, BigDecimal priceRub,
                        int displayOrder, boolean active) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.priceRub = priceRub;
        this.displayOrder = displayOrder;
        this.active = active;
    }

    public void replace(String code, String name, String description, BigDecimal priceRub,
                        int displayOrder, boolean active) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.priceRub = priceRub;
        this.displayOrder = displayOrder;
        this.active = active;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPriceRub() { return priceRub; }
    public int getDisplayOrder() { return displayOrder; }
    public boolean isActive() { return active; }
    public long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
