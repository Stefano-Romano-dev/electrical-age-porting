# Electrical Age modern port

This directory contains the experimental Electrical Age port for Minecraft
1.21.1 on NeoForge. The original 1.7.10 source remains in `../original` and is
kept unchanged as a reference.

The first milestone provides:

- the `eln` mod entrypoint, an `eln:resistor` component item and a six-face host block;
- English and Italian names plus minimal block assets;
- a loader-independent DC modified-nodal-analysis solver;
- numerical tests for a voltage divider and a floating circuit.

Use a resistor on a solid face to install it. Up to six resistors can share the
same block space. Sneak and use an empty hand on a resistor to remove that face.

Build and test on Windows with:

```powershell
.\gradlew.bat build
```

Launch the development client with:

```powershell
.\gradlew.bat runClient
```
