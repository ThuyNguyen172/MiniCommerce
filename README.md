# MiniCommerce – Fashion E-commerce System

Java Core console project, using in-memory repositories (`HashMap`) and no external dependencies.

## Requirements
- JDK 17 or later (JDK 11 may also work with small adjustments).
- Any Java IDE, or terminal.

## Run
From the project root:

```bash
javac -encoding UTF-8 -d out $(find src/main/java -name "*.java")
java -cp out com.minicommerce.Main
```

On Windows PowerShell, compile the source files through your IDE, or use:

```powershell
Get-ChildItem -Recurse src/main/java -Filter *.java | ForEach-Object { $_.FullName } | Set-Content sources.txt
javac -encoding UTF-8 -d out @sources.txt
java -cp out com.minicommerce.Main
```

> `-encoding UTF-8` is required because source comments contain Vietnamese text.

## Package architecture
- `entity`: domain objects.
- `enums`: fixed values used by entities.
- `service`: business-operation interfaces.
- `service/impl`: business logic and validation.
- `repository`: persistence interfaces.
- `repository/impl`: in-memory implementations.
- `controller`: console flows; delegates business work to services.
- `dto`: request/response objects.
- `exception`: custom business exceptions.
- `util`: input, IDs, and receipt formatting.
- `config`: manual dependency wiring.
- `simulation`: multi-threaded flash-sale simulator (Session 6).

## Demo data
The app seeds a few fashion products and variants. Data is kept in memory, so it resets when the program restarts.

## Scope and notes
- Payments are simulated. No real payment processing occurs.
- This starter focuses on the main flow: browse products, add variants to cart, checkout, cancel eligible orders, and view basic admin reports.
- Passwords are plain strings for learning purposes only. Do not use this authentication approach in a real application.
- `ProductVariant` stock is stored per SKU/size/color combination.

## Learning sessions applied
See `CLAUDE.md` for the full change log.
- **Lambda / Stream API (bổ sung):** search & filter menu (composable `Predicate`s,
  `Comparator` sort options), stream-based sales and low-stock reports, `DiscountPolicy` functional interface.
- **Session 6 – Multi-threading:** thread-safe repositories and cart, lock-protected atomic stock reservation,
  async order notifications, parallel dashboard with `CompletableFuture`, flash-sale simulator.
