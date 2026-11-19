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
        validateInputs(employeeId, hoursWorked, hourlyRate);
        // The lookup confirms the employee exists. Pay uses the hours and rate supplied by the caller.
        if (repository.findById(employeeId) == null) {
            throw new EmployeeNotFoundException(employeeId);
        }

        double grossPay = grossPayCalculator.calculateGrossPay(hoursWorked, hourlyRate);
        if (!Double.isFinite(grossPay)) {
            throw new PayrollCalculationException("Gross pay must be finite for employee " + employeeId);
        }

        double netPay = grossPay - taxPolicy.calculateTax(grossPay);
        if (!Double.isFinite(netPay)) {
            throw new PayrollCalculationException("Net pay must be finite for employee " + employeeId);
        }
        return netPay;
    }

    private void validateInputs(String employeeId, double hoursWorked, double hourlyRate) {
        if (employeeId == null || employeeId.isBlank()) {
            throw new IllegalArgumentException("Employee ID must not be blank");
        }
        if (!Double.isFinite(hoursWorked) || hoursWorked < 0) {
            throw new IllegalArgumentException("Hours worked must be finite and non-negative");
        }
        if (!Double.isFinite(hourlyRate) || hourlyRate < 0) {
            throw new IllegalArgumentException("Hourly rate must be finite and non-negative");
        }
    }
}
