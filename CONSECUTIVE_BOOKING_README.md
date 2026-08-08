# Consecutive Booking (Same Service, Multiple Persons)

This feature allows booking multiple persons for a service that is modeled as a single-resource requirement (for example: `Hairstyle` requiring one `Hairstylist`).

Instead of forcing one appointment with quantity `2`, the API creates **N consecutive appointments** with the same service and customer.

## Endpoint

`POST /api/appointments/consecutive`

## Request Body

```json
{
  "businessId": "<uuid>",
  "serviceId": "<uuid>",
  "startTime": "2026-07-29T09:00:00",
  "appointmentsCount": 2,
  "customerName": "Dana",
  "customerPhone": "0700000000",
  "inputs": null,
  "preferredResourceIds": null
}
```

## Behavior

- Slot #1 uses `startTime`.
- Slot #2 starts at `startTime + service.duration`.
- Slot #3 starts at `startTime + 2 * service.duration`, etc.
- Every slot is validated through the solver (`SchedulingService.plan`).
- If any slot is infeasible, the request fails with `400` and no appointments are committed.

## Response

`201 Created` with a list of created `AppointmentDTO` items.

## Notes for Chat AI

For a user request like:

> "I need a hairstyle for 2 persons"

The chat AI can call this endpoint with:
- `appointmentsCount = 2`
- same `serviceId`
- a candidate `startTime`

If the API fails, the AI should try a different `startTime` (or ask availability first).

