# Rosewood Payroll — Net Pay Service

Rosewood Payroll calculates net pay in Ghana cedis from an employee ID, hours worked, and an hourly rate. It pays time-and-a-half for hours above 40 and applies the matching tax rate to the full gross pay. See [ANALYSIS.md](ANALYSIS.md) for the findings from the original AI-generated code and tests.

## Running the project

Use JDK 21 and Maven from the project root.

```bash
mvn compile
mvn exec:java -Dexec.mainClass=com.rosewood.payroll.Demo
```

The demo shows GHS 760.00 for 40 hours at GHS 20, GHS 1,402.50 for 50 hours at GHS 30, and a clear error for an unknown employee.

```bash
mvn test
mvn verify
mvn test-compile org.pitest:pitest-maven:mutationCoverage
```

`mvn test` runs the JUnit 5 suite. Open `target/site/jacoco/index.html` after `mvn verify` to see which lines and branches the tests ran. Open `target/pit-reports/index.html` after PIT finishes to see which deliberate code changes the tests caught.

## Written rationale

### Why the code was refactored

- **Give each part one job.** `GrossPayCalculator` handles base pay and overtime, `BandedTaxPolicy` works out the tax, and `PayrollCalculator` coordinates the lookup and returns net pay. The demo handles printing. An overtime change now belongs in the gross-pay calculation, and a change to the printed message belongs in the demo.
- **Let the caller choose the dependencies.** The demo supplies the employee repository, gross-pay calculator, and tax policy through the constructor. The payroll calculator can use the in-memory repository in the demo or a Mockito repository that throws in a test. Both exercise the real payroll service. Naming the overtime cutoff, premium, and tax rates also makes the business rules easier to find.
- **Make failures visible.** The starter caught any exception and returned zero. Now an unknown ID raises `EmployeeNotFoundException`, invalid inputs get a clear error, and an unexpected repository failure reaches the caller with its original details. A known employee who worked zero hours can still receive a valid zero-pay result.

### How a new tax band fits

A band defines the gross-pay range it covers and the formula it uses. When assembling the tax policy, register the new band and use the standard policy for amounts outside that range. The calculator still asks for a tax amount and subtracts it from gross pay. Its code and the existing bands stay the same.

The integration test demonstrates this with a hypothetical band from **GHS 10,000 inclusive to GHS 15,000 exclusive**. It charges GHS 3,000 plus 40% of earnings above GHS 10,000. At GHS 12,000 gross, that gives GHS 3,800 in tax and GHS 8,200 net. At exactly GHS 15,000, the standard 25% rate applies and net pay is GHS 11,250.

### What was kept, fixed, or rewritten

| Decision | What changed and why |
| --- | --- |
| **Kept** | The employee model, lookup interface, sample data, and original pay rules provided a useful starting point. Expected amounts were worked out by hand and checked with tests around 40 hours and both tax thresholds. |
| **Fixed** | The repository and calculations became separate dependencies, printing moved to the demo, and failed calculations became visible. The valid demo payouts still match the starter. |
| **Rewrote** | The positive-pay check now asserts the correct amount. The overtime test now calls the real calculator. The non-null check on a number was replaced with checks for actual outcomes, including valid zero pay and an unknown employee. Mockito is used for the lookup failure, and integration tests run the real components together. |

The starter tests stayed green when the overtime premium was temporarily set to zero. The rewritten overtime test failed on the same change because net pay dropped from GHS 1,402.50 to GHS 1,275.00. That shows the test can catch a missed overtime payment.
