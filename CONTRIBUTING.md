# Contributing to CarFlow

Thank you for considering contributing to CarFlow! This document provides guidelines and information for contributors.

## How to Contribute

### Reporting Bugs
- Use the [Bug Report template](.github/ISSUE_TEMPLATE/bug_report.md)
- Include device info, Android version, steps to reproduce
- Attach logs (logcat) if possible

### Suggesting Features
- Use the [Feature Request template](.github/ISSUE_TEMPLATE/feature_request.md)
- Describe the problem it solves
- Consider if it fits the scope (car expense tracking)

### Pull Requests
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit changes (`git commit -m 'Add AmazingFeature'`)
4. Push to branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request using the template

### Code Style
- Kotlin code must pass `./gradlew ktlintCheck`
- Follow existing patterns (MVVM, Repository, Hilt)
- Keep modules separated: `app/` for UI, `shared/parser` for NLP logic

### Testing
- Add tests for new features (JUnit for parser, Espresso for UI)
- Ensure existing tests pass: `./gradlew test`

## Development Setup

1. Install Android SDK (API 34) and JDK 17
2. Clone and open in Android Studio or VS Code with Android extensions
3. Create `local.properties` with `sdk.dir=/path/to/sdk`
4. Build: `./gradlew assembleDebug`

## Project Structure
```
app/              - Android application (Compose UI, Room DB)
shared/parser/   - Kotlin JVM library (NLP parsing)
```

## Questions?
Open an issue or reach out at [GitHub Discussions](https://github.com/yourusername/carflow/discussions)
