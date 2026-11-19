package com.rosewood.payroll;

public class PayrollCalculator {
    private static final int MEDIUM_TAX_BAND_START = 1_000;
    private static final int HIGH_TAX_BAND_START = 5_000;
    private static final double LOW_TAX_RATE = 0.05;
    private static final double MEDIUM_TAX_RATE = 0.15;
    private static final double HIGH_TAX_RATE = 0.25;

    private final EmployeeRepository repository;
    private final GrossPayCalculator grossPayCalculator;

    public PayrollCalculator(EmployeeRepository repository, GrossPayCalculator grossPayCalculator) {
        this.repository = repository;
        this.grossPayCalculator = grossPayCalculator;
    }

    public double calculateNetPay(String employeeId, double hoursWorked, double hourlyRate) {
        try {
            if (repository.findById(employeeId) == null) {
                return 0;
            }

            double grossPay = grossPayCalculator.calculateGrossPay(hoursWorked, hourlyRate);
            double taxRate;
            if (grossPay < MEDIUM_TAX_BAND_START) {
                taxRate = LOW_TAX_RATE;
            } else if (grossPay < HIGH_TAX_BAND_START) {
                taxRate = MEDIUM_TAX_RATE;
            } else {
                taxRate = HIGH_TAX_RATE;
            }

            return grossPay - (grossPay * taxRate);
        } catch (Exception calculationFailure) {
            return 0;
        }
    }
}
