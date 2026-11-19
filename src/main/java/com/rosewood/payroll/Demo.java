package com.rosewood.payroll;

public class Demo {
    public static void main(String[] args) {
        EmployeeRepository repository = new InMemoryEmployeeRepository();
        GrossPayCalculator grossPayCalculator = new GrossPayCalculator();
        TaxPolicy taxPolicy = BandedTaxPolicy.standard();
        PayrollCalculator calculator = new PayrollCalculator(repository, grossPayCalculator, taxPolicy);

        System.out.println(
                "Result e1, 40h @ 20     -> " + calculator.calculateNetPay("e1", 40, 20));
        System.out.println(
                "Result e2, 50h @ 30     -> " + calculator.calculateNetPay("e2", 50, 30));
        showMissingEmployee(calculator);
    }

    private static void showMissingEmployee(PayrollCalculator calculator) {
        try {
            calculator.calculateNetPay("unknown", 40, 20);
        } catch (EmployeeNotFoundException missingEmployee) {
            System.err.println("Payroll calculation failed. " + missingEmployee.getMessage());
        }
    }
}
