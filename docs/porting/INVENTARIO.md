# Inventario legacy -> moderno

Questo documento diventerà la fonte autorevole per sapere cosa esiste nella 1.24.8, cosa viene portato, con quale id e con quale stato.

## Stati ammessi

- `da analizzare`
- `mappato`
- `in porting`
- `implementato`
- `verificato`
- `rimandato`
- `escluso`, sempre con motivazione

## Sistemi principali

| Sistema legacy | Posizione originale | Destinazione prevista | Stato | Note |
|---|---|---|---|---|
| Solver MNA | `mods.eln.sim.mna` | `mods.eln.sim.mna` Kotlin, core indipendente | implementato, parità parziale | 30/30 tipi presenti; persistenza e chiamanti reali ancora da verificare, vedi `MNA_AUDIT.md` |

## Solver MNA

| Elemento legacy | Destinazione moderna | Stato | Verifica |
|---|---|---|---|
| `SubSystem` | `mods.eln.sim.mna.SubSystem` | verificato, slice DC | QR/solve/step/singolarità |
| `State`, `VoltageState`, `CurrentState` | stesso package logico | verificato | conteggio stati e valori risolti |
| `Component`, `Bipole` | stesso package logico | verificato, API minima | connessioni e invalidazione |
| `Resistor` | `component.Resistor` | verificato | tensione, corrente, potenza, cambio resistenza |
| `CurrentSource` | `component.CurrentSource` | verificato | esempio 0,01 A / 10 Ω |
| `VoltageSource` | `component.VoltageSource` | verificato | esempio 1 V / 10 Ω e partitore |
| `RootSystem` | `mods.eln.sim.mna.RootSystem` | verificato, lifecycle base | generazione, break/rebuild, confini privati, processi e rimozione |
| `Line`, `VoltageStateLineReady` | stesso package logico | verificato | compressione, resistenza equivalente, flush stati intermedi e ripristino |
| `InterSystem`, `InterSystemAbstraction` | stesso package logico | verificato | split oltre 100 stati, Thévenin, convergenza e distruzione |
| `Capacitor` | stesso package logico | verificato, core | energia e traiettoria RC; corrente zero legacy conservata |
| `Inductor` | stesso package logico | verificato, core | energia, reset e traiettoria RL; adapter di persistenza ancora da integrare |
| `Delay` | stesso package logico | verificato, core | primi campioni `oldIa`/`oldIb` conservati |
| `ResistorSwitch` | stesso package logico | verificato, core | stato, resistenza base/off, alta impedenza e restore persistente puro |
| `Transformer` | stesso package logico | verificato, core | rapporto, stati di corrente e comportamento di invalidazione legacy |
| `PowerSource`, `PowerSourceBipole` | component/process moderni puri | verificato, core | Thévenin, limiti, potenza e fallback NaN; adapter NBT da integrare |
| `TransformerInterSystemProcess` | stesso package logico | verificato, core | accoppiamento Thévenin e rapporto |
| `Monopole`, `SubSystemDebugSnapshot` | stesso package logico | verificato | connessione legacy e snapshot diagnostico distaccato dalla matrice viva |
| `ElectricalLoad`, `ElectricalConnection` | `mods.eln.sim` | verificato, core | resistenze seriali, notifica del bridge e convenzione corrente dimezzata |
| `ThermalLoad`, `ThermalConnection`, `ThermalResistor` | `mods.eln.sim` | verificato, core | trasferimenti, segni, accumulatori, coordinate e flag fast/slow |
| `Simulator.thermalStep` | `ThermalSimulator` + `ThermalAmbientExchange` | verificato, core | ordine connessioni/processi/ambiente/integrazione e reset accumulatori |
| Processi heater base | `mods.eln.sim.process.heater` | verificato, core | resistore, diodo e resistenza seriale di `ElectricalLoad` |
| Simulazione termica | `mods.eln.sim` | core indipendente + adapter ambiente | implementato, parità parziale | Scheduler e processi superiori restano aperti |
| SixNode | `mods.eln.node.six` | block entity host + component registry | mappato | Priorità M2 |
| TransparentNode | `mods.eln.node.transparent` | block/block entity moderni | da analizzare | Port per famiglie |
| SimpleNode | `mods.eln.node.simple`, `simplenode` | blocchi/capability moderni | da analizzare | Include integrazioni |
| GridNode | `mods.eln.gridnode` | rete/multiblocco | rimandato | M5 |
| Ghost system | `mods.eln.ghost` | multiblocco moderno da progettare | rimandato | M5 |
| GUI | `mods.eln.gui` | MenuType + Screen | mappato | M3 |
| Packet legacy | `mods.eln.packets`, `PacketHandler` | CustomPacketPayload | mappato | M3 |
| Worldgen minerali | `mods.eln.ore` | configured/placed features data-driven | da analizzare | M5 |
| API elettrica v1 | `mods.eln.api.v1.electrical` | specifica per API moderna | da analizzare | Nessuna compatibilità binaria promessa |

## Prima vertical slice

| Contenuto | Id legacy | Id moderno proposto | Stato | Test richiesti |
|---|---:|---|---|---|
| Host SixNode | da rilevare | `eln:six_node` | mappato | più facce, save/reload, chunk reload |
| Cavo base | da rilevare | da decidere | da analizzare | connettività e drop |
| Resistore base | da rilevare | da decidere | da analizzare | resistenza, potenza e orientamento |
| Sorgente DC | da rilevare | da decidere | da analizzare | polarità e tensione |

## Regola per gli id

Prima di registrare un contenuto moderno, annotare:

- numeric id/damage legacy;
- nome descriptor originale;
- UUID/tag NBT originale, se presente;
- id moderno;
- classe logica e renderer;
- asset associati;
- compatibilità desiderata;
- test di equivalenza.

Non riutilizzare lo stesso id moderno per contenuti legacy semanticamente diversi solo perché condividono un item contenitore.
