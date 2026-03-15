# UML Diagrams
## LibraTrack — University Library Management System

---

## 1. ER Diagram

```mermaid
erDiagram
    BOOKS {
        SERIAL id PK
        VARCHAR title
        VARCHAR author
        VARCHAR isbn UK
        VARCHAR genre
        INT total_copies
        INT available_copies
    }

    MEMBERS {
        SERIAL id PK
        VARCHAR name
        VARCHAR email
        VARCHAR phone
        VARCHAR member_type
        BOOLEAN is_active
        TIMESTAMP created_at
    }

    BORROW_RECORDS {
        SERIAL id PK
        INT book_id FK
        INT member_id FK
        DATE issue_date
        DATE due_date
        DATE return_date
        BOOLEAN is_returned
    }

    RESERVATIONS {
        SERIAL id PK
        INT book_id FK
        INT member_id FK
        TIMESTAMP reserved_at
        BOOLEAN notified
        BOOLEAN fulfilled
    }

    FINES {
        SERIAL id PK
        INT borrow_record_id FK
        INT member_id FK
        DECIMAL amount
        BOOLEAN is_paid
        TIMESTAMP created_at
    }

    BOOKS ||--o{ BORROW_RECORDS : "is borrowed in"
    MEMBERS ||--o{ BORROW_RECORDS : "borrows"
    BOOKS ||--o{ RESERVATIONS : "is reserved in"
    MEMBERS ||--o{ RESERVATIONS : "reserves"
    BORROW_RECORDS ||--o| FINES : "may generate"
    MEMBERS ||--o{ FINES : "owes"
```

---

## 2. Class Diagram

### 2.1 Domain Models & Factory Pattern

```mermaid
classDiagram
    class Member {
        <<abstract>>
        -int id
        -String name
        -String email
        -String phone
        -MemberType memberType
        -boolean isActive
        -LocalDateTime createdAt
        +getBorrowLimit()* int
        +getLoanPeriodDays()* int
    }

    class Student {
        -BORROW_LIMIT : int = 3
        -LOAN_PERIOD_DAYS : int = 14
        +getBorrowLimit() int
        +getLoanPeriodDays() int
    }

    class Faculty {
        -BORROW_LIMIT : int = 5
        -LOAN_PERIOD_DAYS : int = 30
        +getBorrowLimit() int
        +getLoanPeriodDays() int
    }

    class MemberType {
        <<enumeration>>
        STUDENT
        FACULTY
    }

    class MemberFactory {
        +createMember(type, name, email, phone)$ Member
    }

    class Book {
        -int id
        -String title
        -String author
        -String isbn
        -String genre
        -int totalCopies
        -int availableCopies
    }

    class BorrowRecord {
        -int id
        -int bookId
        -int memberId
        -LocalDate issueDate
        -LocalDate dueDate
        -LocalDate returnDate
        -boolean isReturned
    }

    class Reservation {
        -int id
        -int bookId
        -int memberId
        -LocalDateTime reservedAt
        -boolean notified
        -boolean fulfilled
    }

    class Fine {
        -int id
        -int borrowRecordId
        -int memberId
        -double amount
        -boolean isPaid
        -LocalDateTime createdAt
    }

    Member <|-- Student
    Member <|-- Faculty
    Member --> MemberType
    MemberFactory ..> Member : creates
    MemberFactory ..> MemberType : uses
```

### 2.2 Strategy Pattern — Fine Calculation

```mermaid
classDiagram
    class FineStrategy {
        <<interface>>
        +calculate(daysOverdue : long) double
    }

    class StudentFineStrategy {
        -RATE_PER_DAY : double = 2.0
        +calculate(daysOverdue : long) double
    }

    class FacultyFineStrategy {
        -RATE_PER_DAY : double = 5.0
        +calculate(daysOverdue : long) double
    }

    class FineCalculator {
        -strategy : FineStrategy
        +FineCalculator(memberType : MemberType)
        +calculate(daysOverdue : long) double
    }

    FineStrategy <|.. StudentFineStrategy
    FineStrategy <|.. FacultyFineStrategy
    FineCalculator --> FineStrategy : delegates to
```

### 2.3 Observer Pattern — Reservation Notifications

```mermaid
classDiagram
    class BookAvailabilityObserver {
        <<interface>>
        +onBookAvailable(bookId : int, bookTitle : String) void
    }

    class ReservationNotifier {
        -observers : List~BookAvailabilityObserver~
        -reservationRepo : ReservationRepository
        +addObserver(observer) void
        +removeObserver(observer) void
        +notifyBookAvailable(bookId, bookTitle) void
    }

    ReservationNotifier --> BookAvailabilityObserver : notifies
    ReservationNotifier --> ReservationRepository : queries
```

### 2.4 Command Pattern — CLI Dispatch

```mermaid
classDiagram
    class Command {
        <<interface>>
        +execute() void
    }

    class AddBookCommand {
        +execute() void
    }
    class SearchBookCommand {
        +execute() void
    }
    class ListBooksCommand {
        +execute() void
    }
    class IssueBookCommand {
        +execute() void
    }
    class ReturnBookCommand {
        +execute() void
    }
    class ReserveBookCommand {
        +execute() void
    }
    class RegisterMemberCommand {
        +execute() void
    }
    class ListMembersCommand {
        +execute() void
    }
    class MemberHistoryCommand {
        +execute() void
    }
    class ViewFinesCommand {
        +execute() void
    }
    class PayFineCommand {
        +execute() void
    }

    class LibraTrackApp {
        -commands : Map~String, Command~
        -scanner : Scanner
        +run() void
        -initCommands() void
        -mainLoop() void
    }

    Command <|.. AddBookCommand
    Command <|.. SearchBookCommand
    Command <|.. ListBooksCommand
    Command <|.. IssueBookCommand
    Command <|.. ReturnBookCommand
    Command <|.. ReserveBookCommand
    Command <|.. RegisterMemberCommand
    Command <|.. ListMembersCommand
    Command <|.. MemberHistoryCommand
    Command <|.. ViewFinesCommand
    Command <|.. PayFineCommand
    LibraTrackApp --> Command : dispatches
```

### 2.5 Singleton Pattern — Database Connection

```mermaid
classDiagram
    class DatabaseConnection {
        <<Singleton>>
        -instance$ : DatabaseConnection
        -dataSource : HikariDataSource
        -DatabaseConnection(url, user, password)
        +getInstance()$ DatabaseConnection
        +initialize(url, user, password)$ void
        +getConnection() Connection
        +close() void
    }
```

### 2.6 Repository Layer (Dependency Inversion)

```mermaid
classDiagram
    class BookRepository {
        <<interface>>
        +save(book) Book
        +findById(id) Optional~Book~
        +findByIsbn(isbn) Optional~Book~
        +search(keyword) List~Book~
        +findAll() List~Book~
        +update(book) void
        +delete(id) void
    }

    class MemberRepository {
        <<interface>>
        +save(member) Member
        +findById(id) Optional~Member~
        +findAll() List~Member~
        +update(member) void
    }

    class BorrowRecordRepository {
        <<interface>>
        +save(record) BorrowRecord
        +findActiveByBookAndMember(bookId, memberId) Optional
        +findActiveByMemberId(memberId) List
        +findByMemberId(memberId) List
        +update(record) void
    }

    class FineRepository {
        <<interface>>
        +save(fine) Fine
        +findUnpaidByMemberId(memberId) List~Fine~
        +update(fine) void
        +getTotalUnpaidByMemberId(memberId) double
    }

    class ReservationRepository {
        <<interface>>
        +save(reservation) Reservation
        +findPendingByBookId(bookId) List
        +findByMemberId(memberId) List
        +update(reservation) void
    }

    class PgBookRepository { }
    class PgMemberRepository { }
    class PgBorrowRecordRepository { }
    class PgFineRepository { }
    class PgReservationRepository { }

    BookRepository <|.. PgBookRepository
    MemberRepository <|.. PgMemberRepository
    BorrowRecordRepository <|.. PgBorrowRecordRepository
    FineRepository <|.. PgFineRepository
    ReservationRepository <|.. PgReservationRepository
```

### 2.7 Service Layer

```mermaid
classDiagram
    class BookService {
        -bookRepo : BookRepository
        +addBook(...) Book
        +searchBooks(keyword) List~Book~
        +listAllBooks() List~Book~
        +findByIsbn(isbn) Optional~Book~
        +removeBook(id) void
    }

    class MemberService {
        -memberRepo : MemberRepository
        +registerMember(type, name, email, phone) Member
        +findById(id) Optional~Member~
        +listAllMembers() List~Member~
        +deactivateMember(id) void
    }

    class BorrowService {
        -bookRepo : BookRepository
        -memberRepo : MemberRepository
        -borrowRepo : BorrowRecordRepository
        -reservationRepo : ReservationRepository
        -fineRepo : FineRepository
        -notifier : ReservationNotifier
        +issueBook(memberId, isbn) BorrowRecord
        +returnBook(memberId, isbn) ReturnResult
        +reserveBook(memberId, isbn) Reservation
        +getMemberHistory(memberId) List
    }

    class FineService {
        -fineRepo : FineRepository
        +getUnpaidFines(memberId) List~Fine~
        +getTotalUnpaid(memberId) double
        +payFine(fineId, unpaidFines) void
    }

    BookService --> BookRepository
    MemberService --> MemberRepository
    BorrowService --> BookRepository
    BorrowService --> MemberRepository
    BorrowService --> BorrowRecordRepository
    BorrowService --> FineRepository
    BorrowService --> ReservationNotifier
    BorrowService ..> FineCalculator : creates
    FineService --> FineRepository
```

---

## 3. Sequence Diagrams

### 3.1 Issue Book Flow

```mermaid
sequenceDiagram
    actor Librarian
    participant CLI as LibraTrackApp
    participant Cmd as IssueBookCommand
    participant Svc as BorrowService
    participant MR as MemberRepository
    participant FR as FineRepository
    participant BR as BookRepository
    participant BRR as BorrowRecordRepository

    Librarian->>CLI: selects "6. Issue Book"
    CLI->>Cmd: execute()
    Cmd->>Cmd: read memberId, ISBN from stdin
    Cmd->>Svc: issueBook(memberId, isbn)

    Svc->>MR: findById(memberId)
    MR-->>Svc: Member

    alt Member not found
        Svc-->>Cmd: IllegalArgumentException
    else Member inactive
        Svc-->>Cmd: IllegalStateException
    end

    Svc->>FR: getTotalUnpaidByMemberId(memberId)
    FR-->>Svc: unpaidAmount

    alt unpaidAmount > ₹50
        Svc-->>Cmd: IllegalStateException "Clear fines first"
    end

    Svc->>BR: findByIsbn(isbn)
    BR-->>Svc: Book

    alt Book not found
        Svc-->>Cmd: IllegalArgumentException
    end

    Svc->>BRR: findActiveByMemberId(memberId)
    BRR-->>Svc: activeLoans[]

    alt activeLoans.size >= borrowLimit
        Svc-->>Cmd: IllegalStateException "Limit reached"
    end

    alt availableCopies <= 0
        Svc-->>Cmd: IllegalStateException "No copies"
    end

    Note over Svc: Create BorrowRecord<br/>issueDate = today<br/>dueDate = today + loanPeriod

    Svc->>BRR: save(record)
    BRR-->>Svc: saved record

    Svc->>BR: update(book) [availableCopies - 1]
    BR-->>Svc: done

    Svc-->>Cmd: BorrowRecord
    Cmd-->>CLI: display success
    CLI-->>Librarian: "Book issued! Due: {date}"
```

### 3.2 Return Book Flow

```mermaid
sequenceDiagram
    actor Librarian
    participant CLI as LibraTrackApp
    participant Cmd as ReturnBookCommand
    participant Svc as BorrowService
    participant BR as BookRepository
    participant BRR as BorrowRecordRepository
    participant MR as MemberRepository
    participant FC as FineCalculator
    participant FR as FineRepository
    participant RN as ReservationNotifier

    Librarian->>CLI: selects "7. Return Book"
    CLI->>Cmd: execute()
    Cmd->>Cmd: read memberId, ISBN from stdin
    Cmd->>Svc: returnBook(memberId, isbn)

    Svc->>BR: findByIsbn(isbn)
    BR-->>Svc: Book

    Svc->>BRR: findActiveByBookAndMember(bookId, memberId)
    BRR-->>Svc: BorrowRecord

    Note over Svc: returnDate = today<br/>isReturned = true

    Svc->>BRR: update(record)
    Svc->>BR: update(book) [availableCopies + 1]

    Note over Svc: daysOverdue = today - dueDate

    alt daysOverdue > 0
        Svc->>MR: findById(memberId)
        MR-->>Svc: Member

        Note over Svc,FC: Strategy Pattern:<br/>Student → ₹2/day<br/>Faculty → ₹5/day

        Svc->>FC: new FineCalculator(memberType)
        Svc->>FC: calculate(daysOverdue)
        FC-->>Svc: fineAmount

        Svc->>FR: save(new Fine)
        FR-->>Svc: Fine
    end

    Note over RN: Observer Pattern:<br/>Notify first pending reservation

    Svc->>RN: notifyBookAvailable(bookId, title)
    RN-->>Svc: done

    Svc-->>Cmd: ReturnResult(record, fine, days)

    alt daysOverdue > 0
        Cmd-->>CLI: "Overdue by X days. Fine: ₹Y"
    else On time
        Cmd-->>CLI: "Returned on time. No fine."
    end

    CLI-->>Librarian: display result
```

### 3.3 Reserve Book Flow

```mermaid
sequenceDiagram
    actor Librarian
    participant CLI as LibraTrackApp
    participant Cmd as ReserveBookCommand
    participant Svc as BorrowService
    participant MR as MemberRepository
    participant BR as BookRepository
    participant RR as ReservationRepository

    Librarian->>CLI: selects "8. Reserve Book"
    CLI->>Cmd: execute()
    Cmd->>Cmd: read memberId, ISBN from stdin
    Cmd->>Svc: reserveBook(memberId, isbn)

    Svc->>MR: findById(memberId)
    MR-->>Svc: Member

    alt Member not found
        Svc-->>Cmd: IllegalArgumentException
    end

    Svc->>BR: findByIsbn(isbn)
    BR-->>Svc: Book

    alt Book not found
        Svc-->>Cmd: IllegalArgumentException
    end

    alt availableCopies > 0
        Svc-->>Cmd: IllegalStateException "Book available — issue directly"
    end

    Note over Svc: Create Reservation<br/>bookId, memberId<br/>reservedAt = now

    Svc->>RR: save(reservation)
    RR-->>Svc: saved Reservation

    Svc-->>Cmd: Reservation
    Cmd-->>CLI: "Reservation created! ID: X"
    CLI-->>Librarian: "You'll be notified when available"
```
