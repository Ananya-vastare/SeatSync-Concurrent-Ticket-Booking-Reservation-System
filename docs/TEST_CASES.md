# Manual Test Cases — Concurrent Ticket Booking System

Record the actual result and execution status when you run each case. Do not mark a test as Pass until you have observed the result.

| Test ID | Scenario | Preconditions | Steps | Expected result | Actual result | Status |
|---|---|---|---|---|---|---|
| TC-01 | View events | Application is running | Choose menu option 1 | E01 and its event name are displayed | | Not Run |
| TC-02 | Initial seat availability | Fresh application state | Choose option 2 | A01–A10 are shown as available | | Not Run |
| TC-03 | Successful booking | A01 is available | Choose option 3; enter a user, E01, A01 | Booking is confirmed and A01 becomes booked | | Not Run |
| TC-04 | Duplicate seat booking | A01 is already booked | Try to book E01/A01 as another user | Second request is waitlisted; no second confirmed booking is created | | Not Run |
| TC-05 | Invalid event | Application is running | Try booking with an unknown event ID | Request is rejected; no booking is created | | Not Run |
| TC-06 | Invalid seat | E01 exists | Try booking seat Z99 | Request is rejected; no booking is created | | Not Run |
| TC-07 | Cancel booking | A confirmed booking exists and no one is waiting | Cancel its booking ID | Booking becomes cancelled and seat becomes available | | Not Run |
| TC-08 | Waiting-list promotion | A confirmed booking and at least one waiting user exist for the same seat | Cancel the confirmed booking | First waiting user is confirmed for that seat | | Not Run |
| TC-09 | Cancel unknown booking | No booking has ID B9999 | Cancel B9999 | Operation is rejected and state remains unchanged | | Not Run |
| TC-10 | Booking history | At least one booking exists | Choose option 5 | Booking details and status are displayed | | Not Run |
| TC-11 | Persistence | At least one booking exists | Save and exit; restart; inspect seats/history | Confirmed booking is restored from CSV | | Not Run |
| TC-12 | Concurrent booking | A03 is available | Choose option 7 | Exactly one thread confirms; the other joins the waiting list | | Not Run |

## Execution summary

- Total cases: 12
- Passed: ___
- Failed: ___
- Blocked: ___
- Not run: ___
- Environment: OS ___ / Java version ___ / Date ___
