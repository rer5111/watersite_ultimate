package com.waltercraft.payment.entity;

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
import java.time.LocalDate;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payer_username", nullable = false, length = 120)
    private String payerUsername;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "amount_rub", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountRub;

    @Column(name = "external_reference", length = 128, unique = true)
    private String externalReference;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    protected Payment() {
    }

    public Payment(String payerUsername, LocalDate paymentDate, BigDecimal amountRub, String externalReference) {
        this.payerUsername = payerUsername;
        this.paymentDate = paymentDate;
        this.amountRub = amountRub;
        this.externalReference = externalReference;
    }

    public void replace(String payerUsername, LocalDate paymentDate, BigDecimal amountRub, String externalReference) {
        this.payerUsername = payerUsername;
        this.paymentDate = paymentDate;
        this.amountRub = amountRub;
        this.externalReference = externalReference;
    }

    public Long getId() { return id; }
    public String getPayerUsername() { return payerUsername; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public BigDecimal getAmountRub() { return amountRub; }
    public String getExternalReference() { return externalReference; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
}
