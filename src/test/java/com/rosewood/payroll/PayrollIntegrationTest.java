package com.rosewood.payroll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PayrollIntegrationTest {
    private static final double PAY_TOLERANCE = 0.000_001;

    private final EmployeeRepository repository = new InMemoryEmployeeRepository();
    private final GrossPayCalculator grossPayCalculator = new GrossPayCalculator();
    private final TaxPolicy standardTaxPolicy = BandedTaxPolicy.standard();

    @Test
    void calculatesOvertimeAndTaxTogether() {
        var calculator = new PayrollCalculator(repository, grossPayCalculator, standardTaxPolicy);

        assertEquals(1_402.50, calculator.calculateNetPay("e2", 50, 30), PAY_TOLERANCE);
    }

    @Test
    void reportsAnEmployeeMissingFromTheRealRepository() {
        var calculator = new PayrollCalculator(repository, grossPayCalculator, standardTaxPolicy);

        var failure = assertThrows(EmployeeNotFoundException.class,
                () -> calculator.calculateNetPay("unknown", 40, 20));
        assertEquals("No employee found for ID unknown", failure.getMessage());
    }

    @ParameterizedTest
    @CsvSource({"8000, 6000", "10000, 7000", "12000, 8200", "15000, 11250"})
    void usesTheAddedTaxBandOnlyWithinItsRange(double grossPay, double expectedNetPay) {
        // The new band charges GHS 3,000 plus 40% of earnings above its lower limit.
        var newBand = new BandedTaxPolicy.TaxBand(10_000, 15_000,
                amount -> 3_000 + (amount - 10_000) * 0.4);
        TaxPolicy extendedPolicy = new BandedTaxPolicy(List.of(newBand), standardTaxPolicy);
        var calculator = new PayrollCalculator(repository, grossPayCalculator, extendedPolicy);

        assertEquals(expectedNetPay, calculator.calculateNetPay("e1", 1, grossPay), PAY_TOLERANCE);
    }
}
