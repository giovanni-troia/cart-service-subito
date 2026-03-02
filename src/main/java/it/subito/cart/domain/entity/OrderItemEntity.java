package it.subito.cart.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OrderItemEntity represents a line in an order.
 *
 * Snapshot pattern: stores the product details (price, vat rate) at the time of order creation.
 * This ensures that if a product's price changes later, historical orders remain accurate.
 */
@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * FK column: order_items.order_id -> orders.id
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    private OrderEntity order;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal unitNetPrice;

    @Column(nullable = false)
    private BigDecimal vatRate;

    @Column(nullable = false)
    private BigDecimal lineNetPrice;

    @Column(nullable = false)
    private BigDecimal lineVatAmount;

    @Column(nullable = false)
    private String currency;

    /**
     * Calculates line gross price (net + VAT).
     */
    public BigDecimal getLineGrossPrice() {
        return lineNetPrice.add(lineVatAmount);
    }
}