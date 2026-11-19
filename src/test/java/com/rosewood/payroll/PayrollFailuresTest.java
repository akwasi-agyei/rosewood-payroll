package com.rosewood.payroll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PayrollFailuresTest {
    private static final double PAY_TOLERANCE = 0.000_001;

    private final EmployeeRepository repository = mock(EmployeeRepository.class);
    private final GrossPayCalculator grossPayCalculator = new GrossPayCalculator();
    private final PayrollCalculator calculator = new PayrollCalculator(
            repository, grossPayCalculator, BandedTaxPolicy.standard());

    @ParameterizedTest
    @CsvSource({"0, 20", "40, 0"})
    void returnsZeroPayForAValidEmployee(double hoursWorked, double hourlyRate) {
        when(repository.findById("e1")).thenReturn(new Employee("e1", "Efua"));
        assertEquals(0.0, calculator.calculateNetPay("e1", hoursWorked, hourlyRate), PAY_TOLERANCE);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    void rejectsMissingOrBlankEmployeeIds(String employeeId) {
        var failure = assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateNetPay(employeeId, 40, 20));
        assertEquals("Employee ID must not be blank", failure.getMessage());
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1, Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY})
    void rejectsInvalidHours(double hoursWorked) {
        var failure = assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateNetPay("e1", hoursWorked, 20));
        assertEquals("Hours worked must be finite and non-negative", failure.getMessage());
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1, Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY})
    void rejectsInvalidHourlyRates(double hourlyRate) {
        var failure = assertThrows(IllegalArgumentException.class,
                () -> calculator.calculateNetPay("e1", 40, hourlyRate));
        assertEquals("Hourly rate must be finite and non-negative", failure.getMessage());
    }

    @Test
    void propagatesTheOriginalLookupFailure() {
        var lookupFailure = new IllegalStateException("Employee store is unavailable");
        when(repository.findById("e1")).thenThrow(lookupFailure);

        var actual = assertThrows(IllegalStateException.class,
                () -> calculator.calculateNetPay("e1", 40, 20));
        assertSame(lookupFailure, actual);
    }

    @Test
    void rejectsGrossPayThatOverflows() {
        when(repository.findById("e1")).thenReturn(new Employee("e1", "Efua"));

        var failure = assertThrows(PayrollCalculationException.class,
                () -> calculator.calculateNetPay("e1", 40, Double.MAX_VALUE));
        assertEquals("Gross pay must be finite for employee e1", failure.getMessage());
    }

    @Test
    void rejectsNonFiniteNetPay() {
        when(repository.findById("e1")).thenReturn(new Employee("e1", "Efua"));
        TaxPolicy invalidTaxPolicy = grossPay -> Double.NaN;
        var calculatorWithInvalidTax = new PayrollCalculator(
                repository, grossPayCalculator, invalidTaxPolicy);

        var failure = assertThrows(PayrollCalculationException.class,
                () -> calculatorWithInvalidTax.calculateNetPay("e1", 40, 20));
        assertEquals("Net pay must be finite for employee e1", failure.getMessage());
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1, Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY})
    void rejectsInvalidGrossPayWhenTaxPolicyIsCalledDirectly(double grossPay) {
        var failure = assertThrows(IllegalArgumentException.class,
                () -> BandedTaxPolicy.standard().calculateTax(grossPay));
        assertEquals("Gross pay must be finite and non-negative", failure.getMessage());
    }
}
