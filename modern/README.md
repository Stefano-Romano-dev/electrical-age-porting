# Electrical Age — NeoForge port

Modern implementation of the Electrical Age 1.24.8 port. The repository overview is available in [English](../README.md) and [Italian](../README.it.md).

## Current baseline

- Minecraft 1.21.1
- NeoForge 21.1.250
- ModDevGradle 2.0.146
- Java 21
- Kotlin 2.4.20 (JVM, without KotlinForForge)
- Mod id `eln`
- Version `0.1.0-alpha.1`

Milestones M0, M1, and M2 are complete. M3 has started with a typed, server-authoritative electrical-source configuration payload and the first localized menu. The baseline passes 171 unit tests and 11 GameTests, including a real dedicated-to-client `50 → 123.5 V` configuration probe. The complete catalog, remaining GUIs and tools, audio, and integrations remain in development.

From this directory on Windows:

```powershell
$env:GRADLE_USER_HOME = (Resolve-Path '.gradle').Path
.\gradlew.bat test
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat runServer
```

Project-wide porting documentation starts at [`../docs/porting/README.md`](../docs/porting/README.md).
