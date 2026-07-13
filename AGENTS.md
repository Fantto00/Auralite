# Repository Guidelines

## Project Structure & Module Organization

This is a single-module Android application (`:app`) using Kotlin and Jetpack Compose. Production code is under `app/src/main/java/com/fantto/auralite/`:

- `data/` contains API clients, Room/DataStore persistence, DTOs, and repository implementations.
- `domain/` defines repository contracts, models, engines, and use cases.
- `ui/` contains Compose screens, screen-specific components, navigation, icons, and theme code.
- `di/`, `service/`, and `util/` hold dependency wiring, Android services, and shared platform helpers.

Android resources belong in `app/src/main/res/`; bundled speech assets are in `app/src/main/assets/`. Local unit tests use `app/src/test/`; device and Compose integration tests use `app/src/androidTest/`.

## Build, Test, and Development Commands

Run commands from the repository root (use `gradlew.bat` on Windows):

- `./gradlew assembleDebug`: builds a debug APK.
- `./gradlew testDebugUnitTest`: runs JVM unit tests.
- `./gradlew connectedDebugAndroidTest`: runs instrumentation and Compose tests on a connected emulator or device.
- `./gradlew lint`: runs Android Lint checks.

Use Android Studio for deployment and Compose previews. Do not commit `local.properties`; it contains machine-specific SDK configuration.

## Coding Style & Naming Conventions

Follow existing Kotlin style: four-space indentation, no tabs, braces on the same line, and one primary type per file. Use `PascalCase` for classes, composables, and files; `camelCase` for functions and properties; and `UPPER_SNAKE_CASE` for constants. Keep package placement aligned with responsibility (for example, `ui/screen/chat/ChatViewModel.kt` and `data/remote/api/LlmApiService.kt`). Prefer thin Compose components and move business logic into ViewModels and domain use cases. No dedicated formatter or linter configuration is currently checked in; use Android Studio formatting and run `lint` before review.

## Testing Guidelines

Write JUnit tests as `*Test.kt` under the matching package in `app/src/test`. Put Android-dependent, navigation, and UI behavior tests in `app/src/androidTest`. Add tests for changed use cases, repositories, and ViewModel state where practical; there is no configured coverage threshold. Run the smallest relevant test task locally, then run `testDebugUnitTest` before opening a pull request.

## Commit & Pull Request Guidelines

Recent history uses Conventional Commit-style prefixes, including `feat:`, `fix:`, and `update:`. Write focused, imperative subjects, e.g. `fix: prevent duplicate conversation creation`. Keep unrelated cleanup separate. Pull requests should explain the user-visible or architectural change, link the relevant issue when available, list validation commands, and include screenshots or recordings for Compose UI changes. Call out API, database, permission, or asset changes explicitly.
