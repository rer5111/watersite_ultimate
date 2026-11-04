package com.waltercraft.donation.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "donations")
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "donation_type_id", nullable = false)
    private DonationType donationType;

    @Column(name = "amount_rub", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountRub;

    @Column(name = "payment_reference", length = 128, unique = true)
    private String paymentReference;

    @CreationTimestamp
    @Column(name = "donated_at", nullable = false, updatable = false)
    private Instant donatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected Donation() {
    }

    public Donation(Long playerId, DonationType donationType, BigDecimal amountRub, String paymentReference) {
        this.playerId = playerId;
        this.donationType = donationType;
        this.amountRub = amountRub;
        this.paymentReference = paymentReference;
    }

    public Long getId() { return id; }
    public Long getPlayerId() { return playerId; }
    public DonationType getDonationType() { return donationType; }
    public BigDecimal getAmountRub() { return amountRub; }
    public String getPaymentReference() { return paymentReference; }
    public Instant getDonatedAt() { return donatedAt; }
    public long getVersion() { return version; }
}
