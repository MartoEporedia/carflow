# CarFlow

> Track your car expenses effortlessly with NLP-powered input

[![CI](https://github.com/yourusername/carflow/actions/workflows/ci.yml/badge.svg)](https://github.com/yourusername/carflow/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-blue.svg)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-14-green.svg)](https://developer.android.com)

**CarFlow** è un'applicazione Android per tracciare le spese della tua auto in modo intelligente e intuitivo. Utilizza un parser NLP (Natural Language Processing) per interpretare input in linguaggio naturale come "benzina 50€ 30L" o "tagliando 200€" e convertire automaticamente in spese strutturate.

## Features

- **Parser NLP avanzato**: Inserisci spese in testo libero (Italiano/Inglese) - es: "olio 40€", "gomme 300€", "rc auto 120€"
- **Gestione multi-veicolo**: Supporta più veicoli con dati separati
- **Categorizzazione automatica**: Classifica in Carburante, Manutenzione, Extra
- **Database locale**: Room database per privacy e offline-first
- **Statistiche**: Grafici di spesa per categoria, trend mensili, km/litro (in sviluppo)
- **Material3 Design**: UI moderna con Jetpack Compose
- **Kotlin Coroutines**: Architettura reattiva con Flow
- **Test coverage**: Parserlibrary con 95%+ test coverage

## Tech Stack

- **Language**: Kotlin 1.9.22 (Java 17)
- **UI**: Jetpack Compose + Material3 BOM 2024.01.00
- **Architecture**: MVVM + Repository Pattern
- **Database**: Room 2.6.1 + SQLite
- **DI**: Hilt 2.50
- **NLP**: Custom parser con regex e keyword dictionary (IT/EN)
- **Navigation**: Navigation Compose 2.7.6
- **Background**: WorkManager (esteso future)
- **Serialization**: Kotlinx Serialization 1.6.2

## Project Structure

```
carflow/
├── app/                    # Android application
│   ├── data/
│   │   ├── dao/           # Room DAOs (Expense, Vehicle, Category)
│   │   ├── entity/        # Database entities
│   │   ├── repository/    # Repository implementations
│   │   └── database/      # Room database setup
│   ├── di/                # Hilt modules
│   ├── ui/
│   │   ├── navigation/    # Compose Navigation
│   │   ├── screens/       # Feature screens
│   │   └── theme/         # Material3 theming
│   └── CarFlowApplication.kt
├── shared/parser/         # Kotlin JVM library (NLP)
│   ├── pipeline/          # TokenExtractor, DateParser, etc.
│   ├── keywords/          # Italian/English dictionaries
│   └── model/             # ParsedExpense, FuelType, enums
└── build.gradle.kts       # Multi-module Gradle config
```

## Screenshots

_Placeholder - screenshots verranno aggiunti_

| Expense List | Input Parsing | Statistics |
|-------------|---------------|------------|
| coming soon | coming soon   | coming soon |

## Prerequisites

- Android Studio Hedgehog (2023.1.1) o superiore
- JDK 17
- Android SDK con API level 34 (Android 14)
- Gradle 8.5+ (wrapper will be added soon)

## Build & Run

```bash
# Clone the repository
git clone https://github.com/yourusername/carflow.git
cd carflow

# Build the project (requires Gradle installed)
./gradlew assembleDebug  # wrapper coming soon
# or
gradle assembleDebug

# Open in Android Studio and run on emulator/device
```

**Minimum Android version**: API 26 (Android 8.0)

## Testing

```bash
# Run parser library tests
./gradlew :shared:parser:test

# Run instrumented tests (app)
./gradlew :app:connectedAndroidTest

# Run unit tests (app)
./gradlew :app:test
```

## Architecture Highlights

### Expense Parser (shared:parser)

Il cuore dell\'app: converte testo libero in `ParsedExpense` strutturato:

```kotlin
val parser = ExpenseParser()
val result = parser.parse("benzina 50€ 30L")
// result.category = FUEL
// result.amount = 50.0
// result.quantity = 30.0
// result.fuelType = PETROL
```

Vedi `shared/parser/src/test/kotlin/ExpenseParserTest.kt` per 50+ casi di test.

### Data Layer

- **Room DB** con relazioni 1:N (Vehicle → Expenses)
- **Repository pattern** astrae data sources
- **Flow** per streams reattivi
- **Hilt** per dependency injection

## Contributing

We welcome contributions! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## License

MIT License - see [LICENSE](LICENSE) file.

## Roadmap

- [x] Parser NLP (IT/EN)
- [x] Room database + DAOs
- [x] Hilt DI setup
- [x] Compose UI skeletons
- [ ] Full UI implementation (list, input, stats, vehicles)
- [ ] Statistics charts (MPAndroidChart/Charts)
- [ ] Export to CSV/PDF
- [ ] Backup/restore
- [ ] Sync with cloud (optional)
- [ ] Localization (EN/IT/ES/FR)
- [ ] Widget home screen
- [ ] Export to CSV/PDF
- [ ] Multi-currency support
- [ ] Receipt scanning (ML Kit)

## Acknowledgments

Built with ❤️ using Kotlin and Jetpack Compose.

---

**Status**: Early Alpha - Core data layer functional, UI implementation in progress.

**Issues**: Please report bugs or feature requests on [GitHub Issues](https://github.com/yourusername/carflow/issues).
