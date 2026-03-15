# Design Patterns & SOLID Principles
## LibraTrack — University Library Management System

---

## Design Patterns Used (Gang of Four)

### 1. Singleton Pattern (Creational)
**Where:** `DatabaseConnection` class
**Why:** Ensures a single database connection pool is shared across the entire application, preventing resource waste from multiple connection instances.

```
┌─────────────────────────┐
│   DatabaseConnection    │
├─────────────────────────┤
│ - instance: DBConnection│
│ - connectionPool: Pool  │
├─────────────────────────┤
│ - DatabaseConnection()  │
│ + getInstance(): DBConn │
│ + getConnection(): Conn │
└─────────────────────────┘
```

**Implementation:** Private constructor, static `getInstance()` method with thread-safe lazy initialization.

---

### 2. Factory Pattern (Creational)
**Where:** `MemberFactory` class
**Why:** Encapsulates the creation logic for different member types (Student, Faculty). Adding a new member type (e.g., ResearchScholar) requires no changes to client code — just a new subclass and a factory update.

```
         ┌──────────┐
         │ Member   │ (abstract)
         └────┬─────┘
        ┌─────┴──────┐
   ┌────┴───┐  ┌─────┴───┐
   │Student │  │ Faculty  │
   └────────┘  └─────────┘

   MemberFactory.create(type) → Member
```

**Implementation:** `MemberFactory.createMember(MemberType type, ...)` returns the appropriate `Member` subclass with pre-configured borrowing limits and loan periods.

---

### 3. Observer Pattern (Behavioral)
**Where:** Reservation notification system
**Why:** When a book is returned and becomes available, all members who reserved that book need to be notified. Observer decouples the return logic from the notification logic.

```
┌──────────────┐       ┌────────────────┐
│ Book (Subject)│──────▶│ ReservationObs │
│              │       │  (Observer)    │
│ + addObs()   │       │ + update()     │
│ + removeObs()│       └────────────────┘
│ + notifyObs()│
└──────────────┘
```

**Implementation:** When a book is returned, `BookAvailabilitySubject.notifyObservers()` triggers notification to all `ReservationObserver` instances watching that book.

---

### 4. Strategy Pattern (Behavioral)
**Where:** Fine calculation
**Why:** Different member types have different fine rates. Strategy allows swapping fine calculation algorithms without modifying the borrowing/return logic.

```
     ┌──────────────────┐
     │ FineStrategy     │ (interface)
     │ + calculate()    │
     └───────┬──────────┘
       ┌─────┴──────┐
  ┌────┴────┐ ┌─────┴─────┐
  │Student  │ │ Faculty   │
  │FineStrat│ │ FineStrat │
  │ ₹2/day  │ │  ₹5/day   │
  └─────────┘ └───────────┘
```

**Implementation:** `FineCalculator` holds a `FineStrategy` reference. During return processing, the appropriate strategy is selected based on member type and `calculate(daysOverdue)` is called.

---

### 5. Command Pattern (Behavioral)
**Where:** CLI command processing
**Why:** Each CLI action (issue book, return book, search, etc.) is encapsulated as a `Command` object. This decouples the CLI input parsing from business logic execution, and makes it easy to add new commands.

```
┌──────────────┐      ┌──────────────┐
│  CLIRunner   │─────▶│  Command     │ (interface)
│              │      │ + execute()  │
└──────────────┘      └──────┬───────┘
                      ┌──────┴───────┐──────────────┐
                ┌─────┴─────┐ ┌──────┴─────┐ ┌──────┴──────┐
                │IssueBook  │ │ReturnBook  │ │SearchBook   │
                │Command    │ │Command     │ │Command      │
                └───────────┘ └────────────┘ └─────────────┘
```

**Implementation:** `CLIRunner` maps user input strings to `Command` objects and calls `execute()`.

---

## SOLID Principles

### S — Single Responsibility Principle
Each class has one reason to change:
- `BookRepository` — only handles book database operations
- `FineCalculator` — only calculates fines
- `CLIRunner` — only handles user input/output
- `BorrowService` — only orchestrates borrow/return business logic

### O — Open/Closed Principle
- **Fine calculation:** New fine strategies can be added (e.g., `ResearchScholarFineStrategy`) without modifying `FineCalculator`
- **Member types:** New member types via `MemberFactory` without changing existing creation logic
- **CLI commands:** New commands by implementing `Command` interface, no changes to `CLIRunner`

### L — Liskov Substitution Principle
- `Student` and `Faculty` are substitutable for `Member` — any code that works with `Member` works identically with either subclass
- All `FineStrategy` implementations are interchangeable where `FineStrategy` is expected
- All `Command` implementations can be used wherever `Command` is expected

### I — Interface Segregation Principle
- `FineStrategy` has only `calculate()` — implementers aren't forced to implement unrelated methods
- `Command` has only `execute()` — each command implements exactly what it needs
- Repository interfaces are split by entity (`BookRepository`, `MemberRepository`, `BorrowRecordRepository`) rather than one monolithic `Repository`

### D — Dependency Inversion Principle
- `BorrowService` depends on `BookRepository` and `MemberRepository` interfaces, not on concrete PostgreSQL implementations
- `FineCalculator` depends on `FineStrategy` interface, not on concrete strategy classes
- This enables testing with mock/in-memory repositories without changing service code
