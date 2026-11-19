# Payroll Test Fixtures

This directory contains the authoritative test vectors for wage and overtime calculations. 

Per **AGENTS.md Section 5** and **TRD Section 12**, payroll logic is implemented twice:
1. **Java Backend** (`api/`): The authoritative server-side calculation.
2. **TypeScript Frontend** (`web/`): The offline preview engine running in IndexedDB.

Both engines must pass every test case defined in [`payroll.json`](./payroll.json) without deviation.

---

## JSON Format

The [`payroll.json`](./payroll.json) file contains a list of test cases under `cases`.

```json
{
  "version": "1.0.0",
  "rounding": "HALF_UP",
  "currency": "INR",
  "cases": [
    {
      "id": "CASE-A",
      "description": "...",
      "inputs": {
        "payType": "DAILY",
        "dailyWage": "600.00",
        "standardHours": 8,
        "otMultiplier": 1.5,
        "presentDays": 24,
        "halfDays": 2,
        "otHours": 6.0,
        "advancesRecovered": "3000.00"
      },
      "expected": {
        "hourlyRate": "75.00",
        "dayPay": "14400.00",
        "halfDayPay": "600.00",
        "otHours": 6.0,
        "otPay": "675.00",
        "gross": "15675.00",
        "advancesRecovered": "3000.00",
        "net": "12675.00"
      }
    }
  ]
}
```

### Fields

#### `inputs`
- `payType`: Compensation model (`DAILY`, `HOURLY`, `MONTHLY`).
- `dailyWage`: Daily rate as a 2-decimal string (`₹`).
- `standardHours`: Standard shift duration (default `8` hours).
- `otMultiplier`: Overtime hourly multiplier (default `1.5`).
- `presentDays`: Full days worked (weight: `1.0`).
- `halfDays`: Half-days worked (weight: `0.5`).
- `otHours`: Payable overtime hours.
- `workedMinutes`: Total recorded minutes worked on shift (for timestamp calculations).
- `otStepMinutes`: Step rounding size in minutes (default `15`).
- `advancesRecovered`: Total advance amount deducted this period as a 2-decimal string.

#### `expected`
- `hourlyRate`: `dailyWage / standardHours`.
- `dayPay`: `presentDays * dailyWage`.
- `halfDayPay`: `halfDays * 0.5 * dailyWage`.
- `otPay`: `otHours * hourlyRate * otMultiplier`.
- `gross`: `dayPay + halfDayPay + otPay`.
- `advancesRecovered`: Total advance deductions applied.
- `net`: `gross - advancesRecovered`.

---

## Strict Financial Rules

1. **No Floating Point**:
   - Currency strings in JSON represent exact fixed-point values.
   - **Java**: Always use `java.math.BigDecimal`. Never `double` or `float`.
   - **TypeScript**: Always use integer paise (`₹1.00 = 100 paise`) or a decimal library. Never JavaScript `number` for currency math.
2. **Rounding Rule**:
   - Round `HALF_UP` to 2 decimal places **once**, at the end of each worker's period total.
   - Example (Case C): `2.25 × 75 × 1.5 = 253.125` $\rightarrow$ rounds `HALF_UP` to `253.13`.
3. **Overtime Step Rounding**:
   - Raw overtime minutes are rounded down to the nearest configured step (default 15 minutes / 0.25 hours).
   - Example (Case C): 10h 20m worked on an 8h shift leaves 2h 20m (140 min) overtime. Rounded down to 15m steps = 135 min = 2.25 hours.
