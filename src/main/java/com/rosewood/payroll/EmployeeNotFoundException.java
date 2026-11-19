package com.rosewood.payroll;

public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(String employeeId) {
        super("No employee found for ID " + employeeId);
    }
}
