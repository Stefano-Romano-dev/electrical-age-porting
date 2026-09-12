# Registro delle decisioni

Ogni decisione ha un id stabile. Non cancellare le decisioni superate: marcarle come sostituite e collegare quella nuova.

## D-001 — Piattaforma iniziale

- Stato: accettata
- Data: 11 settembre 2026
- Scelta: Minecraft 1.21.1, NeoForge e Java 21.
- Motivo: ecosistema maturo, documentazione stabile e rischio inferiore durante un port molto esteso.
- Conseguenza: il core deve restare indipendente dalla piattaforma per facilitare un futuro aggiornamento.

## D-002 — Ripartenza da zero

- Stato: accettata
- Data: 11 settembre 2026
- Scelta: creare il port in `modern/` senza recuperare automaticamente il tentativo precedente.
- Motivo: usare Electrical Age 1.24.8 come unica base funzionale e riesaminare tutte le scelte architetturali.
- Conseguenza: il vecchio stato Git resta intatto e non costituisce implementazione valida.

## D-003 — CoFH non obbligatorio

- Stato: accettata
- Data: 11 settembre 2026
- Scelta: il port non dipende da CoFH Core.
- Motivo: CoFH serve solo al ponte Redstone Flux legacy; il requisito della 1.24.8 è anche causato da `Other.modIdTe = "Eln"`.
- Conseguenza: ELN funziona autonomamente; eventuali ponti energetici saranno adapter opzionali.

## D-004 — Identità dei componenti

- Stato: proposta da validare nel milestone M2
- Data: 11 settembre 2026
- Scelta: registro ELN con `ResourceLocation` stabile per i tipi di componente; data component sugli item e stato nel block entity.
- Motivo: metadata e ItemStack damage non sono un sistema di sottotipi moderno.
- Conseguenza: occorre una tabella esplicita dagli id numerici legacy agli id moderni.

## D-005 — Architettura SixNode

- Stato: proposta da validare nel milestone M2
- Data: 11 settembre 2026
- Scelta: conservare un blocco host capace di contenere componenti indipendenti sulle sei facce.
- Motivo: è una caratteristica fondamentale di Electrical Age e limita la proliferazione di blocchi registrati.
- Conseguenza: selezione, collisione, supporto, persistenza, rendering e networking devono funzionare per singola faccia.

## D-006 — Fedeltà estetica

- Stato: accettata
- Data: 11 settembre 2026
- Scelta: il port deve essere fedele anche all'estetica di Electrical Age 1.24.8.
- Motivo: grafica, modelli 3D, interfacce e suoni fanno parte dell'identità del mod quanto il comportamento tecnico.
- Conseguenza: preservare geometrie, proporzioni, texture, palette, orientamenti, animazioni, GUI, particelle e sound design, convertendoli soltanto quanto necessario per il renderer e i formati moderni.
- Conseguenza: asset placeholder o modelli semplificati sono ammessi durante lo sviluppo, ma non soddisfano il completamento del relativo contenuto.
- Verifica prevista: confronto affiancato con la 1.24.8 tramite screenshot/video di riferimento e checklist per ogni famiglia di contenuti.

## D-007 — Entry point e uso di Kotlin

- Stato: accettata
- Data: 12 settembre 2026
- Contesto: la sorgente 1.24.8 contiene sia Java sia Kotlin, ma KotlinForForge non deve diventare accidentalmente una dipendenza obbligatoria del port.
- Scelta: mantenere in Java l'entry point NeoForge; introdurre Kotlin nel milestone M1 come linguaggio JVM ordinario soltanto dopo averne fissato plugin, runtime e packaging.
- Alternative considerate: usare subito KotlinForForge come loader/libreria obbligatoria; riscrivere tutto il core in Java.
- Motivo: separare il rischio della piattaforma dal port del core e preservare il codice Kotlin utile senza vincolare l'avvio del mod a un'integrazione non ancora verificata per NeoForge 1.21.1.
- Conseguenze: il JAR M0 non ha dipendenze esterne oltre a Minecraft e NeoForge; la configurazione Kotlin dovrà essere verificata con test e dedicated server in M1.
- Verifica prevista: compilazione mista Java/Kotlin, inclusione controllata del runtime e avvio su client/server senza KotlinForForge.
- Verifica eseguita: compilazione mista e dedicated server superati il 12 settembre 2026; il log dell'entry point conferma Kotlin 2.4.20. Kotlin stdlib e Commons Math sono presenti nel metadata Jar-in-Jar.

## D-008 — Parità numerica prima delle correzioni del solver

- Stato: accettata
- Data: 12 settembre 2026
- Contesto: il solver 1.24.8 contiene comportamenti discutibili, incluso `SubSystem.addToI` che assegna anziché accumulare i contributi sul vettore RHS.
- Scelta: conservare inizialmente algoritmo QR, orientamento dei segni e semantica RHS della 1.24.8; correggere soltanto dopo aver costruito fixture di parità dedicate.
- Alternative considerate: correggere immediatamente il solver durante la traduzione in Kotlin.
- Motivo: evitare che un refactoring e un cambio comportamentale simultanei rendano impossibile distinguere regressioni da correzioni.
- Conseguenze: i primi circuiti preservano i risultati originali; il caso di più sorgenti sullo stesso stato resta un rischio noto P-008.
- Verifica prevista: fixture legacy e moderna sul caso multi-sorgente, quindi decisione esplicita sull'accumulo del RHS.

## D-009 — Ownership e ricostruzione deterministica delle reti

- Stato: accettata
- Data: 12 settembre 2026
- Contesto: il `RootSystem` legacy governa topologia e processi globali, mentre nel port non sono ammessi singleton server-globali impliciti.
- Scelta: ogni simulazione possiede un'istanza esplicita di `RootSystem`; stati, componenti e sottosistemi sono raccolti in ordine deterministico e la modifica di una rete ne provoca break e rigenerazione controllati.
- Alternative considerate: conservare un root globale; affidare immediatamente il lifecycle agli eventi NeoForge.
- Motivo: il core resta testabile senza gioco e l'ownership potrà essere assegnata esplicitamente a server, livello o chunk nel milestone del mondo.
- Conseguenze: la politica concreta di partizionamento per livello/chunk resta da decidere in M2; la traversata conserva la semantica legacy e può riscoprire un componente rimosso se non è stato prima scollegato dagli stati.
- Verifica eseguita: nove test coprono generazione, soluzione, break/rebuild, confini privati, processi, rimozione e distruttori.

## D-010 — Conservazione delle astrazioni Line e inter-system

- Stato: accettata
- Data: 12 settembre 2026
- Contesto: la 1.24.8 riduce la dimensione delle matrici comprimendo catene resistive e partizionando reti pubbliche oltre 100 stati.
- Scelta: conservare `Line`, la soglia legacy di 100 stati e l'accoppiamento tramite equivalenti di Thévenin prima di valutare algoritmi diversi.
- Alternative considerate: risolvere sempre una matrice monolitica; sostituire subito il partizionamento con un nuovo solver sparso.
- Motivo: prestazioni, convergenza e ricostruzione della topologia sono comportamento osservabile del mod originale e devono avere una baseline di parità.
- Conseguenze: `Component` e `State` distinguono appartenenza diretta e appartenenza tramite un `IAbstractor`; il break di un sottosistema collegato propaga agli altri e ripristina la topologia concreta.
- Verifica eseguita: catena resistiva, rete da 105 stati, distruzione dell'astrazione e circuito legacy a due reti con valori numerici attesi.

## D-011 — Parità percepita come definizione di completamento

- Stato: accettata
- Data: 12 settembre 2026
- Contesto: la fedeltà funzionale ed estetica, considerate separatamente, non garantiscono che un giocatore esperto percepisca la stessa esperienza complessiva.
- Scelta: Electrical Age 1.24.8 è il riferimento canonico; un contenuto è completo solo quando comportamento, tempi, interazioni, progressione, presentazione, audio e persistenza applicabili risultano equivalenti secondo `FEDELTA.md`.
- Alternative considerate: conservare soltanto le funzionalità principali; modernizzare liberamente UX e bilanciamento.
- Motivo: l'utente richiede che, a port completato, le differenze non siano percepibili durante il gioco.
- Conseguenze: nessun miglioramento o bugfix legacy viene introdotto silenziosamente; le differenze imposte dalla piattaforma sono minimizzate e documentate, quelle intenzionali richiedono approvazione esplicita.
- Verifica prevista: scenari gemelli 1.24.8/1.21.1, fixture numeriche e temporali, confronti visivi e audio, save/reload, chunk reload e multiplayer secondo il contenuto.

## D-012 — Confine dello scambio termico ambientale

- Stato: accettata
- Data: 12 settembre 2026
- Contesto: `Simulator.thermalStep` della 1.24.8 contiene sia l'algoritmo termico sia chiamate a `RoomThermalManager`, che dipende dal mondo Minecraft.
- Scelta: conservare formule e ordine del passo in un `ThermalSimulator` puro e rappresentare il solo scambio con la stanza tramite `ThermalAmbientExchange`; l'assenza dell'adapter usa la dispersione legacy verso 0 °C.
- Alternative considerate: portare subito il room manager nel core; omettere temporaneamente lo scambio ambientale; riscrivere il modello termico.
- Motivo: consente fixture deterministiche e mantiene il core indipendente dalla piattaforma senza cambiare il punto né il segno con cui la potenza ambientale entra nell'algoritmo.
- Conseguenze: il futuro adapter NeoForge deve restituire potenza positiva quando esce dal carico e replicare la semantica di `RoomThermalManager`; le coordinate primitive legacy restano nel carico finché non verrà definita l'ownership per livello/chunk.
- Verifica eseguita: fixture con fallback ambientale, carico con coordinate e adapter, ordine connessione/processo e integrazione degli accumulatori.

## D-013 — Scheduler multi-rate separato dall'evento NeoForge

- Stato: accettata
- Data: 12 settembre 2026
- Contesto: il `Simulator` legacy combina algoritmo temporale, liste dei processi, ownership MNA, callback Forge e accesso a singleton globali.
- Scelta: mantenere nel core un `Simulator` posseduto esplicitamente che replica accumulatori, confronti, periodi e ordine delle fasi; l'adapter NeoForge si limita a chiamare `tick()` sul simulatore del server appropriato.
- Alternative considerate: riscrivere il ritmo sui tick Minecraft; integrare direttamente gli eventi NeoForge nel core; uniformare tutti i processi a 20 Hz.
- Motivo: frequenze e ordine sono comportamento osservabile e devono essere verificabili deterministicamente senza avviare il gioco.
- Conseguenze: resta da decidere l'ownership concreta server/livello/chunk; il solve elettrico aggiuntivo al primo tick e il periodo slow fisso a 0,05 s vengono conservati.
- Verifica eseguita: fixture su conteggi a 20/400 Hz, ordine delle sette fasi, autorimozione dei processi slow, classificazione fast/slow e validazione termica.

## D-014 — Policy configurabili e snapshot separati dalla piattaforma

- Stato: accettata
- Data: 12 settembre 2026
- Contesto: batteria e regolatore legacy leggono rispettivamente `SaveConfig.instance` e `NBTTagCompound`, introducendo singleton e API Minecraft nel core.
- Scelta: rappresentare l'opzione di aging con `BatteryAgingPolicy` iniettata a ogni processo e lo stato persistente con `BatteryState`/`RegulatorState`; codec e adapter NeoForge saranno esterni al core.
- Alternative considerate: mantenere una configurazione statica globale; importare `CompoundTag` direttamente nei processi; rimandare interamente batteria e regolatori.
- Motivo: formule e lettura dinamica dell'opzione restano verificabili, mentre ownership della configurazione e formato di storage possono essere assegnati correttamente a server/livello.
- Conseguenze: i costruttori moderni ricevono esplicitamente la policy; i codec di piattaforma conservano suffissi, chiavi e riparazione dei valori non finiti documentati nell'inventario.
- Verifica eseguita: aging abilitato/disabilitato, snapshot validi/non finiti e composizione esatta delle chiavi legacy.

## D-015 — Effetti dei watchdog come confini iniettati

- Stato: accettata
- Data: 12 settembre 2026
- Contesto: `ValueWatchdog` legge flag globali e casualità da `Eln/Utils`; il watchdog termico scrive log/dump, mentre `WorldExplosion` modifica direttamente il mondo.
- Scelta: conservare nel core la decisione temporale del guasto e iniettare `WatchdogPolicy`, `WatchdogRandomFactor`, `IDestructible`, sink diagnostico e observer del trip. `WorldExplosion` resta un adapter NeoForge futuro.
- Alternative considerate: importare configurazione e mondo nel core; disabilitare provvisoriamente i guasti; sostituire le esplosioni con callback vuote nei contenuti.
- Motivo: soglie, distribuzione e tempi possono essere verificati deterministicamente senza perdere gli effetti finali, che richiedono ownership e API del livello.
- Conseguenze: ogni nodo moderno deve fornire esplicitamente policy e distruttore; l'assenza di un distruttore replica il target nullo legacy ma non soddisfa la vertical slice del contenuto.
- Verifica eseguita: categorie, policy off, fattore deterministico, trip termico, dump riuscito/fallito e callback distruttiva.

## D-016 — Codec di piattaforma e simulatore per istanza server

- Stato: accettata
- Data: 12 settembre 2026
- Contesto: gli schemi NBT legacy devono restare fedeli senza introdurre Minecraft nel core; il simulatore non può dipendere da un singleton che indichi il server corrente.
- Scelta: collocare i codec `CompoundTag` in `mods.eln.platform.persistence` e possedere un `Simulator` per identità concreta di `MinecraftServer` in `ServerSimulationLifecycle`. Il tick avviene su `ServerTickEvent.Pre`, equivalente alla fase START legacy.
- Alternative considerate: rendere i processi direttamente NBT-aware; usare un unico simulatore statico globale; creare un simulatore per livello già in M1.
- Motivo: mantiene puro il modello numerico, conserva le chiavi 1.24.8 e rende esplicito il confine del lifecycle prima dell'introduzione di livelli, chunk e nodi.
- Conseguenze: i block entity M2 comporranno questi codec; l'eventuale partizionamento per livello/chunk sarà deciso con la topologia reale. Lo schema anomalo di `NbtResistor`, `prefix + "R"` senza nome, resta intenzionalmente conservato.
- Verifica eseguita: 8 test con il vero `CompoundTag`, suite totale da 134 test, build completa e dedicated server fino a `Done` con creazione dell'owner.

## Modello per nuove decisioni

```text
## D-NNN — Titolo

- Stato: proposta | accettata | rifiutata | sostituita da D-NNN
- Data: AAAA-MM-GG
- Contesto:
- Scelta:
- Alternative considerate:
- Motivo:
- Conseguenze:
- Verifica prevista:
```
