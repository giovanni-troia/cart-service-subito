package it.subito.cart.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PricingCalculator - Unit Tests")
class PricingCalculatorTest {

    @Test
    @DisplayName("Calculate line net price - basic case")
    void calculateLineNetPrice_basicCase() {
        BigDecimal unitPrice = new BigDecimal("10.00");
        int quantity = 5;

        BigDecimal result = PricingCalculator.calculateLineNetPrice(unitPrice, quantity);

        assertEquals(new BigDecimal("50.00"), result);
        assertEquals(2, result.scale());
    }

    @Test
    @DisplayName("Calculate line net price - with rounding")
    void calculateLineNetPrice_withRounding() {
        BigDecimal unitPrice = new BigDecimal("10.99");
        int quantity = 3;

        BigDecimal result = PricingCalculator.calculateLineNetPrice(unitPrice, quantity);

        assertEquals(new BigDecimal("32.97"), result);
    }

    @Test
    @DisplayName("Calculate line net price - large quantity")
    void calculateLineNetPrice_largeQuantity() {
        BigDecimal unitPrice = new BigDecimal("1.50");
        int quantity = 1000;

        BigDecimal result = PricingCalculator.calculateLineNetPrice(unitPrice, quantity);

        assertEquals(new BigDecimal("1500.00"), result);
    }

    @Test
    @DisplayName("Calculate VAT amount - standard VAT 22%")
    void calculateVatAmount_standardVat() {
        BigDecimal netPrice = new BigDecimal("100.00");
        BigDecimal vatRate = new BigDecimal("0.22");

        BigDecimal result = PricingCalculator.calculateVatAmount(netPrice, vatRate);

        assertEquals(new BigDecimal("22.00"), result);
    }

    @Test
    @DisplayName("Calculate VAT amount - reduced VAT 5%")
    void calculateVatAmount_reducedVat() {
        BigDecimal netPrice = new BigDecimal("100.00");
        BigDecimal vatRate = new BigDecimal("0.05");

        BigDecimal result = PricingCalculator.calculateVatAmount(netPrice, vatRate);

        assertEquals(new BigDecimal("5.00"), result);
    }

    @Test
    @DisplayName("Calculate VAT amount - with rounding (0.99 EUR with 22%)")
    void calculateVatAmount_withRounding() {
        BigDecimal netPrice = new BigDecimal("0.99");
        BigDecimal vatRate = new BigDecimal("0.22");

        BigDecimal result = PricingCalculator.calculateVatAmount(netPrice, vatRate);

        // 0.99 * 0.22 = 0.2178 -> 0.22 (HALF_UP rounding)
        assertEquals(new BigDecimal("0.22"), result);
        assertEquals(2, result.scale());
    }

    @Test
    @DisplayName("Calculate VAT amount - edge case with small amount")
    void calculateVatAmount_smallAmount() {
        BigDecimal netPrice = new BigDecimal("0.10");
        BigDecimal vatRate = new BigDecimal("0.22");

        BigDecimal result = PricingCalculator.calculateVatAmount(netPrice, vatRate);

        // 0.10 * 0.22 = 0.022 -> 0.02 (HALF_UP rounding)
        assertEquals(new BigDecimal("0.02"), result);
    }

    @Test
    @DisplayName("Calculate gross price")
    void calculateGrossPrice() {
        BigDecimal netPrice = new BigDecimal("100.00");
        BigDecimal vatAmount = new BigDecimal("22.00");

        BigDecimal result = PricingCalculator.calculateGrossPrice(netPrice, vatAmount);

        assertEquals(new BigDecimal("122.00"), result);
    }

    @Test
    @DisplayName("Complete calculation flow - standard scenario")
    void completeCalculationFlow() {
        // Scenario: 2x Premium Widget at 50.00 EUR (net) with 22% VAT
        BigDecimal unitNetPrice = new BigDecimal("50.00");
        BigDecimal vatRate = new BigDecimal("0.22");
        int quantity = 2;

        BigDecimal lineNetPrice = PricingCalculator.calculateLineNetPrice(unitNetPrice, quantity);
        BigDecimal lineVatAmount = PricingCalculator.calculateVatAmount(lineNetPrice, vatRate);
        BigDecimal lineGrossPrice = PricingCalculator.calculateGrossPrice(lineNetPrice, lineVatAmount);

        assertEquals(new BigDecimal("100.00"), lineNetPrice);
        assertEquals(new BigDecimal("22.00"), lineVatAmount);
        assertEquals(new BigDecimal("122.00"), lineGrossPrice);
    }

    @Test
    @DisplayName("Validate VAT rate - valid range")
    void validateVatRate_validRange() {
        assertDoesNotThrow(() -> PricingCalculator.validateVatRate(new BigDecimal("0.22")));
        assertDoesNotThrow(() -> PricingCalculator.validateVatRate(new BigDecimal("0.00")));
        assertDoesNotThrow(() -> PricingCalculator.validateVatRate(new BigDecimal("1.00")));
    }

    @Test
    @DisplayName("Validate VAT rate - invalid (negative)")
    void validateVatRate_negative() {
        assertThrows(IllegalArgumentException.class,
            () -> PricingCalculator.validateVatRate(new BigDecimal("-0.22")));
    }

    @Test
    @DisplayName("Validate VAT rate - invalid (greater than 1)")
    void validateVatRate_greaterThanOne() {
        assertThrows(IllegalArgumentException.class,
            () -> PricingCalculator.validateVatRate(new BigDecimal("1.50")));
    }

    @Test
    @DisplayName("Get configured scale")
    void getScale() {
        assertEquals(2, PricingCalculator.getScale());
    }

    @Test
    @DisplayName("Get configured rounding mode")
    void getRoundingMode() {
        assertEquals(RoundingMode.HALF_UP, PricingCalculator.getRoundingMode());
    }

    @Test
    @DisplayName("Complex rounding scenario - multiple items")
    void complexRoundingScenario() {
        // Multiple items with different amounts that need careful rounding
        BigDecimal item1Net = PricingCalculator.calculateLineNetPrice(new BigDecimal("9.99"), 3);
        BigDecimal item1Vat = PricingCalculator.calculateVatAmount(item1Net, new BigDecimal("0.22"));

        BigDecimal item2Net = PricingCalculator.calculateLineNetPrice(new BigDecimal("15.75"), 2);
        BigDecimal item2Vat = PricingCalculator.calculateVatAmount(item2Net, new BigDecimal("0.22"));

        // Verify rounding at each step
        assertEquals(new BigDecimal("29.97"), item1Net);
        assertEquals(new BigDecimal("6.59"), item1Vat); // 6.5934 -> 6.59

        assertEquals(new BigDecimal("31.50"), item2Net);
        assertEquals(new BigDecimal("6.93"), item2Vat); // 6.93 exactly

        BigDecimal totalNet = item1Net.add(item2Net);
        BigDecimal totalVat = item1Vat.add(item2Vat);

        assertEquals(new BigDecimal("61.47"), totalNet);
        assertEquals(new BigDecimal("13.52"), totalVat);
    }
}

