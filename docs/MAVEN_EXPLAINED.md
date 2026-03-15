# Understanding Maven — Through LibraTrack

## What is Maven?

Maven is a **build automation and dependency management tool** for Java projects. Think of it like a smart assistant that:

1. Downloads libraries your project needs (dependencies)
2. Compiles your Java code
3. Runs your tests
4. Packages everything into a JAR file you can distribute

Without Maven, you'd manually download `.jar` files, add them to your classpath, and write shell scripts to compile — Maven handles all of this with one command.

---

## The pom.xml — Maven's Brain

`pom.xml` (Project Object Model) is the single configuration file that tells Maven everything about your project. Let's break down LibraTrack's `pom.xml` section by section.

### 1. Project Identity (GAV Coordinates)

```xml
<groupId>com.libratrack</groupId>
<artifactId>libratrack</artifactId>
<version>1.0-SNAPSHOT</version>
<packaging>jar</packaging>
```

Every Maven project is uniquely identified by three values (called **GAV**):

| Field | Purpose | Our Value |
|---|---|---|
| **groupId** | Organization/team (like a package name) | `com.libratrack` |
| **artifactId** | Project name | `libratrack` |
| **version** | Release version | `1.0-SNAPSHOT` |

- `SNAPSHOT` means "this is still in development, not a final release"
- `packaging: jar` means Maven will produce a `.jar` file as output

These same three values are how you reference *other* projects as dependencies.

### 2. Properties

```xml
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```

| Property | What it does |
|---|---|
| `maven.compiler.source` | Java version of your source code (we write Java 17) |
| `maven.compiler.target` | Java version for the compiled bytecode (runs on Java 17+) |
| `project.build.sourceEncoding` | Character encoding (UTF-8 handles all languages/symbols like ₹) |

### 3. Dependencies — The Core Power of Maven

```xml
<dependencies>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <version>42.7.1</version>
    </dependency>
    ...
</dependencies>
```

Instead of manually downloading JAR files, you just declare what you need. Maven automatically:
1. Downloads it from **Maven Central** (a public repository of Java libraries)
2. Stores it in `~/.m2/repository/` (your local cache)
3. Adds it to your project's classpath

#### LibraTrack's Dependencies Explained

| Dependency | Why we need it | Scope |
|---|---|---|
| `postgresql` | JDBC driver to connect Java to PostgreSQL database | **compile** (default) — included in final JAR |
| `HikariCP` | Fast database connection pooling (used by `DatabaseConnection.java`) | **compile** (default) |
| `junit-jupiter` | Testing framework to write and run unit tests | **test** — only available during testing, NOT in final JAR |
| `mockito-core` | Mocking library to fake repositories in service tests | **test** |
| `mockito-junit-jupiter` | Integrates Mockito with JUnit 5 (`@ExtendWith`, `@Mock`) | **test** |

#### Dependency Scopes

```xml
<scope>test</scope>   <!-- Only available in src/test/, not shipped in JAR -->
```

| Scope | Available during compile? | Available during test? | Included in JAR? |
|---|---|---|---|
| **compile** (default) | Yes | Yes | Yes |
| **test** | No | Yes | No |
| **provided** | Yes | Yes | No (server provides it) |
| **runtime** | No | Yes | Yes |

### 4. Build Plugins

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-jar-plugin</artifactId>
            <version>3.3.0</version>
            <configuration>
                <archive>
                    <manifest>
                        <mainClass>com.libratrack.cli.LibraTrackApp</mainClass>
                    </manifest>
                </archive>
            </configuration>
        </plugin>
    </plugins>
</build>
```

This configures the JAR plugin to set the **main class** in the JAR's manifest. Without this, running `java -jar libratrack-1.0-SNAPSHOT.jar` wouldn't know which class has the `main()` method to start.

---

## Maven Project Structure

Maven enforces a **standard directory layout** — every Maven project follows this:

```
LibraTrack/
├── pom.xml                          # Maven config (the brain)
├── src/
│   ├── main/
│   │   ├── java/                    # Your application source code
│   │   │   └── com/libratrack/
│   │   │       ├── cli/             # CLI entry point
│   │   │       ├── model/           # Domain entities
│   │   │       ├── service/         # Business logic
│   │   │       ├── repository/      # Data access interfaces + impl/
│   │   │       ├── command/         # Command pattern classes
│   │   │       ├── factory/         # Factory pattern
│   │   │       ├── strategy/        # Strategy pattern
│   │   │       ├── observer/        # Observer pattern
│   │   │       └── db/              # Database connection (Singleton)
│   │   └── resources/               # Non-code files (config, SQL)
│   │       ├── application.properties
│   │       └── schema.sql
│   └── test/
│       ├── java/                    # Test source code (mirrors main/)
│       │   └── com/libratrack/
│       │       ├── model/           # Model tests
│       │       ├── factory/         # Factory tests
│       │       ├── strategy/        # Strategy tests
│       │       ├── observer/        # Observer tests
│       │       └── service/         # Service tests (with Mockito)
│       └── resources/               # Test-only config files
└── target/                          # OUTPUT — Maven puts compiled code & JAR here
    ├── classes/                     # Compiled .class files
    ├── test-classes/                # Compiled test .class files
    └── libratrack-1.0-SNAPSHOT.jar  # The final packaged JAR
```

Key rules:
- `src/main/java/` — your application code goes here
- `src/test/java/` — your test code goes here (mirrors the main structure)
- `src/main/resources/` — config files, SQL scripts
- `target/` — generated by Maven (never edit manually, safe to delete)

---

## Maven Commands — The Build Lifecycle

Maven has a **lifecycle** — a fixed sequence of phases. When you run a phase, all previous phases run first.

```
validate → compile → test → package → verify → install → deploy
```

### Commands You'll Use

| Command | What it does | Phases that run |
|---|---|---|
| `mvn clean` | Deletes the `target/` folder (fresh start) | clean |
| `mvn compile` | Compiles `src/main/java/` into `target/classes/` | validate → compile |
| `mvn test` | Compiles + runs all tests in `src/test/java/` | validate → compile → test |
| `mvn package` | Compiles + tests + creates the JAR in `target/` | validate → compile → test → package |
| `mvn clean package` | Deletes target, then does full build | clean + all above |
| `mvn clean compile` | Fresh compile (no tests, no JAR) | clean + validate → compile |

### How It Works for LibraTrack

```bash
# Step 1: Clean + compile (checks for syntax errors)
mvn clean compile

# Step 2: Run all 67 tests
mvn test

# Step 3: Build the runnable JAR
mvn clean package

# Step 4: Run the application
java -jar target/libratrack-1.0-SNAPSHOT.jar
```

### Useful Flags

```bash
mvn test -Dtest=BorrowServiceTest           # Run only one test class
mvn test -Dtest=BorrowServiceTest#issueBookSuccessfully  # Run one test method
mvn package -DskipTests                      # Build JAR without running tests
mvn dependency:tree                          # Show all dependencies (including transitive ones)
```

---

## How Maven Resolves Dependencies

When you add a dependency, Maven doesn't just download that one JAR. Libraries depend on other libraries (**transitive dependencies**). Maven resolves the full tree:

```
libratrack
├── postgresql:42.7.1
├── HikariCP:5.1.0
│   └── slf4j-api:2.0.x        (HikariCP needs this for logging)
├── junit-jupiter:5.10.1        [test]
│   ├── junit-jupiter-api
│   ├── junit-jupiter-engine
│   └── junit-platform-launcher
└── mockito-core:5.8.0          [test]
    ├── byte-buddy               (for creating mock objects at runtime)
    └── objenesis                (for instantiating mocks without constructors)
```

You can see this tree by running:
```bash
mvn dependency:tree
```

---

## Maven vs Other Tools

| Feature | Maven | Gradle | Manual (no tool) |
|---|---|---|---|
| Config format | XML (`pom.xml`) | Groovy/Kotlin DSL | Shell scripts |
| Dependency management | Automatic | Automatic | Download JARs manually |
| Standard structure | Enforced | Flexible | Whatever you want |
| Learning curve | Medium | Steeper | Low (but painful at scale) |
| Used in industry | Very common | Growing (Android default) | Never for real projects |

---

## Quick Reference

```bash
mvn clean compile       # "Does my code compile?"
mvn test                # "Do my tests pass?"
mvn clean package       # "Give me a runnable JAR"
mvn dependency:tree     # "What libraries am I using?"
```

The `target/` folder is always regenerable — you can delete it anytime and `mvn clean package` will recreate everything.
