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
| Solver MNA | `mods.eln.sim.mna` | `mods.eln.sim.mna` Kotlin, core indipendente | in porting | Slice DC e lifecycle base RootSystem verificati; restano dinamica, linee e inter-system |

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
| linee e inter-system | da portare | in porting | prossimo slice; connettività avanzata non ancora implementata |
| condensatore, induttore, trasformatore e delay | da portare | mappato | transitori e parità temporale |
| Simulazione termica | `mods.eln.sim` | core indipendente | da analizzare | Dopo baseline elettrica |
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
