# Rosewood Payroll starter analysis

## Baseline

| Demo case                            | Observed result |
| ------------------------------------ | --------------: |
| Known employee, 40 hours at GHS 20   |      GHS 760.00 |
| Known employee, 50 hours at GHS 30   |    GHS 1,402.50 |
| Unknown employee, 40 hours at GHS 20 |           `0.0` |

## What is wrong with the code:

### 1. A failed employee lookup looks like zero pay
This is the most serious code issue. When an employee ID is missing, the lookup returns `null`. The calculator tries to read the employee's name, catches the resulting error, and returns `0.0`. Now, the same return value, 0.0, can mean either "the calculation succeeded and the employee earned zero" or "the calculation failed because the employee could not be found." A caller that receives only the returned value cannot tell the difference.
```java
//InMemoryEmployeeRepository
@Override
public Employee findById(String id) {
    return employees.get(id); //returns null when the id is not found
}

//PayrollCalculator
try {
    Employee employee = repository.findById(employeeId);
    //...
    System.out.println("Net pay for " + employee.getName() + ": GHS " + net);
    return net;
} catch (Exception e) {
    return 0;
}
```

### 2. Hours and rates go straight into the calculation
The starter accepts negative hours and rates, as well as values such as `NaN` and infinity. For example, -1 hour at GHS 20 gives GHS -19 in net pay. Invalid input needs a clear failure that the caller can act on.
```java
//PayrollCalculator
double gross = hoursWorked * hourlyRate;
```

### 3. The calculator creates its own repository (Dependency Inversion violation)
The calculator creates the in-memory repository itself. Changing where employee data comes from requires editing the calculator. It also prevents a test from supplying a replacement. This violates dependency inversion. 
```java
//PayrollCalculator
private final EmployeeRepository repository = new InMemoryEmployeeRepository();
```
The field is declared as an `EmployeeRepository`, but the calculator still creates an `InMemoryEmployeeRepository` itself. That ties the calculator to one implementation. Passing an `EmployeeRepository` into the constructor would let the caller choose where employee data comes from and let tests supply a replacement.

### 4. A new tax band needs a change to the calculator (Open-Closed violation)
The calculator contains every tax band in one chain of conditions. Adding a band means editing the existing conditions, this is modification which violates the Open-Closed principle. The class should be closed for modification. Rather, extract the branching behavior into a method that can be called. Then, when adding a new band, you can extend that method, without having to modify the method call in the class.
```java
if (hoursWorked > 40) {
    gross = gross + (hoursWorked - 40) * hourlyRate * 0.5;
}

double taxRate;
if (gross < 1000) {
    taxRate = 0.05;
} else if (gross < 5000) {
    taxRate = 0.15;
} else {
    taxRate = 0.25;
}
```

### 5. One method does too many jobs (Single Responsibility violation)
`calculateNetPay` looks up the employee, calculates gross pay and tax, prints the result, and decides what to do when something goes wrong. The method does not have a single responsibility and instead does too much.
```java
double net = gross - (gross * taxRate);
System.out.println("Net pay for " + employee.getName() + ": GHS " + net);
```

### Style and readability issues
- **Magic numbers:** Values like the overtime cutoff `40`, `0.5` for the extra overtime premium, the tax thresholds `1000` and `5000` and other rates such as `0.05`, `0.15`, and `0.25` are also business rules. These values should be given descriptive names and be used as constants. The code snippet under #4 show these values being used.

- **Unnecessary comments:** There are a lot of comments that repeat what the code already shows. Some examples are in the snippet below
```java
//tax band, worked out from gross pay
double taxRate;
if (gross < 1000) {
    taxRate = 0.05;        // LOW
} else if (gross < 5000) {
    taxRate = 0.15;        // MEDIUM
} else {
    taxRate = 0.25;        // HIGH
}

assertTrue(net > 0); // it returned some pay
assertNotNull(net); // a result came back
```
- **Variable names:** Some of the variable names are not descriptive. One example is below:
```java
//PayrollCalculator
} catch (Exception e) {
    // something went wrong (for example the employee was not found) - return zero
    return 0;
}
```
The catch block calls its exception `e` when a more descriptive name should have been used.
- **Braces and method layout:** The `if` and `else` blocks have the opening brace, the return statement, and the closing brace on one line. Each method body should be indented on a separate line after the opening brace, with the closing brace on a separate line after the method body.
```java
//Employee
public String getId() { return id; }
public String getName() { return name; }
```
- **Indentation:** The starter uses four spaces per block. The default is two spaces when the team has no agreed indentation width.
- **Imports:** The test uses two wildcard static imports. Import the individual assertions and Mockito methods rather, and place the static import block above the regular JUnit import.
```java
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
```
- **Column limit:** The three demo print statements exceed the 100-character column limit. Wrap the statements or store the calculated amounts in clearly named variables before printing.
```java
System.out.println("Result e1, 40h @ 20     -> " + calculator.calculateNetPay("e1", 40, 20));
System.out.println("Result e2, 50h @ 30     -> " + calculator.calculateNetPay("e2", 50, 30));
System.out.println("Result unknown employee -> " + calculator.calculateNetPay("unknown", 40, 20));
```

## Which tests pass without proving the result
I have ranked the tests by what matters most for payroll.

### 1. `calculatesOvertimePay` 
The real calculation never runs. The test creates a mock for the calculator using Mockito. The mock is told to return 1,402.50 whenever it receives employee `e2`, 50 hours, and a rate of GHS 30. The assertion then calls this stand-in with those exact inputs instead of the real calculator and checks for GHS 1402.50. The answer used for the assertion comes from the test setup. The code that actually calculates overtime never runs. You can prove this by doing manual mutation testing. For example, you change how the overtime premium is calculated so no extra overtime pay is added. With the same inputs, the real calculator will return a net pay that is less than what the mock will return. The test should fail in this case because the return value and the value we are comparing to don't match anymore, but it still passes. This is a sign of a weak test. 
```java
//PayrollCalculatorTest
PayrollCalculator calculator = mock(PayrollCalculator.class);
when(calculator.calculateNetPay("e2", 50, 30)).thenReturn(1402.5);
assertEquals(1402.5, calculator.calculateNetPay("e2", 50, 30));
```

### 2. `returnsPayForAnEmployee`
Any returned number passes. The method returns a `double` which cannot be null. This means the assertion will always pass because it uses `assertNotNull`. 
```java
double net = calculator.calculateNetPay("e1", 35, 25);
assertNotNull(net);
```
For these inputs, the expected net pay is GHS 831.25 but the test passes regardless of what value is passed in the check. A better test would be to check that the employee was paid the correct amount:
```java
assertEquals(831.25, net);
```

### 3. `calculatesNetPay` 
Any positive amount passes. The test calls the real calculator, but it only checks that the result is above zero. It would accept GHS 800 with no tax deducted, even though the expected result is GHS 760.
```java
@Test
void calculatesNetPay() {
    PayrollCalculator calculator = new PayrollCalculator();
    double net = calculator.calculateNetPay("e1", 40, 20);
    assertTrue(net > 0); // it returned some pay
}
```

The test suite has no checks for tax boundaries, invalid hours or rates, a missing employee, or a repository failure.

