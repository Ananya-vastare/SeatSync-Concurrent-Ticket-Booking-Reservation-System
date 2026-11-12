# Testing Guide

This project includes manual test documentation and automated unit tests using JUnit 5.

## Automated unit tests (recommended)

Prerequisite: JDK 17+ and Maven installed. From the project root, run:

```bash
mvn test
```

Maven downloads JUnit on the first run, compiles the application and test code, and executes `BookingServiceTest`.

To run only this test class:

```bash
mvn -Dtest=BookingServiceTest test
```

The test class covers successful booking, invalid event/seat, duplicate booking and waiting list, cancellation and promotion, unknown booking cancellation, CSV persistence, and simultaneous booking requests.

## Manual test cases

Open `docs/TEST_CASES.md`. Execute each case through the console menu, fill in the Actual result column, and change Status to Pass or Fail based on what you observed.

## Defect reporting

If actual behaviour differs from expected behaviour, create a report using `docs/BUG_REPORT_TEMPLATE.md`. Include reproducible steps and console output. Do not report a defect just because output formatting differs unless the requirement specifies that format.

## Important scope note

These tests validate this in-memory, single-process demonstration. The `synchronized` guarantee applies to one `BookingService` instance; it is not distributed locking across multiple application instances. Waiting-list entries are not persisted by the current implementation.
