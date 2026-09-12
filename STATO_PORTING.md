# Stato del porting di Electrical Age

Aggiornato: 12 settembre 2026

## Stato generale

**Fase corrente: milestone M1 in corso; primo nucleo dinamico MNA verificato.**

Target confermato:

- sorgente: Electrical Age 1.24.8 / Forge 1.7.10;
- destinazione: Minecraft 1.21.1 / NeoForge / Java 21;
- sorgente legacy in `original/`;
- nuovo progetto in `modern/`.

`modern/` contiene una baseline NeoForge funzionante derivata dall'MDK ufficiale. Nessun file sotto `original/` è stato modificato durante la creazione della baseline.

Toolchain fissata:

- Minecraft 1.21.1;
- NeoForge 21.1.250;
- ModDevGradle 2.0.146;
- Parchment 2024.11.17;
- Gradle 9.2.1;
- Java 21;
- Kotlin 2.4.20, senza KotlinForForge;
- mod id `eln`, versione iniziale `0.1.0-alpha.1`.

## Avanzamento

- [x] Individuati e letti i file di build e le istruzioni di `original/Agents.md`.
- [x] Inventariati sorgenti, test, dipendenze e risorse.
- [x] Identificati entry point, lifecycle, registrazioni e sistemi di networking legacy.
- [x] Analizzati solver, tick di simulazione, node manager e persistenza.
- [x] Identificato il catalogo descriptor-driven basato su ItemStack damage/metadata.
- [x] Valutati GUI, rendering OBJ/LWJGL e problemi dei resource path.
- [x] Confrontata l'architettura con le API ufficiali NeoForge 1.21.1.
- [x] Definita la strategia a milestone e il primo vertical slice.
- [x] Creata la documentazione operativa per roadmap, decisioni, inventario, verifiche, problemi, risorse e diario.
- [x] Creato `AGENTS.md` con le regole persistenti del workspace.
- [x] Creato il workspace NeoForge 1.21.1 in `modern/`.
- [x] Ottenuta una clean build riproducibile e un test JUnit 5 minimale.
- [x] Avviati client e dedicated server con caricamento del mod `eln`.
- [ ] Estrarre e portare il core di simulazione con i test (solver DC, lifecycle, linee, inter-system, condensatore, induttore e delay completati; restano gli altri componenti e processi fisici).
- [ ] Implementare il primo SixNode verticale.

## Decisioni registrate

1. Il port riparte da zero in `modern/`; il precedente contenuto moderno non viene recuperato automaticamente.
2. `original/` resta una sorgente di riferimento e non viene adattata in place.
3. Il core solver/MNA viene preservato prima di portare il catalogo di contenuti.
4. I numeric descriptor id legacy non diventano registry id moderni: si useranno id namespaced stabili e una mappa di migrazione separata.
5. Il SixNode rimane un blocco host multi-faccia; non verrà trasformato banalmente in un blocco separato per ogni componente.
6. Networking, GUI, registrazione, rendering e persistenza saranno riscritti sulle API NeoForge 1.21.1.
7. Le integrazioni esterne sono rimandate finché il nucleo standalone non è giocabile.
8. La compatibilità diretta dei mondi 1.7.10 non è parte del primo milestone.
9. Il dedicated server è un criterio di verifica obbligatorio, non un test finale opzionale.
10. Il port deve essere fedele anche esteticamente alla 1.24.8; placeholder e semplificazioni non costituiscono asset finali.
11. La parità percepita con la 1.24.8 è la definizione di completamento; ogni differenza intenzionale richiede approvazione esplicita e tracciamento.

## Rilevazioni importanti

- Il progetto principale contiene 835 sorgenti e circa 88.000 righe.
- 607 file dipendono direttamente da API Minecraft/Forge o integrazioni legacy.
- 72 dei 90 test sono privi di import diretti Minecraft/Forge e costituiscono la base migliore per validare il core.
- Sono presenti almeno 409 registrazioni descriptor/element statiche rilevate.
- 144 file usano LWJGL direttamente e circa 173 sono legati al rendering legacy.
- 708 resource path contengono maiuscole o spazi e richiedono normalizzazione tracciata.
- `NodeManager` è globale e `NodeManagerNbt.writeToNBT` non scrive lo stato: l'ownership e la persistenza devono essere ridisegnate.
- CoFH Core è effettivamente necessario nella 1.24.8: la relazione Modrinth è `required` e `Other.modIdTe = "Eln"` rende sempre attiva l'integrazione RF che implementa `IEnergyHandler`. Il JAR moderno verificato non lo dichiara.
- La macchina dispone già di Java 21 a 64 bit (Temurin 21.0.11).
- `original/` non ha un `.git` proprio; il repository Git effettivo è la radice del workspace e contiene modifiche/cancellazioni preesistenti che non devono essere alterate.

## Prossimo milestone: core di simulazione

Deliverable previsto per M1:

- struttura `mods.eln.sim` senza import Minecraft/NeoForge;
- port incrementale del solver MNA e delle quantità fisiche necessarie;
- test originali rilevanti adattati e fixture numeriche di confronto;
- lifecycle del simulatore controllabile nei test;
- scelta e documentazione del supporto Kotlin senza imporre KotlinForForge come dipendenza del mod.

Avanzamento M1 verificato:

- Kotlin JVM 2.4.20 configurato su Java 21;
- Kotlin stdlib 2.4.20 e Commons Math 3.6.1 incorporati nel JAR con Jar-in-Jar;
- `mods.eln.sim.mna` non importa Minecraft o NeoForge, verificato anche da test automatico;
- portati `SubSystem`, `State`, `VoltageState`, `CurrentState`, `Component`, `Bipole`, `Resistor`, `CurrentSource` e `VoltageSource`;
- superati i due circuiti esempio della 1.24.8, un partitore, il caso singolare e l'invalidazione dinamica della matrice/RHS;
- portati ownership esplicita di `RootSystem`, generazione deterministica dei sottosistemi con confini privati, break/rebuild, oversampling pre-step, flush globale e distruttori;
- aggiunti nove test del lifecycle di root, inclusa la non-riregistrazione di componenti rimossi;
- portate la compressione delle catene resistive in `Line`, la soglia legacy di 100 stati, `InterSystemAbstraction` e l'equivalente di Thévenin;
- verificata la convergenza dell'esempio legacy a due reti prima e dopo l'aggiunta di un carico;
- portati `Capacitor`, `Inductor` e `Delay` con semantica temporale backward Euler e stato interno legacy;
- verificate traiettorie RC/RL campione per campione e la progressione interna di `Delay`;
- dedicated server avviato con caricamento effettivo del runtime Kotlin 2.4.20.

Il dettaglio tecnico e la roadmap completa sono in [ANALISI_PORTING.md](./ANALISI_PORTING.md).

L'indice di tutti i documenti operativi è in [docs/porting/README.md](./docs/porting/README.md).
