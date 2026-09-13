# Audit dei processi di simulazione legacy

Audit del 12 settembre 2026 sui tipi direttamente sotto `mods.eln.sim` e `mods.eln.sim.process`, escluso il package MNA già coperto da `MNA_AUDIT.md`.

## Risultato sintetico

- tipi legacy esaminati: 42;
- controparti pure implementate o sostituzioni architetturali equivalenti: 33;
- tipi rimandati al layer che possiede le loro dipendenze: 9;
- import diretti Minecraft/NeoForge nei root puri moderni `mods.eln.sim` e `mods.eln.misc`: 0.

Questo conteggio misura la copertura strutturale, non dichiara completa la simulazione dell'intero mod. Persistenza concreta, chiamanti nel mondo e comportamento dei dispositivi richiedono ancora vertical slice.

## Tipi diretti `mods.eln.sim`

| Tipo legacy | Destinazione moderna | Stato/note |
|---|---|---|
| `IProcess` | stesso package | verificato |
| `Simulator` | `Simulator` + `ThermalSimulator` + adapter ambiente | scheduler verificato; tick NeoForge collegato e avvio dedicated verificato |
| `ElectricalLoad`, `ElectricalConnection` | stesso package | verificato |
| `ThermalLoad`, `ThermalConnection`, `ThermalResistor` | stesso package | verificato |
| `ThermalLoadInitializer`, `ThermalLoadInitializerByPowerDrop` | stesso package + validator esplicito | verificato |
| `ElectricalResistorHeatThermalLoad`, `FurnaceProcess`, `DiodeProcess` | stesso package | verificato |
| `BatteryProcess`, `BatterySlowProcess` | stesso package + policy aging | verificato, distruzione concreta rimandata |
| `RegulatorProcess`, `RegulatorFurnaceProcess`, `RegulatorThermalLoadToElectricalResistor` | stesso package + snapshot puro | verificato |
| `Integrator`, `Differentiator` | stesso package | verificato |
| `PhysicalConstant` | stesso package | implementato |
| `ITimeRemoverObserver`, `TimeRemover` | stesso package + owner `Simulator` esplicito | verificato |
| `ElectricalStackMachineProcess`, `StackMachineProcess` | layer macchina/inventario moderno | rimandati: dipendono da `IInventory`, `ItemStack`, ricette e inserimento stack |
| `ResistorProcess` | vertical slice SixNode resistore | rimandato: dipende da descriptor/element e pubblicazione rete |
| `NodeElectricalGateInputHysteresisProcess` | layer segnali + snapshot/codec | rimandato: gate NBT e costanti di segnale non ancora portati |
| `NodeVoltageState` | `VoltageStateTagCodec` esterno | codec implementato e testato; composizione nel nodo rimandata a M2 |
| `SignalRp` | layer segnali | rimandato: richiede la definizione moderna canonica di `SVU/SVII` |
| `MnaMatrixDebugger` | diagnostica NeoForge sopra `SubSystemDebugSnapshot` | rimandato: selezione nodi e scrittura file sono dipendenze esterne |
| `MonsterPopFreeProcess` | layer mondo/entità | rimandato a M5 |

## Tipi `mods.eln.sim.process`

| Tipo legacy | Destinazione moderna | Stato/note |
|---|---|---|
| `ResistorHeatThermalLoad`, `DiodeHeatThermalLoad`, `ElectricalLoadHeatThermalLoad` | stesso package | verificato |
| `IDestructible`, `ValueWatchdog` | stesso package + policy/fattore casuale iniettati | verificato |
| `VoltageStateWatchDog`, `BipoleVoltageWatchdog`, `ResistorPowerWatchdog` | stesso package | verificato |
| `ThermalLoadWatchDog` | stesso package + sink diagnostico/observer | verificato |
| `DelayedDestruction` | stesso package + owner `Simulator` esplicito | verificato |
| `ShaftSpeedWatchdog` | core meccanico futuro | rimandato: richiede `ShaftElement` e connettività albero |
| `WorldExplosion` | adapter distruzione mondo NeoForge | rimandato: coordinate, blocchi ed esplosione sono effetti di piattaforma |

## Confini preservati

- Le policy watchdog vengono consultate al momento del trip, come i flag globali legacy, ma senza singleton.
- Il fattore casuale resta uniforme nell'intervallo `[0,5; 1,5)`; le fixture usano un provider deterministico.
- `ThermalLoadWatchDog` produce dati diagnostici puri; dump e log concreti sono sink esterni.
- Distruzione ritardata e `TimeRemover` si registrano su un `Simulator` esplicito invece di `Eln.simulator`.
- `WorldExplosion` non viene simulata con un placeholder: sarà implementata quando esisteranno coordinate e ownership del mondo moderne.

## Criterio di ripresa

Il prossimo audit deve partire dai tipi rimandati seguendo le relative vertical slice, non copiandoli anticipatamente nel core. I codec `CompoundTag`, l'adapter tick e gli handler start/tick/stop sono coperti; il dedicated server ha verificato separatamente caricamento e creazione dell'owner. La verifica passa ora alla composizione dello shell SixNode con block entity, save/reload e chunk lifecycle in M2.
