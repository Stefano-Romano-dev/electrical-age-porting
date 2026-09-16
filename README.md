# Electrical Age — NeoForge port

[Italiano](README.it.md) · **English**

This project is a modern port of **Electrical Age 1.24.8** from Minecraft Forge 1.7.10 to **Minecraft 1.21.1**, **NeoForge**, and **Java 21**.

The goal is to preserve the original mod's electrical simulation, gameplay, progression, timing, and visual identity while replacing the legacy platform integration with modern APIs. The original source is kept in [`original/`](original/) as a read-only behavioral and visual reference; all new implementation lives in [`modern/`](modern/).

## Current status

The project is in active development and is not yet a complete playable release.

Currently working:

- platform-independent electrical and thermal simulation core;
- MNA solver, scheduler, persistence, and server lifecycle;
- first playable SixNode vertical slice;
- low-voltage cables, electrical source, and power resistor;
- multi-face placement, removal, drops, save/reload, and chunk reload;
- original OBJ models, textures, item variants, and cable rendering;
- coplanar, internal, and outer-corner cable connections.
- dedicated-to-client synchronization for the M2 SixNode slice.
- first server-authoritative configuration menu for the electrical source.

The current baseline passes **171 unit tests** and **11 NeoForge GameTests**, including dedicated-server, persistence, and real client/server configuration checks.

Still in progress: the full device catalog, configuration GUIs and inventories, audio and particles, multiplayer validation for future interactive payloads, and optional integrations with other mods.

## Development

On Windows, from [`modern/`](modern/):

```powershell
$env:GRADLE_USER_HOME = (Resolve-Path '.gradle').Path
.\gradlew.bat test
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat runServer
```

See [`STATO_PORTING.md`](STATO_PORTING.md) for the current dashboard and [`docs/porting/README.md`](docs/porting/README.md) for the complete technical documentation.

## License

See [`modern/LICENSE.md`](modern/LICENSE.md) and the original project's [`original/LICENSE.md`](original/LICENSE.md).
