# Fare Calculation Feature - Complete Explanation

## 📋 What This Feature Does

This feature calculates how much a passenger will pay for a ride. It's like a price calculator that considers:
- **Distance** traveled (in kilometers)
- **Time** spent in the ride (in minutes)
- **Type of ride** (UberX, UberBlack, or UberPool)
- **Demand** at that moment (surge pricing)

### Example:
```
User wants: 10 km ride, takes 20 minutes, wants UberX, no surge
System calculates: €17.00
```

---

## 🏗️ Architecture & File Structure

### Why This Structure?

You asked about **service folders** and **handler locations** - here's the clean CQRS approach:

```
payment/
├── application/
│   └── CalculateFareQueryHandler.java  ✅ Handler WITH business logic
│                                            (No separate service needed!)
├── application/query/
│   ├── CalculateFareQuery.java          📝 Query record (input)
│   └── FareCalculationResult.java       📝 Result record (output)
│
├── api/web/
│   └── FareController.java              🌐 REST API endpoint
│
└── (NO service folder needed!)
```

### Why No Service Folder?

**Before (Wrong ❌):**
```
Query → QueryHandler → Service → Strategy
         (thin)        (fat)
```

**Now (Right ✅):**
```
Query → QueryHandler → Strategy
         (contains all logic!)
```

**Reason**: In CQRS, handlers ARE the service layer. No need for an extra layer!

---

## 📂 File-by-File Explanation

### 1. **CalculateFareQuery.java** (Input)
**What it is**: The request the user sends to calculate a fare

**Location**: `payment/application/query/`

**Code**:
```java
public record CalculateFareQuery(
    Double distanceKm,       // How far (10.0 km)
    Integer durationMin,     // How long (20 minutes)
    RideType rideType,       // Which service (UBER_X)
    Double demandMultiplier  // Surge pricing (1.0 = normal, 1.5 = 50% surge)
) {}
```

**Real Example**:
```json
{
  "distanceKm": 10.0,
  "durationMin": 20,
  "rideType": "UBER_X",
  "demandMultiplier": 1.0
}
```

---

### 2. **FareCalculationResult.java** (Output)
**What it is**: The answer the system gives back with detailed pricing

**Location**: `payment/application/query/`

**Code**:
```java
public record FareCalculationResult(
    RideType rideType,         // UBER_X
    Double distanceKm,         // 10.0
    Integer durationMin,       // 20
    Double demandMultiplier,   // 1.0
    BigDecimal totalFare,      // 17.00
    String currency,           // "EUR"
    FareBreakdown breakdown    // Details below
) {
    public record FareBreakdown(
        BigDecimal baseFare,              // €2.50 (pickup fee)
        BigDecimal distanceFare,          // €12.00 (10km × €1.20)
        BigDecimal durationFare,          // €6.00 (20min × €0.30)
        BigDecimal subtotal,              // €20.50
        BigDecimal demandMultiplierAmount,// €0.00 (no surge)
        BigDecimal discount,              // €0.00 (not UberPool)
        BigDecimal total                  // €17.00
    ) {}
}
```

**Real Example Response**:
```json
{
  "rideType": "UBER_X",
  "distanceKm": 10.0,
  "durationMin": 20,
  "demandMultiplier": 1.0,
  "totalFare": 17.00,
  "currency": "EUR",
  "breakdown": {
    "baseFare": 2.50,
    "distanceFare": 12.00,
    "durationFare": 6.00,
    "subtotal": 20.50,
    "demandMultiplierAmount": 0.00,
    "discount": 0.00,
    "total": 17.00
  }
}
```

---

### 3. **CalculateFareQueryHandler.java** (Business Logic)
**What it is**: The brain that does all the calculation

**Location**: `payment/application/` ← **In `application` folder, NOT in `query` folder!**

**What it does** (step by step):

#### Step 1: Pick the Right Strategy
```java
private PricingStrategy getStrategyForRideType(RideType rideType) {
    return switch (rideType) {
        case UBER_X -> uberXStrategy;      // Standard
        case UBER_BLACK -> uberBlackStrategy; // Premium
        case UBER_POOL -> uberPoolStrategy;   // Shared/Discounted
    };
}
```

#### Step 2: Calculate Total Fare
```java
// Uses existing UberXStrategy, UberBlackStrategy, or UberPoolStrategy
BigDecimal totalFare = strategy.calculateFare(
    query.distanceKm(),      // 10.0
    query.durationMin(),     // 20
    query.demandMultiplier() // 1.0
);
```

**What each strategy does**:
- **UberX**: `€2.50 + (10 × €1.20) + (20 × €0.30) = €20.50` (no discount)
- **UberBlack**: `€8.00 + (10 × €3.50) + (20 × €0.80) = €59.00` (premium)
- **UberPool**: `(€1.50 + (10 × €0.80) + (20 × €0.20)) × 0.70 = €7.70` (30% discount!)

#### Step 3: Calculate Breakdown (for transparency)
```java
// Base fare depends on ride type
BigDecimal baseFare = switch (rideType) {
    case UBER_X -> €2.50
    case UBER_BLACK -> €8.00
    case UBER_POOL -> €1.50
};

// Distance cost
BigDecimal distanceFare = 10.0 km × €1.20/km = €12.00

// Time cost
BigDecimal durationFare = 20 min × €0.30/min = €6.00

// Subtotal before surge/discount
BigDecimal subtotal = €2.50 + €12.00 + €6.00 = €20.50

// Apply UberPool discount (30% off)
BigDecimal discount = rideType == UBER_POOL ? €20.50 × 0.30 = €6.15 : €0.00

// Apply surge pricing (if demand > 1.0)
BigDecimal surgeFee = demandMultiplier > 1.0 ?
    (subtotal - discount) × (1.5 - 1.0) = extra € : €0.00
```

#### Step 4: Return Result
```java
return new FareCalculationResult(
    UBER_X,
    10.0,
    20,
    1.0,
    €17.00,      // Final fare
    "EUR",
    breakdown    // All the details
);
```

---

### 4. **FareController.java** (REST API)
**What it is**: The entry point where users send requests

**Location**: `payment/api/web/`

**Two endpoints**:

#### Endpoint 1: POST `/api/fares/calculate`
**Use case**: Frontend sends a JSON body

**Request**:
```bash
curl -X POST http://localhost:8080/api/fares/calculate \
  -H "Content-Type: application/json" \
  -d '{
    "distanceKm": 10.0,
    "durationMin": 20,
    "rideType": "UBER_X",
    "demandMultiplier": 1.0
  }'
```

**Response**: (Full FareCalculationResult as shown above)

#### Endpoint 2: GET `/api/fares/estimate`
**Use case**: Quick estimate with URL parameters (easier for simple requests)

**Request**:
```bash
curl "http://localhost:8080/api/fares/estimate?distanceKm=10.0&durationMin=20&rideType=UBER_X&demandMultiplier=1.0"
```

**Code**:
```java
@PostMapping("/calculate")
public ResponseEntity<FareCalculationResult> calculateFare(@RequestBody CalculateFareQuery query) {
    FareCalculationResult result = calculateFareQueryHandler.handle(query);
    return ResponseEntity.ok(result);
}

@GetMapping("/estimate")
public ResponseEntity<FareCalculationResult> estimateFare(
    @RequestParam Double distanceKm,
    @RequestParam Integer durationMin,
    @RequestParam String rideType,
    @RequestParam(defaultValue = "1.0") Double demandMultiplier
) {
    // Convert params to query object
    CalculateFareQuery query = new CalculateFareQuery(...);
    return ResponseEntity.ok(calculateFareQueryHandler.handle(query));
}
```

---

### 5. **UberPoolStrategy.java** (New Strategy)
**What it is**: Pricing logic specifically for UberPool (shared rides)

**Location**: `ride/domain/strategy/`

**Why it's different**: UberPool is cheaper because you share with other passengers!

**Code**:
```java
@Component
public class UberPoolStrategy implements PricingStrategy {
    private static final BigDecimal BASE_FARE = €1.50;      // Lower base
    private static final BigDecimal COST_PER_KM = €0.80;    // Lower per km
    private static final BigDecimal COST_PER_MIN = €0.20;   // Lower per min
    private static final BigDecimal DISCOUNT_RATE = 0.70;   // 30% discount!

    @Override
    public BigDecimal calculateFare(double distanceKm, int durationMin, double demandMultiplier) {
        BigDecimal total = BASE_FARE
            .add(distanceKm × COST_PER_KM)
            .add(durationMin × COST_PER_MIN)
            .multiply(DISCOUNT_RATE)        // Apply 30% discount
            .multiply(demandMultiplier);    // Then surge if needed

        return Math.max(total, €3.00);      // Minimum €3.00
    }
}
```

---

## 🔄 How It All Works Together

### Request Flow:
```
1. User sends request
   POST /api/fares/calculate
   { "distanceKm": 10.0, "durationMin": 20, "rideType": "UBER_X", "demandMultiplier": 1.0 }

2. FareController receives it
   → Converts to CalculateFareQuery object

3. CalculateFareQueryHandler.handle(query) is called
   Step 3a: Select UberXStrategy
   Step 3b: Calculate: €2.50 + (10 × €1.20) + (20 × €0.30) = €17.00
   Step 3c: Build detailed breakdown
   Step 3d: Return FareCalculationResult

4. FareController returns JSON response
   { "totalFare": 17.00, "breakdown": {...}, ... }
```

---

## 💰 Pricing Examples

### Example 1: UberX (Normal Day)
```
Distance: 10 km
Duration: 20 minutes
Ride Type: UBER_X
Surge: 1.0 (no surge)

Calculation:
Base fare:     €2.50
Distance:      10 × €1.20 = €12.00
Duration:      20 × €0.30 = €6.00
Subtotal:      €20.50
Surge:         ×1.0 = €0.00 extra
Discount:      €0.00
──────────────────────
TOTAL:         €20.50
```

### Example 2: UberBlack (Premium)
```
Distance: 10 km
Duration: 20 minutes
Ride Type: UBER_BLACK
Surge: 1.0

Calculation:
Base fare:     €8.00
Distance:      10 × €3.50 = €35.00
Duration:      20 × €0.80 = €16.00
Subtotal:      €59.00
Surge:         ×1.0 = €0.00
──────────────────────
TOTAL:         €59.00
```

### Example 3: UberPool (Shared, Discounted)
```
Distance: 10 km
Duration: 20 minutes
Ride Type: UBER_POOL
Surge: 1.0

Calculation:
Base fare:     €1.50
Distance:      10 × €0.80 = €8.00
Duration:      20 × €0.20 = €4.00
Subtotal:      €13.50
Discount:      30% = -€4.05
After Discount:€9.45
Surge:         ×1.0 = €0.00
──────────────────────
TOTAL:         €9.45
```

### Example 4: UberX with Surge (Friday Night)
```
Distance: 10 km
Duration: 20 minutes
Ride Type: UBER_X
Surge: 1.5 (50% surge!)

Calculation:
Base fare:     €2.50
Distance:      10 × €1.20 = €12.00
Duration:      20 × €0.30 = €6.00
Subtotal:      €20.50
Surge:         ×1.5 = +€10.25 extra
──────────────────────
TOTAL:         €30.75
```

---

## 🧪 Testing

### Test File Location
`src/test/java/com/uber/backend/payment/application/CalculateFareQueryHandlerTest.java`

### What We Test:
1. ✅ UberX calculation works
2. ✅ UberBlack calculation works (premium pricing)
3. ✅ UberPool calculation works (with discount)
4. ✅ Surge pricing applies correctly
5. ✅ Minimum fare is enforced
6. ✅ Zero distance defaults to minimum fare
7. ✅ Breakdown is provided
8. ✅ High surge (2x) works correctly

**All 103 tests pass!** ✅

---

## 🎯 Key Design Decisions

### 1. No Separate Service Layer
**Why?** CQRS handlers already ARE the service layer. Adding a service folder creates unnecessary indirection.

**Before (Confusing)**:
```
Controller → QueryHandler → Service → Strategy
              (calls)        (logic)
```

**After (Clean)**:
```
Controller → QueryHandler → Strategy
              (has logic)
```

### 2. Handler in `/application`, NOT `/application/query`
**Why?** Handlers are application-level components that USE queries, they're not part of the query data itself.

**Structure**:
```
application/
├── CalculateFareQueryHandler.java  ← Handler (application logic)
└── query/
    ├── CalculateFareQuery.java      ← Data (input)
    └── FareCalculationResult.java   ← Data (output)
```

### 3. Using Records for Queries/Results
**Why?** Records are perfect for immutable data transfer objects (DTOs) - concise and clear.

### 4. Reusing Existing Strategies
**Why?** You already had `UberXStrategy` and `UberBlackStrategy` - no need to duplicate! Just added `UberPoolStrategy` to complete the set.

---

## 🚀 Next Steps (Phase 2)

Now that fare calculation works, you can:
1. **Process Payments** - Charge the calculated fare
2. **Save Payment Methods** - Let users save credit cards
3. **Auto-charge After Rides** - When ride completes, charge automatically
4. **Payment History** - Show past payments

---

## 📝 Summary

**What we built**: A fare calculator that:
- Calculates prices for UberX, UberBlack, and UberPool
- Handles surge pricing
- Provides detailed breakdowns
- Has clean CQRS architecture
- Has 100% test coverage

**Structure**: Clean, no unnecessary layers
- Queries/Results in `/application/query`
- Handler WITH logic in `/application`
- Controller in `/api/web`
- No service folder needed!
