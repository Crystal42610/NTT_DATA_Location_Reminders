# Submission and evaluation notes

## Challenge coverage

- Select a location on an OpenStreetMap map while creating a Kotlin memo; save exact latitude/longitude in Room.
- Register an `ENTER` geofence with a **200-metre** radius using Google Play services.
- On entry, send a notification with a status-bar icon, memo title, and first **140 Unicode characters** of its description.
- Handle events in a receiver when the Activity is not visible; restore pending registrations on boot and foreground return.
- Keep map and geofencing providers behind project-owned interfaces (`MapPicker`, `GeofenceRegistrar`).
- Protect duplicate deliveries through an atomic SQL claim, and preserve that state when completing a memo.

## Tests and code quality

- `app/src/test/` contains JVM tests for point validation, geofence IDs/radius and Unicode previews.
- `MemoDaoIntegrationTest` exercises Room one-shot notification claims, completion race behaviour, and persisted coordinates on Android.
- `scripts/verify_windows.bat` performs an Android debug build, JVM tests, and Android lint (requires SDK + Gradle download).
- See `docs/ENGINEERING_NOTES.md` for design trade-offs and emulator scenarios.

## Execution limitations

The full Android Gradle build, lint and emulator integration tests **could not be run in the isolated preparation workspace** (no Android SDK, and Gradle download blocked by networking). The release should not be called device-verified until those commands and at least one geofence test pass on an Android Studio setup. This package includes the original Gradle wrapper, build files and documentation, and contains no local machine path or credential.

## Reviewer workflow

Unzip, open the root folder (`settings.gradle`) in Android Studio, sync/install prompted SDK components, use a Google Play-enabled emulator, grant precise and background location plus notification permissions, save a memo with a map location, and simulate a transition from outside to inside the 200-metre radius.
