package com.rosewood.payroll;

public class GrossPayCalculator {
    private static final int STANDARD_WEEK_HOURS = 40;
    private static final double OVERTIME_PREMIUM_RATE = 0.5;

    public double calculateGrossPay(double hoursWorked, double hourlyRate) {
        double grossPay = hoursWorked * hourlyRate;
        if (hoursWorked > STANDARD_WEEK_HOURS) {
            grossPay += (hoursWorked - STANDARD_WEEK_HOURS) * hourlyRate * OVERTIME_PREMIUM_RATE;
        }
        return grossPay;
    }
}
