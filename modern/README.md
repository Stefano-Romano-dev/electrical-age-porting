# Electrical Age — NeoForge port

Clean port of Electrical Age 1.24.8 to Minecraft 1.21.1 and NeoForge.

## Current baseline

- Minecraft 1.21.1
- NeoForge 21.1.250
- ModDevGradle 2.0.146
- Java 21
- Kotlin 2.4.20 (JVM, without KotlinForForge)
- Mod id `eln`
- Version `0.1.0-alpha.1`

Milestone M0 provides the verified NeoForge walking skeleton. Milestone M1 is now porting the platform-independent simulation core; its first DC MNA slice includes states, resistors, current sources and voltage sources. Kotlin stdlib and Commons Math are bundled through NeoForge Jar-in-Jar. The original 1.7.10 source remains in `../original/` as a read-only behavioral and visual reference.

From this directory on Windows:

```powershell
.\gradlew.bat test
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat runServer
```

Project-wide porting documentation starts at [`../docs/porting/README.md`](../docs/porting/README.md).
