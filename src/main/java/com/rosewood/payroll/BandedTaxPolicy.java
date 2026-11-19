package com.rosewood.payroll;

import java.util.List;

public class BandedTaxPolicy implements TaxPolicy {
    private static final double MEDIUM_TAX_BAND_START = 1_000;
    private static final double HIGH_TAX_BAND_START = 5_000;
    private static final double LOW_TAX_RATE = 0.05;
    private static final double MEDIUM_TAX_RATE = 0.15;
    private static final double HIGH_TAX_RATE = 0.25;

    private final List<TaxBand> bands;
    private final TaxPolicy fallback;

    public BandedTaxPolicy(List<TaxBand> bands, TaxPolicy fallback) {
        this.bands = List.copyOf(bands);
        this.fallback = fallback;
    }

    public static BandedTaxPolicy standard() {
        return new BandedTaxPolicy(
                List.of(
                        new TaxBand(0, MEDIUM_TAX_BAND_START,
                                grossPay -> grossPay * LOW_TAX_RATE),
                        new TaxBand(MEDIUM_TAX_BAND_START, HIGH_TAX_BAND_START,
                                grossPay -> grossPay * MEDIUM_TAX_RATE)),
                grossPay -> grossPay * HIGH_TAX_RATE);
    }

    @Override
    public double calculateTax(double grossPay) {
        if (!Double.isFinite(grossPay) || grossPay < 0) {
            throw new IllegalArgumentException("Gross pay must be finite and non-negative");
        }
        for (TaxBand band : bands) {
            if (band.appliesTo(grossPay)) {
                return band.policy().calculateTax(grossPay);
            }
        }
        return fallback.calculateTax(grossPay);
    }

    public record TaxBand(double minimumInclusive, double maximumExclusive, TaxPolicy policy) {
        private boolean appliesTo(double grossPay) {
            return grossPay >= minimumInclusive && grossPay < maximumExclusive;
        }
    }
}
