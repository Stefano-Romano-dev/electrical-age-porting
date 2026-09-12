# Audit di parità del package MNA

Data audit: 12 settembre 2026

Riferimento: `original/src/main/java/mods/eln/sim/mna` della release 1.24.8.

## Copertura dei tipi

- Tipi Java legacy rilevati: 30.
- Tipi Kotlin moderni corrispondenti: 30.
- Assenze chiuse durante l'audit: `Monopole` e `SubSystemDebugSnapshot`.
- Dipendenze Minecraft/NeoForge nel core moderno: nessuna.

| Area | Tipi coperti | Stato |
|---|---|---|
| Root e sottosistemi | `RootSystem`, `SubSystem`, `SubSystemDebugSnapshot` | implementato e coperto da fixture |
| Stati | `State`, `VoltageState`, `CurrentState`, `VoltageStateLineReady` | implementato e coperto da fixture |
| Componenti base | `Component`, `Bipole`, `Monopole`, `IAbstractor` | implementato e coperto da fixture |
| Componenti DC | `Resistor`, `CurrentSource`, `VoltageSource`, `ResistorSwitch` | implementato e coperto da fixture |
| Componenti dinamici | `Capacitor`, `Inductor`, `Delay` | implementato e coperto da fixture temporali |
| Trasformazione e potenza | `Transformer`, `PowerSource`, `PowerSourceBipole`, `TransformerInterSystemProcess` | implementato e coperto da fixture |
| Astrazioni di rete | `Line`, `InterSystem`, `InterSystemAbstraction` | implementato e coperto da fixture |
| Processi e costanti | quattro interfacce process/lifecycle e `MnaConst` | implementato |

## Semantiche legacy confermate

- Il vettore RHS usa assegnazione e non accumulo: P-008.
- `Transformer.setRatio` non invalida la matrice: P-010.
- Un componente rimosso ma ancora collegato può essere riscoperto: P-011.
- `Bipole.breakConnection` rimuove le registrazioni dagli stati ma conserva i riferimenti ai pin.
- Uno stesso componente può comparire due volte nella lista di uno stato, per esempio quando entrambi i pin coincidono.
- `CurrentSource` registra il processo senza impostare l'appartenenza locale al sottosistema, come nella classe legacy.
- `Capacitor.current` resta sempre zero e il parametro storico mantiene il nome `coulombs`.
- `Delay` conserva l'accumulo di `oldIa` e `oldIb`.
- Gli errori durante l'inversione QR rendono il sottosistema singolare, come nel catch generale originale.

## Schemi persistenti collegati agli adapter moderni

Il core non importa NBT. I codec esterni conservano queste chiavi legacy e sono coperti con il vero `CompoundTag`; l'integrazione nei block entity e le prove save/reload restano a M2.

| Tipo | Chiavi legacy, dopo il prefisso | Stato moderno |
|---|---|---|
| `VoltageSource` | `<name>U`, `<name>Istate` | codec implementato e testato |
| `CurrentSource` | `<name>I` | codec implementato e testato |
| `Inductor` | `<name>Istate` | codec implementato e testato |
| `ResistorSwitch` | `<name>R`, `<name>State` | codec e riparazione restore testati |
| `PowerSource` | chiavi `VoltageSource` più `<name>P`, `<name>Umax`, `<name>Imax` | codec implementato e testato |
| `PowerSourceBipole` | `P`, `Umax`, `Imax` dopo il prefisso | codec implementato e testato |

La compatibilità diretta dei mondi 1.7.10 resta esclusa dal primo port, ma lo schema non deve andare perso.

## Criteri ancora aperti

Il package MNA puro ha corrispondenza di tipi e copertura numerica/strutturale. Prima di dichiarare completa la parità dell'intero sistema elettrico restano:

- integrazione dei codec nei block entity e prove save/reload;
- audit dei chiamanti in `mods.eln.sim` per ordine di registrazione, teardown e aggiornamenti dinamici;
- fixture ricavate dai dispositivi reali, non soltanto da circuiti sintetici;
- verifica delle prestazioni e della convergenza su reti rappresentative della 1.24.8.
