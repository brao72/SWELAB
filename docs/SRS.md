# Software Requirements Specification (SRS)
## LibraTrack — University Library Management System

### 1. Introduction

#### 1.1 Purpose
This document specifies the software requirements for LibraTrack, a CLI-based university library management system. It serves as the primary reference for system design, implementation, and testing.

#### 1.2 Scope
LibraTrack manages book cataloging, member registration, borrowing/returning workflows, fine calculation, and book reservations for a university library. The system is accessed via a Command Line Interface (CLI) and persists data in a PostgreSQL database.

#### 1.3 Definitions and Acronyms
| Term | Definition |
|------|-----------|
| Member | A registered library user (Student or Faculty) |
| Librarian | An admin user who manages books and members |
| Borrowing | The act of checking out a book from the library |
| Reservation | A hold placed on a currently unavailable book |
| Fine | A monetary penalty for overdue book returns |

#### 1.4 References
- IEEE 830-1998 (SRS Standard)
- Gang of Four — Design Patterns: Elements of Reusable Object-Oriented Software

---

### 2. Overall Description

#### 2.1 Product Perspective
LibraTrack is a standalone system with a CLI front-end and PostgreSQL back-end. It does not integrate with external systems in its current version.

```
┌──────────┐     ┌──────────────┐     ┌────────────┐
│   CLI    │────▶│  Application │────▶│ PostgreSQL │
│ (User)   │◀────│    Layer     │◀────│  Database  │
└──────────┘     └──────────────┘     └────────────┘
```

#### 2.2 User Classes and Characteristics

| User Class | Description | Privileges |
|-----------|-------------|------------|
| Student | Undergraduate/postgraduate library member | Borrow up to 3 books, 14-day loan period |
| Faculty | Teaching/research staff | Borrow up to 5 books, 30-day loan period |
| Librarian | Library administrator | Full CRUD on books and members, issue/return processing |

#### 2.3 Operating Environment
- **Runtime:** Java 17+
- **Database:** PostgreSQL 14+
- **OS:** Cross-platform (Windows, macOS, Linux)
- **Build Tool:** Maven

#### 2.4 Constraints
- Single-library deployment (no multi-branch support)
- CLI-only interaction (no GUI/web interface in v1)
- Single-user session (no concurrent CLI sessions)

#### 2.5 Assumptions and Dependencies
- PostgreSQL server is running and accessible on localhost
- Java 17+ is installed
- Maven is installed for building the project

---

### 3. Functional Requirements

#### 3.1 Book Management

| ID | Requirement | Priority |
|----|------------|----------|
| FR-B01 | System shall allow librarians to add a new book (title, author, ISBN, genre, quantity) | High |
| FR-B02 | System shall allow searching books by title, author, ISBN, or genre | High |
| FR-B03 | System shall allow librarians to update book details | Medium |
| FR-B04 | System shall allow librarians to remove a book from the catalog | Low |
| FR-B05 | System shall track available copies vs total copies for each book | High |

#### 3.2 Member Management

| ID | Requirement | Priority |
|----|------------|----------|
| FR-M01 | System shall allow librarians to register a new member (Student or Faculty) | High |
| FR-M02 | System shall enforce borrowing limits based on member type | High |
| FR-M03 | System shall allow librarians to view member details and borrowing history | Medium |
| FR-M04 | System shall allow librarians to deactivate a member account | Low |

#### 3.3 Borrowing and Returning

| ID | Requirement | Priority |
|----|------------|----------|
| FR-BR01 | System shall allow issuing a book to a member if copies are available and borrowing limit is not reached | High |
| FR-BR02 | System shall record the issue date and calculate the due date based on member type | High |
| FR-BR03 | System shall allow returning a book and update available copies | High |
| FR-BR04 | System shall calculate and apply fines for overdue returns | High |
| FR-BR05 | System shall prevent issuing to members with unpaid fines exceeding a threshold | Medium |

#### 3.4 Reservations

| ID | Requirement | Priority |
|----|------------|----------|
| FR-R01 | System shall allow members to reserve a book that is currently unavailable | Medium |
| FR-R02 | System shall notify (via CLI message on next login) when a reserved book becomes available | Medium |
| FR-R03 | System shall auto-cancel a reservation if not claimed within 3 days of notification | Low |

#### 3.5 Fine Management

| ID | Requirement | Priority |
|----|------------|----------|
| FR-F01 | System shall calculate fines at ₹2/day for students and ₹5/day for faculty | High |
| FR-F02 | System shall allow librarians to record fine payments | Medium |
| FR-F03 | System shall display outstanding fines for a member | Medium |

---

### 4. Non-Functional Requirements

| ID | Requirement | Category |
|----|------------|----------|
| NFR-01 | System shall respond to any CLI command within 2 seconds | Performance |
| NFR-02 | System shall use connection pooling for database access | Performance |
| NFR-03 | System shall hash librarian passwords before storage | Security |
| NFR-04 | System shall validate all user inputs before processing | Reliability |
| NFR-05 | System shall log all borrow/return transactions | Auditability |
| NFR-06 | System shall gracefully handle database connection failures | Reliability |

---

### 5. Use Cases

#### UC-01: Issue a Book
- **Actor:** Librarian
- **Preconditions:** Member is registered, book exists with available copies, member has not exceeded borrowing limit, member has no excessive unpaid fines
- **Main Flow:**
  1. Librarian selects "Issue Book"
  2. Librarian enters member ID and book ISBN
  3. System validates member eligibility
  4. System validates book availability
  5. System creates a borrowing record with issue date and due date
  6. System decrements available copies
  7. System confirms successful issue
- **Alternate Flow:**
  - 3a. Member not found → display error
  - 3b. Borrowing limit reached → display error
  - 4a. No copies available → offer reservation
- **Postconditions:** Borrowing record created, available copies decremented

#### UC-02: Return a Book
- **Actor:** Librarian
- **Preconditions:** Active borrowing record exists
- **Main Flow:**
  1. Librarian selects "Return Book"
  2. Librarian enters member ID and book ISBN
  3. System locates the borrowing record
  4. System calculates if return is overdue
  5. If overdue, system calculates and records fine
  6. System marks borrowing record as returned
  7. System increments available copies
  8. System checks and fulfills any pending reservations
- **Postconditions:** Book returned, fine applied if overdue, reservation notified if applicable

#### UC-03: Search for a Book
- **Actor:** Librarian / Member
- **Main Flow:**
  1. User selects "Search Book"
  2. User enters search criteria (title/author/ISBN/genre)
  3. System queries catalog and displays matching results with availability

#### UC-04: Reserve a Book
- **Actor:** Member (via Librarian)
- **Preconditions:** Book exists but has no available copies
- **Main Flow:**
  1. Librarian selects "Reserve Book"
  2. Librarian enters member ID and book ISBN
  3. System creates reservation record
  4. System confirms reservation
- **Postconditions:** Reservation recorded, member will be notified when book is available

---

### 6. Data Requirements

#### 6.1 Core Entities
- **Book:** id, title, author, isbn (unique), genre, total_copies, available_copies
- **Member:** id, name, email, phone, member_type (STUDENT/FACULTY), is_active, created_at
- **Librarian:** id, name, email, password_hash
- **BorrowRecord:** id, book_id, member_id, issue_date, due_date, return_date, is_returned
- **Reservation:** id, book_id, member_id, reserved_at, notified, fulfilled
- **Fine:** id, borrow_record_id, member_id, amount, is_paid, created_at
