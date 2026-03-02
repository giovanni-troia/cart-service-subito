package it.subito.cart.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Centralized pricing calculation service.
 *
 * Rounding policy:
 * - Scale: 2 decimal places (cents)
 * - Mode: HALF_UP (standard commercial rounding)
 * - Strategy: round each line item, then sum for totals
 *
 * This ensures accuracy and prevents rounding discrepancies.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PricingCalculator {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * Calculates line net price.
     *
     * @param unitNetPrice price per unit (net, without VAT)
     * @param quantity quantity ordered
     * @return line net price
     */
    public static BigDecimal calculateLineNetPrice(BigDecimal unitNetPrice, int quantity) {
        BigDecimal result = unitNetPrice.multiply(BigDecimal.valueOf(quantity));
        return result.setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Calculates VAT amount for a given net price and VAT rate.
     *
     * @param netPrice net price (before VAT)
     * @param vatRate VAT rate as decimal (e.g., 0.22 for 22%)
     * @return VAT amount
     */
    public static BigDecimal calculateVatAmount(BigDecimal netPrice, BigDecimal vatRate) {
        BigDecimal result = netPrice.multiply(vatRate);
        return result.setScale(SCALE, ROUNDING_MODE);
    }

    /**
     * Calculates gross price (net + VAT).
     *
     * @param netPrice net price
     * @param vatAmount VAT amount
     * @return gross price
     */
    public static BigDecimal calculateGrossPrice(BigDecimal netPrice, BigDecimal vatAmount) {
        return netPrice.add(vatAmount);
    }

    /**
     * Validates VAT rate is between 0 and 1.
     *
     * @param vatRate VAT rate to validate
     * @throws IllegalArgumentException if rate is invalid
     */
    public static void validateVatRate(BigDecimal vatRate) {
        if (vatRate.compareTo(BigDecimal.ZERO) < 0 || vatRate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("VAT rate must be between 0 and 1");
        }
    }

    /**
     * Gets the configured rounding scale.
     */
    public static int getScale() {
        return SCALE;
    }

    /**
     * Gets the configured rounding mode.
     */
    public static RoundingMode getRoundingMode() {
        return ROUNDING_MODE;
    }
}

