package com.rosewood.payroll;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

class PayrollCalculatorTest {
    @Test
    void calculatesNetPay() {
        PayrollCalculator calculator = new PayrollCalculator(new InMemoryEmployeeRepository());
        double net = calculator.calculateNetPay("e1", 40, 20);
        assertTrue(net > 0);
    }

    @Test
    void calculatesOvertimePay() {
        PayrollCalculator calculator = mock(PayrollCalculator.class);
        when(calculator.calculateNetPay("e2", 50, 30)).thenReturn(1402.5);
        assertEquals(1402.5, calculator.calculateNetPay("e2", 50, 30));
    }

    @Test
    void returnsPayForAnEmployee() {
        PayrollCalculator calculator = new PayrollCalculator(new InMemoryEmployeeRepository());
        double net = calculator.calculateNetPay("e1", 35, 25);
        assertNotNull(net);
    }
}
