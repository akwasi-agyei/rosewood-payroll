package com.rosewood.payroll;

public class PayrollCalculator {
    private static final int STANDARD_WEEK_HOURS = 40;
    private static final double OVERTIME_PREMIUM_RATE = 0.5;
    private static final int MEDIUM_TAX_BAND_START = 1_000;
    private static final int HIGH_TAX_BAND_START = 5_000;
    private static final double LOW_TAX_RATE = 0.05;
    private static final double MEDIUM_TAX_RATE = 0.15;
    private static final double HIGH_TAX_RATE = 0.25;

    private final EmployeeRepository repository;

    public PayrollCalculator(EmployeeRepository repository) {
        this.repository = repository;
    }

    public double calculateNetPay(String employeeId, double hoursWorked, double hourlyRate) {
        try {
            Employee employee = repository.findById(employeeId);

            double grossPay = hoursWorked * hourlyRate;

            if (hoursWorked > STANDARD_WEEK_HOURS) {
                grossPay += (hoursWorked - STANDARD_WEEK_HOURS) * hourlyRate * OVERTIME_PREMIUM_RATE;
            }

            double taxRate;
            if (grossPay < MEDIUM_TAX_BAND_START) {
                taxRate = LOW_TAX_RATE;
            } else if (grossPay < HIGH_TAX_BAND_START) {
                taxRate = MEDIUM_TAX_RATE;
            } else {
                taxRate = HIGH_TAX_RATE;
            }

            double netPay = grossPay - (grossPay * taxRate);
            System.out.println("Net pay for " + employee.getName() + ": GHS " + netPay);
            return netPay;
        } catch (Exception calculationFailure) {
            return 0;
        }
    }
}
