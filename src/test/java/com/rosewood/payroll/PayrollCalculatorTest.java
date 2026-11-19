package com.rosewood.payroll;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PayrollCalculatorTest {
    private static final double PAY_TOLERANCE = 0.000_001;

    private final PayrollCalculator calculator = new PayrollCalculator(
            new InMemoryEmployeeRepository(), new GrossPayCalculator(), BandedTaxPolicy.standard());

    @Test
    void calculatesRegularNetPay() {
        assertEquals(760.0, calculator.calculateNetPay("e1", 40, 20), PAY_TOLERANCE);
    }

    @Test
    void calculatesNetPayWithOvertimePremium() {
        assertEquals(1_402.50, calculator.calculateNetPay("e2", 50, 30), PAY_TOLERANCE);
    }

    @ParameterizedTest
    @CsvSource({
            "39.99, 759.81",
            "40.00, 760.00",
            "40.01, 760.285"
    })
    void appliesOvertimeOnlyToHoursAboveForty(double hoursWorked, double expectedNetPay) {
        assertEquals(expectedNetPay, calculator.calculateNetPay("e1", hoursWorked, 20), PAY_TOLERANCE);
    }

    @ParameterizedTest
    @CsvSource({
            "999.99, 949.9905",
            "1000.00, 850.00",
            "1000.01, 850.0085",
            "4999.99, 4249.9915",
            "5000.00, 3750.00",
            "5000.01, 3750.0075"
    })
    void appliesTheBandRateToAllGrossPay(double grossPay, double expectedNetPay) {
        assertEquals(expectedNetPay, calculator.calculateNetPay("e1", 1, grossPay), PAY_TOLERANCE);
    }

    @ParameterizedTest
    @CsvSource({"20, 40, 40, 20", "20, 50, 25, 40", "25, 200, 40, 125"})
    void equalGrossPayProducesEqualNetPay(
            double firstHours, double firstRate, double secondHours, double secondRate) {
        double firstNetPay = calculator.calculateNetPay("e1", firstHours, firstRate);
        double secondNetPay = calculator.calculateNetPay("e1", secondHours, secondRate);

        assertEquals(firstNetPay, secondNetPay, PAY_TOLERANCE);
    }
}
