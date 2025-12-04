# Lombok Annotations Explained

## What is Lombok?

Lombok is a library that generates boilerplate code for you at compile time. Instead of writing getters, setters, constructors, etc., you just add annotations and Lombok creates them automatically.

---

## The Annotations You're Using

### 1. @Getter

**What it does**: Creates getter methods for all fields

**Without Lombok:**
```java
public class Driver {
    private String firstName;
    private String lastName;

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
```

**With Lombok:**
```java
@Getter
public class Driver {
    private String firstName;
    private String lastName;
}
```

Lombok automatically generates `getFirstName()` and `getLastName()` for you!

---

### 2. @Setter

**What it does**: Creates setter methods for all fields

**Without Lombok:**
```java
public class Driver {
    private String firstName;
    private String lastName;

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
```

**With Lombok:**
```java
@Setter
public class Driver {
    private String firstName;
    private String lastName;
}
```

Lombok automatically generates `setFirstName()` and `setLastName()` for you!

---

### 3. @NoArgsConstructor

**What it does**: Creates a constructor with NO arguments (empty constructor)

**Without Lombok:**
```java
public class Driver {
    private String firstName;
    private String lastName;

    public Driver() {
        // Empty constructor
    }
}
```

**With Lombok:**
```java
@NoArgsConstructor
public class Driver {
    private String firstName;
    private String lastName;
}
```

**Why do you need this?**
- JPA/Hibernate requires a no-args constructor to create objects
- Spring uses it for dependency injection in some cases
- Useful for creating empty objects that you fill later

**Usage:**
```java
Driver driver = new Driver();  // Creates empty driver
driver.setFirstName("John");
driver.setLastName("Doe");
```

---

### 4. @AllArgsConstructor

**What it does**: Creates a constructor with ALL fields as arguments

**Without Lombok:**
```java
public class Driver {
    private String firstName;
    private String lastName;
    private String email;

    public Driver(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
}
```

**With Lombok:**
```java
@AllArgsConstructor
public class Driver {
    private String firstName;
    private String lastName;
    private String email;
}
```

**Why do you need this?**
- Quick way to create fully populated objects
- Useful for testing
- Good for immutable objects

**Usage:**
```java
Driver driver = new Driver("John", "Doe", "john@example.com");
```

---

### 5. @Builder

**What it does**: Creates a builder pattern for your class

**Without Lombok:**
```java
public class Driver {
    private String firstName;
    private String lastName;
    private String email;

    public static class DriverBuilder {
        private String firstName;
        private String lastName;
        private String email;

        public DriverBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public DriverBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public DriverBuilder email(String email) {
            this.email = email;
            return this;
        }

        public Driver build() {
            return new Driver(firstName, lastName, email);
        }
    }

    public static DriverBuilder builder() {
        return new DriverBuilder();
    }

    // Constructor
    public Driver(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
}
```

**With Lombok:**
```java
@Builder
public class Driver {
    private String firstName;
    private String lastName;
    private String email;
}
```

**Why do you need this?**
- Clean and readable object creation
- You can set only the fields you want
- Order doesn't matter

**Usage:**
```java
Driver driver = Driver.builder()
    .firstName("John")
    .lastName("Doe")
    .email("john@example.com")
    .build();

// Or just some fields:
Driver driver2 = Driver.builder()
    .firstName("Jane")
    .build();
```

---

### 6. @SuperBuilder

**What it does**: Like `@Builder`, but works with inheritance (parent-child classes)

**The Problem:**
```java
@Builder
public class Account {
    private String firstName;
    private String lastName;
}

@Builder
public class Driver extends Account {
    private String licenseNumber;
}

// This WON'T work:
Driver driver = Driver.builder()
    .firstName("John")      // ERROR! Can't set parent fields
    .licenseNumber("DL123")
    .build();
```

**The Solution with @SuperBuilder:**
```java
@SuperBuilder
public class Account {
    private String firstName;
    private String lastName;
}

@SuperBuilder
public class Driver extends Account {
    private String licenseNumber;
}

// This WORKS:
Driver driver = Driver.builder()
    .firstName("John")      // ✅ Can set parent fields!
    .lastName("Doe")        // ✅ Can set parent fields!
    .licenseNumber("DL123") // ✅ Can set own fields!
    .build();
```

**Why do you need this?**
- When you have inheritance (Driver extends Account, Passenger extends Account)
- You want to use builder pattern with both parent and child fields
- Regular `@Builder` doesn't work with inheritance

---

## Your Current Setup

```java
@Getter                  // Creates getters
@Setter                  // Creates setters
@NoArgsConstructor       // Creates Driver()
@AllArgsConstructor      // Creates Driver(all fields)
@SuperBuilder            // Creates builder for inheritance
public class Driver extends Account {
    private Double driverRating;
    private Boolean isAvailable;
    private String licenseNumber;
    private Location currentLocation;
    private Long currentVehicleId;

    @Builder.Default
    private List<Long> rideIds = new ArrayList<>();
}
```

This gives you:
1. All getters: `getDriverRating()`, `getIsAvailable()`, etc.
2. All setters: `setDriverRating()`, `setIsAvailable()`, etc.
3. Empty constructor: `new Driver()`
4. Full constructor: `new Driver(rating, available, license, ...)`
5. Builder with parent fields:
```java
Driver driver = Driver.builder()
    .firstName("John")           // From Account parent
    .lastName("Doe")             // From Account parent
    .email("john@example.com")   // From Account parent
    .driverRating(4.5)           // From Driver
    .isAvailable(true)           // From Driver
    .licenseNumber("DL123")      // From Driver
    .build();
```

---

## Why You NEED @SuperBuilder (Can't Just Use @Builder)

Your class hierarchy:
```
Account (parent)
   ├── Driver (child)
   └── Passenger (child)
```

**Problem**: If you use `@Builder` instead of `@SuperBuilder`:
```java
@Builder  // ❌ Won't work properly!
public class Driver extends Account {
    // ...
}

// This will FAIL:
Driver driver = Driver.builder()
    .firstName("John")  // ❌ ERROR: firstName is in parent class!
    .build();
```

**Solution**: Use `@SuperBuilder`:
```java
@SuperBuilder  // ✅ Works with inheritance!
public class Driver extends Account {
    // ...
}

// This WORKS:
Driver driver = Driver.builder()
    .firstName("John")  // ✅ Can access parent fields!
    .driverRating(4.5)  // ✅ Can access own fields!
    .build();
```

---

## Alternative: Remove Inheritance and Use @Builder

If you REALLY want to avoid `@SuperBuilder`, you need to **stop using inheritance**:

### Option 1: Keep Current Structure (RECOMMENDED)
```java
@SuperBuilder
public abstract class Account { ... }

@SuperBuilder
public class Driver extends Account { ... }
```

### Option 2: Remove Inheritance (NOT RECOMMENDED)
```java
@Builder
public class Driver {
    // Copy all Account fields here:
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phoneNumber;
    private LocalDateTime createdAt;

    // Driver-specific fields:
    private Double driverRating;
    private Boolean isAvailable;
    private String licenseNumber;
}
```

**Why Option 2 is bad:**
- Code duplication (Account fields copied to Driver AND Passenger)
- Harder to maintain
- Violates DRY (Don't Repeat Yourself) principle

---

## Summary Table

| Annotation | Creates | When to Use |
|-----------|---------|-------------|
| `@Getter` | `getFieldName()` methods | Always (very common) |
| `@Setter` | `setFieldName()` methods | When fields should be mutable |
| `@NoArgsConstructor` | `new Object()` | JPA entities, Spring beans |
| `@AllArgsConstructor` | `new Object(all fields)` | Quick object creation, testing |
| `@Builder` | Builder pattern | Clean object creation, NO inheritance |
| `@SuperBuilder` | Builder pattern | Clean object creation WITH inheritance |

---

## Recommendation

**Keep `@SuperBuilder`** for your Driver and Passenger classes because:
1. They extend Account
2. You want to use builder pattern
3. You need to set both parent and child fields

**Using `@SuperBuilder` is the correct choice for your architecture!**
