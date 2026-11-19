package com.rosewood.payroll;

public class PayrollCalculator {
    private final EmployeeRepository repository;
    private final GrossPayCalculator grossPayCalculator;
    private final TaxPolicy taxPolicy;

    public PayrollCalculator(
            EmployeeRepository repository, GrossPayCalculator grossPayCalculator, TaxPolicy taxPolicy) {
        this.repository = repository;
        this.grossPayCalculator = grossPayCalculator;
        this.taxPolicy = taxPolicy;
    }

    public double calculateNetPay(String employeeId, double hoursWorked, double hourlyRate) {
        try {
            if (repository.findById(employeeId) == null) {
                return 0;
            }

            double grossPay = grossPayCalculator.calculateGrossPay(hoursWorked, hourlyRate);
            return grossPay - taxPolicy.calculateTax(grossPay);
        } catch (Exception calculationFailure) {
            return 0;
        }
    }
}
