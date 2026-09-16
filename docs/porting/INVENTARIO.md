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
| `Inductor` | stesso package logico | verificato, core + codec | energia, reset, traiettoria RL e chiave `Istate`; composizione block entity in M2 |
| `Delay` | stesso package logico | verificato, core | primi campioni `oldIa`/`oldIb` conservati |
| `ResistorSwitch` | stesso package logico | verificato, core | stato, resistenza base/off, alta impedenza e restore persistente puro |
| `Transformer` | stesso package logico | verificato, core | rapporto, stati di corrente e comportamento di invalidazione legacy |
| `PowerSource`, `PowerSourceBipole` | component/process moderni puri | verificato, core + codec | Thévenin, limiti, potenza, fallback NaN e campi NBT legacy |
| `TransformerInterSystemProcess` | stesso package logico | verificato, core | accoppiamento Thévenin e rapporto |
| `Monopole`, `SubSystemDebugSnapshot` | stesso package logico | verificato | connessione legacy e snapshot diagnostico distaccato dalla matrice viva |
| `ElectricalLoad`, `ElectricalConnection` | `mods.eln.sim` | verificato, core | resistenze seriali, notifica del bridge e convenzione corrente dimezzata |
| `ThermalLoad`, `ThermalConnection`, `ThermalResistor` | `mods.eln.sim` | verificato, core | trasferimenti, segni, accumulatori, coordinate e flag fast/slow |
| `Simulator.thermalStep` | `ThermalSimulator` + `ThermalAmbientExchange` | verificato, core | ordine connessioni/processi/ambiente/integrazione e reset accumulatori |
| Processi heater base | `mods.eln.sim.process.heater` | verificato, core | resistore, diodo e resistenza seriale di `ElectricalLoad` |
| `Simulator` | `mods.eln.sim.Simulator` puro + `ServerSimulationLifecycle` | verificato, scheduler + lifecycle server | sequenza multi-rate, owner per identità server, avvio dedicated e handler start/tick/stop |
| Inizializzatori termici | stesso package logico, validator esplicito | verificato, core | formule `Rs`/`Rp`/`C`, copia, applicazione e rifiuto instabile |
| `FurnaceProcess`, `DiodeProcess`, conversione resistiva | stesso package logico | verificato, core | consumo combustibile, clamp gain, segno del diodo e potenza Joule |
| `Integrator`, `Differentiator` | stesso package logico | verificato, core | sequenze campione-per-campione e reset legacy |
| `FunctionTable`, `FunctionTableYProtect` | `mods.eln.misc` puro | verificato, core | interpolazione, estrapolazione, clamp, duplicazione e cache scale legacy |
| `BatteryProcess`, `BatterySlowProcess` | `mods.eln.sim` + `BatteryAgingPolicy` | verificato, core | carica/scarica, calore di ricarica, energia a 50 campioni, vita, aging e distruzione astratta |
| `RegulatorProcess` e adapter termici | `mods.eln.sim` | verificato, core | None/Manual/OnOff/Analog, reset guadagni, clamp e soglie resistenza |
| Persistenza batteria | `BatteryState` + `BatteryProcessTagCodec` | verificato | suffissi legacy `NBPQ`, `NBPlife`; valori non finiti riparati come in origine |
| Persistenza regolatore | `RegulatorState` + `RegulatorProcessTagCodec` | verificato | chiavi `prefix + name + errorIntegrated/target`; solo integrale NaN riparato |
| Watchdog elettrici/termici | `mods.eln.sim.process.destruct` + policy/sink | verificato, core | soglie, primo overflow ignorato, timeout casuale, categorie e telemetria trip |
| `DelayedDestruction`, `TimeRemover` | stesso package logico + owner `Simulator` | verificato, core | registrazione, scadenza, autorimozione e callback |
| `WorldExplosion` | adapter mondo NeoForge | rimandato | nessun placeholder: forza, rimozione blocco ed effetti verranno portati con il nodo |
| `ShaftSpeedWatchdog` | futuro core meccanico | rimandato | richiede rete shaft e velocità angolare |
| Simulazione termica | `mods.eln.sim` | core indipendente + adapter ambiente | implementato, parità parziale | Adapter stanza, persistenza concreta e chiamanti nel mondo restano aperti |
| SixNode | `mods.eln.node.six` | block entity host + component registry + grafo per livello + renderer client | M2 completata | host/BE, shell, item, montaggio, selezione, rimozione/drop, terminali orientati, runtime cavo/sorgente/resistore, circuito DC, ricostruzione disco/chunk, asset e sync multiplayer verificati nel perimetro M2 |
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
| Host SixNode | blocco contenitore legacy `Eln.SixNode` | `eln:six_node` | verificato per M2 | block/BE registrati; ricostruzione nel `ServerLevel`, riapertura file regione, chunk unload/reload, facce residue, drop completo e sincronizzazione client verificati |
| Cavo bassa tensione | `2052` (`32 << 6` + `4`) | `eln:low_voltage_cable` | verificato per M2 | carico `0,0125 Ω`, tratte complanari/interne/diagonali, circuito, load/unload e ricostruzione verificati; geometria, tinta, cap, texture e montaggio sulle sei facce coperti |
| Resistore di potenza | `6180` (`96 << 6` + `36`) | `eln:power_resistor` | verificato per M2 | terminali LRDU, resistenza vuota `0,01 Ω`, gruppi OBJ e montaggio sulle sei facce coperti; inventario, termica e distruzione restano nelle slice successive |
| Sorgente elettrica | `192` (`3 << 6` + `0`) | `eln:electrical_source` | M2 verificata, configurazione M3 integrata | sorgente monopolo, chiave `voltage`, resistenza seriale, gruppo `main`, texture e montaggio sulle sei facce coperti; menu/payload server-authoritative verificati, LED resta nel catalogo M4 |

Lo shell moderno conserva inoltre la mappa delle direzioni legacy: `0 WEST`, `1 EAST`, `2 DOWN`, `3 UP`, `4 NORTH`, `5 SOUTH`; le rotazioni LRDU usano rispettivamente `0 LEFT`, `1 RIGHT`, `2 DOWN`, `3 UP`. Questa informazione è mantenuta per parità e per un eventuale importer, ma non implica compatibilità diretta dei mondi 1.7.10.

Audit del piazzamento legacy: `SixNodeItem` sposta la coordinata verso la faccia cliccata quando il blocco bersaglio non è sostituibile, monta sulla faccia inversa dell'host e opera soltanto lato server. Un nuovo host richiede un blocco adiacente non-air e opaco; un host esistente accetta soltanto una faccia libera con lo stesso requisito di supporto. Lo stack viene decrementato solo dopo il successo. La rotazione base è `UP` sulle facce laterali; su pavimento e soffitto dipende dalla direzione orizzontale del giocatore, e alcuni descriptor applicano ulteriori rotazioni. Questi vincoli sono ora implementati per il cavo bassa tensione; le definizioni non ancora portate vengono rifiutate per non creare componenti segnaposto.

Audit della rimozione legacy: il ray test usa un raggio di otto blocchi, l'ordine fisso `WEST, EAST, DOWN, UP, NORTH, SOUTH` e intervalli diretti; questa particolarità può selezionare la faccia di uscita ed è conservata. Se il ray test non trova una faccia occupata, viene scelta quella più opposta alla vista. La faccia rimossa produce il proprio item salvo creative, l'host resta finché contiene altre facce, la perdita del supporto applica la stessa rimozione selettiva e la sostituzione esterna dell'host rilascia tutte le facce. Le sagome di selezione non-volume usano gli spessori canonici legacy `0.02..0.20` e `0.80..0.98`; la collisione del cavo resta vuota.

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
