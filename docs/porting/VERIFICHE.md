# Registro delle verifiche

Registrare soltanto comandi e prove realmente eseguiti. Ogni risultato deve essere riferito al codice corrente o a un commit identificabile.

## Ambiente iniziale

- Data: 11 settembre 2026
- Sistema: Windows / PowerShell
- Java rilevato: Temurin OpenJDK 21.0.11, 64 bit
- Stato `modern/`: cartella vuota

## Ambiente della baseline moderna

- Data: 12 settembre 2026
- Sistema: Windows 11 / PowerShell
- JVM osservata nei run: Eclipse Adoptium OpenJDK 21.0.11+10-LTS, 64 bit
- Minecraft: 1.21.1
- NeoForge: 21.1.250
- ModDevGradle: 2.0.146
- Gradle wrapper: 9.2.1
- Parchment: 2024.11.17
- Mod: `eln` 0.1.0-alpha.1

## Analisi statica della sorgente originale

- Data: 11 settembre 2026
- Esito: completata
- Risultati principali: 478 sorgenti Java, 357 Kotlin, 90 test, 1.442 risorse, circa 88.000 righe principali.
- Nota: non è stata eseguita la build originale e non sono stati modificati file sotto `original/`.

## Tabella delle esecuzioni moderne

| Data | Revisione | Comando/prova | Esito | Note o log |
|---|---|---|---|---|
| 12 settembre 2026 | working tree M0 | `.\\gradlew.bat test --no-daemon` | superato | JUnit 5; 6 task, build riuscita |
| 12 settembre 2026 | working tree M0 | `.\\gradlew.bat clean build --no-daemon` | superato | JAR `eln-0.1.0-alpha.1.jar`; build pulita riuscita |
| 12 settembre 2026 | working tree M0 | `.\\gradlew.bat runClient --no-daemon` | superato | Menu client raggiunto; entry point `mods.eln.ElectricalAge` caricato |
| 12 settembre 2026 | working tree M0 | `.\\gradlew.bat runServer --no-daemon` | superato | Dedicated server arrivato a `Done (3.104s)`; mod caricato lato server |
| 12 settembre 2026 | commit `f000b76` | Verifica manuale dell'utente | superato | Client avviato ed Electrical Age presente nell'elenco mod |

### 12 settembre 2026 — Ispezione del JAR M0

- Artefatto: `modern/build/libs/eln-0.1.0-alpha.1.jar`
- SHA-256 osservato: `345DB78208272E72AE414B22DC7D3D96EE430C47F97107D936E6DC5AB53D652E`
- Contenuto verificato: classi `mods.eln`, metadata `META-INF/neoforge.mods.toml`, lingue `en_us`/`it_it`, licenza.
- Dipendenze dichiarate: soltanto Minecraft 1.21.1 e NeoForge da 21.1.250; nessun riferimento CoFH.
- Nota: l'hash identifica questa build locale e cambierà con qualsiasi modifica successiva.

### Note sulle prime esecuzioni

- Il primo `test` è fallito perché Gradle 9 richiede esplicitamente `junit-platform-launcher`; la dipendenza runtime è stata aggiunta e la prova successiva è passata.
- Il primo `build` è fallito per il contesto della closure che rinominava la licenza; il nome è ora calcolato in configurazione e la clean build è passata.
- Il primo `runServer` in sandbox non ha potuto scaricare una dipendenza Netty; ripetuto con accesso di rete, il server è partito correttamente.
- Al primo avvio server Minecraft registra come errore l'assenza iniziale di `server.properties`, poi lo crea e raggiunge regolarmente `Done`; non è un errore del mod.
- Il workflow `.github/workflows/modern-build.yml` è configurato per Java 21 e `modern/`, ma non è ancora stato osservato su GitHub Actions in questa revisione locale.

## 12 settembre 2026 — Primo slice MNA di M1

- Revisione/stato: working tree successivo a `7fa7093`.
- Kotlin: 2.4.20 su JVM 21, compilazione in-process.
- Matematica: Commons Math 3.6.1, decomposizione QR come nella 1.24.8.
- `test --rerun-tasks --no-daemon`: superato, 7 test e 0 fallimenti/errori.
- `build --rerun-tasks --no-daemon`: superato, 10 task eseguiti.
- Fixture MNA: sorgente di corrente + resistore, sorgente di tensione + resistore, partitore a due nodi, matrice singolare, invalidazione matrice e aggiornamento RHS.
- Isolamento: test automatico e ricerca statica senza import `net.minecraft` o `net.neoforged` sotto `mods/eln/sim`.
- Dedicated server: `runServer --no-daemon` arrivato a `Done (2.557s)`; log `Starting Electrical Age ... with Kotlin 2.4.20`.
- Packaging: metadata Jar-in-Jar contiene `kotlin-stdlib-2.4.20.jar` con range `[2.4.0,2.5.0)` e `commons-math3-3.6.1.jar` con range `[3.6,4.0)`.
- Artefatto osservato: `modern/build/libs/eln-0.1.0-alpha.1.jar`, SHA-256 `F50C142FA05D7632581BB38E03B93975A35B6E719CB5B57404A04AC05FBCBBE1`.
- Nota: due `clean build` sono fallite prima della compilazione per un lock Windows sul report della configuration cache; P-009. La ricompilazione forzata è passata.
- Verifica successiva: dopo l'arresto del daemon Gradle globale e l'esecuzione con accesso coerente agli output, `clean build --no-daemon` è superata (11 task); P-009 chiuso.

## 12 settembre 2026 — Lifecycle base di RootSystem

- Revisione/stato: working tree successivo a `69bee69`.
- Portata verificata: ownership di root, generazione dei sottosistemi con confini privati, break/rebuild, processi pre-step e flush, distruttori e rimozione dalla topologia.
- Primo tentativo `test --no-daemon`: non avviato, percorso `GRADLE_USER_HOME` errato (`modern/modern/.gradle`).
- Secondo tentativo `test --no-daemon`: fallito in compilazione test; il test usava `isRegistered` con un `Component` mentre l'API accetta `State`. Corretto senza modificare il codice di produzione.
- Terzo tentativo `test --no-daemon`: superato.
- Test presenti: 16 totali, 0 fallimenti/errori; nove appartengono a `RootSystemLifecycleTest`.
- Comando finale `$env:GRADLE_USER_HOME = (Resolve-Path '.\\.gradle').Path; .\\gradlew.bat clean build --no-daemon`: superato, 11 task (6 eseguiti e 5 da cache).
- Isolamento: nessun import Minecraft/NeoForge sotto `modern/src/main/kotlin/mods/eln/sim`.
- Limite intenzionale: linee e inter-system non fanno parte di questo slice e non sono dichiarati verificati.

## 12 settembre 2026 — Linee e inter-system

- Revisione/stato: working tree successivo a `5a67c37`.
- Portata verificata: astrazione `Line`, ripristino della topologia, split delle reti oltre 100 stati, `InterSystemAbstraction`, Thévenin e propagazione del break.
- Primo `test --no-daemon`: fallito 1 test su 16; il test del confine privato attendeva ancora il bridge pending invece della nuova astrazione inter-system. Aspettativa aggiornata al comportamento completo.
- Secondo `test --no-daemon`: superato dopo l'aggiunta delle fixture dedicate.
- Fixture principale: esempio `RootSystem.main` della 1.24.8; dopo 50 step `n2=0,6896551724`, `n12=0,7241379310`, poi con il secondo carico `n2=0,6470588235`, `n12=0,6176470588`.
- Comando finale `$env:GRADLE_USER_HOME = (Resolve-Path '.\\.gradle').Path; .\\gradlew.bat clean build --no-daemon`: superato, 11 task eseguiti.
- Test presenti: 23 totali, 0 fallimenti/errori; sette appartengono a `LineInterSystemParityTest`.
- Isolamento: nessun import Minecraft/NeoForge sotto `modern/src/main/kotlin/mods/eln/sim`.

## 12 settembre 2026 — Contratto documentale di fedeltà

- Revisione/stato: working tree successivo a `afa6cc3`; sola documentazione, nessun codice o asset runtime modificato.
- `git diff --check`: superato; soltanto avvisi informativi sulla futura conversione LF/CRLF di Git.
- Ricerca dei riferimenti a D-011 e `FEDELTA.md`: superata in istruzioni, analisi, stato e indice documentale.
- Build non ripetuta: la modifica non interessa sorgenti, risorse runtime o configurazione Gradle.

## 12 settembre 2026 — Primo nucleo dinamico MNA

- Revisione/stato: working tree successivo a `ade6271`.
- Portata verificata: `Capacitor`, `Inductor` e `Delay`, mantenendo formule e ordine di aggiornamento della 1.24.8.
- Primo `test --no-daemon`: fallito 1 test su 30 perché la fixture chiamava `Delay.simProcessI` prima della generazione della matrice e degli id MNA.
- Secondo `test --no-daemon`: fallito 1 test su 30 perché la fixture ipotizzava corrente 0 al secondo aggiornamento di `Delay`; il codice originale produce 2 per l'accumulo di `oldIa`/`oldIb`, quindi l'aspettativa è stata corretta alla semantica legacy.
- Terzo `test --no-daemon`: superato, 30 test e 0 fallimenti/errori.
- Comando finale `$env:GRADLE_USER_HOME = (Resolve-Path '.\\.gradle').Path; .\\gradlew.bat clean build --no-daemon`: superato, 11 task (6 eseguiti e 5 da cache).
- Fixture temporali: RC con campioni `0,5; 0,75; 0,875; 0,9375`, RL con `0,05; 0,075; 0,0875; 0,09375`, entrambe a `dt=0,1`.
- Isolamento statico: nessun import Minecraft/NeoForge sotto `modern/src/main/kotlin/mods/eln/sim`.
- Limite dichiarato: l'adapter moderno per la persistenza della corrente dell'induttore non fa parte di questo slice.

## 12 settembre 2026 — Componenti MNA avanzati

- Revisione/stato: working tree successivo a `3af92b2`.
- Portata verificata: `ResistorSwitch`, `Transformer`, `PowerSource`, `PowerSourceBipole` e `TransformerInterSystemProcess`.
- Primo `test --no-daemon`: fallito in compilazione; Kotlin vieta un setter privato su una proprietà aperta. `VoltageSource.voltage` è stata resa finale mantenendo l'accesso tramite `setVoltage`.
- Secondo `test --no-daemon`: fallito 1 test su 39 per conteggio errato della fixture; il quinto stato è la corrente interna della sorgente di tensione, come nell'originale.
- Terzo `test --no-daemon`: superato, 39 test e 0 fallimenti/errori.
- Fixture aggiuntiva successiva: fallback a tensione zero quando la potenza richiesta è NaN.
- Comando finale `$env:GRADLE_USER_HOME = (Resolve-Path '.\\.gradle').Path; .\\gradlew.bat clean build --no-daemon`: superato, 11 task (9 eseguiti e 2 da cache), 40 test e 0 fallimenti/errori.
- Parità coperta: rapporto ideale, mancata invalidazione legacy al cambio rapporto, limiti di tensione/corrente, potenza erogata, formule Thévenin e fallback NaN.
- Isolamento statico: nessun import Minecraft/NeoForge sotto `modern/src/main/kotlin/mods/eln/sim`.
- Limite dichiarato: gli adapter moderni di persistenza per switch e sorgenti non fanno parte di questo slice.

## 12 settembre 2026 — Audit di completezza MNA

- Revisione/stato: working tree successivo a `e84d68e`.
- Confronto file: 30 tipi Java sotto il package MNA legacy e 30 controparti Kotlin moderne dopo l'aggiunta di `Monopole` e `SubSystemDebugSnapshot`.
- API diagnostiche aggiunte: snapshot di matrice/RHS/ownership/connessioni, `setX`, `getX`, `getXSafe`, `componentSize` e descrizione del sottosistema.
- Semantiche riallineate: pin conservati dopo `breakConnection`, duplicati nella lista componenti dello stato, membership particolare di `CurrentSource`, catch generale della decomposizione QR e riscoperta dei componenti non scollegati.
- `test --no-daemon`: superato, 49 test e 0 fallimenti/errori.
- Comando finale `$env:GRADLE_USER_HOME = (Resolve-Path '.\\.gradle').Path; .\\gradlew.bat clean build --no-daemon`: superato, 11 task (6 eseguiti e 5 da cache), 49 test e 0 fallimenti/errori.
- Nuove fixture audit: sette per snapshot, coefficienti MNA e API; due aggiuntive per teardown pin e duplicati legacy.
- Isolamento statico: nessun import Minecraft/NeoForge sotto `modern/src/main/kotlin/mods/eln/sim`.
- Limiti dichiarati: adapter NBT, chiamanti reali e benchmark/convergenza rappresentativi restano aperti; dettagli in `MNA_AUDIT.md`.

## 12 settembre 2026 — Primo strato elettrico e termico puro

- Revisione/stato: working tree successivo a `3b1ec01`.
- Portata verificata: `ElectricalLoad`, `ElectricalConnection`, `ThermalLoad`, `ThermalConnection`, `ThermalResistor`, `ThermalSimulator` e processi heater base.
- Primo tentativo `test`: non avviato; `GRADLE_USER_HOME` puntava erroneamente a `modern/modern/.gradle`.
- Secondo tentativo `test -Dkotlin.compiler.execution.strategy=in-process`: non avviato; PowerShell ha passato l'opzione `-D` come nome task. La proprietà era già fissata correttamente in `gradle.properties`.
- Primo test effettivo: fallito 1 su 68 perché la fixture pretendeva pin nulli dopo `breakConnection`; riallineata alla conservazione dei riferimenti legacy già documentata nell'audit MNA.
- Comando `$env:GRADLE_USER_HOME = (Resolve-Path '.\.gradle').Path; .\gradlew.bat test`: superato, 68 test e 0 fallimenti/errori.
- Comando finale `$env:GRADLE_USER_HOME = (Resolve-Path '.\.gradle').Path; .\gradlew.bat clean build --no-daemon`: superato in 30 s, 11 task (6 eseguiti e 5 da cache), 68 test e 0 fallimenti/errori.
- Parità coperta: resistenza seriale e bridge, corrente dimezzata con esclusione `Line`, trasferimenti termici e guardie NaN, stranezza signed-throughput, coordinate/velocità, ordine completo del passo, adapter ambiente e tre processi Joule→calore.
- Isolamento statico: `CoreIsolationTest` copre l'intero package `modern/src/main/kotlin/mods/eln/sim`; nessun import Minecraft/NeoForge.
- Limiti dichiarati: scheduler multi-rate, `RoomThermalManager` moderno, persistenza e processi fisici superiori non fanno parte di questa slice.

## 12 settembre 2026 — Scheduler multi-rate e inizializzatori termici

- Revisione/stato: working tree successivo a `16686c4`.
- Portata verificata: scheduler puro `Simulator`, validazione/inizializzazione termica, `FurnaceProcess`, `DiodeProcess`, conversione resistiva, `Integrator` e `Differentiator`.
- Primo `test`: fallito 3 test su 85 per aspettative della fixture; l'implementazione mostrava il doppio solve elettrico iniziale legacy e la potenza residua del forno dopo il consumo. Le aspettative sono state riallineate alle formule sorgente.
- Secondo `test`: superato, 85 test e 0 fallimenti/errori.
- Aggiunta una fixture per verificare che l'inizializzatore deleghi davvero il rifiuto dei parametri instabili; totale corrente 86 test.
- Primo comando finale `$env:GRADLE_USER_HOME = (Resolve-Path '.\.gradle').Path; .\gradlew.bat clean build --no-daemon`: fallito in `compileJava` per `AccessDeniedException` sul JAR `mergetool-2.0.3-api.jar` nella cache locale, dopo compilazione Kotlin riuscita.
- Stesso comando con accesso coerente alla cache/output: superato in 14 s, 11 task (10 eseguiti e 1 da cache), 86 test e 0 fallimenti/errori.
- Parità coperta: 2/3 solve elettrici cumulativi nei primi due tick, 20 passi termici fast per tick, ordine slow-pre/elettrico/termico/slow/distruzione/slow-post, formule di stabilità, consumi e utility numeriche.
- Isolamento statico: nessun import Minecraft/NeoForge nel package core, verificato dalla suite.
- Limiti dichiarati: l'adapter evento NeoForge, ownership per server/livello, batterie, regolatori, persistenza e room manager moderno restano aperti.

## 12 settembre 2026 — Curve, batterie e regolatori

- Revisione/stato: working tree successivo a `5af78ad`.
- Portata verificata: `IFunction`, `FunctionTable`, `FunctionTableYProtect`, `BatteryProcess`, `BatterySlowProcess`, `BatteryAgingPolicy`, regolatore base e adapter forno/resistore.
- Primo `test`: fallito in compilazione test; la proprietà fixture `hit` generava `getHit()` e collideva con il metodo astratto. Rinominata in `measuredValue`, senza modifica alla produzione.
- Secondo `$env:GRADLE_USER_HOME = (Resolve-Path '.\.gradle').Path; .\gradlew.bat test`: superato, 108 test e 0 fallimenti/errori.
- Esteso successivamente `CoreIsolationTest` a entrambi i root puri `mods/eln/sim` e `mods/eln/misc`.
- Comando finale `$env:GRADLE_USER_HOME = (Resolve-Path '.\.gradle').Path; .\gradlew.bat clean build --no-daemon`, eseguito con accesso coerente alla cache: superato in 13 s, 11 task (9 eseguiti e 2 da cache), 108 test e 0 fallimenti/errori.
- Parità coperta: interpolazione/estrapolazione e clamp, carica/scarica e calore, energia a 50 campioni, scaling vita, aging quadratico e policy off, sovratensione, modalità regolatore, isteresi, PID legacy, soglie del resistore e snapshot.
- Schemi preservati: batteria `NBPQ`/`NBPlife`; regolatore `prefix + name + errorIntegrated/target`.
- Limiti dichiarati: codec `CompoundTag`, distruzione del nodo, configurazione server e prove su dispositivi nel mondo non fanno parte di questa slice.

## 12 settembre 2026 — Watchdog, timer e audit processi

- Revisione/stato: working tree successivo a `f785dec`.
- Portata verificata: `ValueWatchdog`, watchdog tensione/bipolo/resistore/termico, `IDestructible`, `DelayedDestruction`, `TimeRemover` e relativi confini di policy/diagnostica.
- Comando `$env:GRADLE_USER_HOME = (Resolve-Path '.\.gradle').Path; .\gradlew.bat test`: superato, 126 test e 0 fallimenti/errori; rimossi successivamente cinque warning non funzionali nella fixture termica.
- Comando finale `$env:GRADLE_USER_HOME = (Resolve-Path '.\.gradle').Path; .\gradlew.bat clean build --no-daemon`, con accesso coerente alla cache: superato in 14 s, 11 task (8 eseguiti e 3 da cache), 126 test e 0 fallimenti/errori.
- Parità coperta: primo overflow ignorato, timeout e recupero, policy per categoria, ripetizione del trip, limiti nominali, temperatura assoluta/ambiente, derivata termica, dump, delay e timer osservabile.
- Audit strutturale: 42 tipi legacy esaminati, 32 controparti pure presenti, 10 rimandati con destinazione esplicita in `PROCESS_AUDIT.md`.
- Isolamento statico: nessun import Minecraft/NeoForge nei root puri `mods/eln/sim` e `mods/eln/misc`.
- Limiti dichiarati: `WorldExplosion`, `ShaftSpeedWatchdog`, adapter di configurazione/dump e teardown di nodi reali non fanno parte di questa slice.

## 12 settembre 2026 — Codec NBT e lifecycle server NeoForge

- Revisione/stato: working tree successivo a `3ecad5b`.
- Portata verificata: codec `CompoundTag` per batteria, regolatore, tensione, temperatura, forno, sorgenti, induttore, switch, sorgenti di potenza e resistore; owner `Simulator` per istanza server.
- Primo comando `.\gradlew.bat test --no-daemon`: fallito in `compileTestKotlin`, perché la classpath test predefinita di ModDevGradle non comprendeva Minecraft.
- Diagnosi `.\gradlew.bat dependencies --configuration compileClasspath --no-daemon` e `.\gradlew.bat dependencies --configuration testCompileClasspath --no-daemon`: confermata la presenza di NeoForge/Minecraft soltanto nella prima; abilitato `neoForge.unitTest` secondo l'API del plugin.
- Comando finale `.\gradlew.bat test --no-daemon`, con accesso coerente alla cache: superato, 134 test e 0 fallimenti/errori.
- Primo comando `.\gradlew.bat build --no-daemon`, con accesso coerente alla cache: superato, 13 task tutti aggiornati e artefatto completo valido.
- Comando finale `.\gradlew.bat build --no-daemon` dopo la revisione conclusiva di codice e documentazione: superato in 15 s, 13 task (3 eseguiti, 10 aggiornati), 134 test e 0 fallimenti/errori.
- Comando `.\gradlew.bat runServer --no-daemon`: dedicated server arrivato a `Done (2.506s)` e log `Created Electrical Age simulation owner for server ...`; nessun classloading client osservato.
- Parità coperta: nomi esatti delle chiavi, uso float/double legacy, riparazione dei valori non finiti prevista dall'originale, limiti delle sorgenti, restore dello switch e anomalia `NbtResistor` `prefix + R`.
- Limite dichiarato: la console del task Gradle non ha inoltrato `stop` al processo Minecraft; il callback `ServerStoppingEvent` e il relativo log di rimozione non sono quindi dichiarati verificati in questa revisione. Le istanze di prova sono state arrestate e la porta 25565 risulta libera.

## 13 settembre 2026 — Chiusura M1 e fondazione SixNode M2

- Revisione/stato: working tree successivo a `51bfe59`.
- Riferimenti legacy verificati in `SixNodeRegistration.kt`: sorgente elettrica `192`, cavo bassa tensione `2052`, resistore di potenza `6180`; asset associati registrati in `RISORSE.md`.
- Lifecycle: testato lo stesso `ServerSimulationLifecycle` usato dall'event bus invocando start, tick e stop con le classi evento NeoForge reali; l'owner viene creato, avanzato e rimosso. Questa prova chiude il contratto dell'handler, ma non dichiara osservato il log di stop in un processo dedicated interattivo.
- Primo comando `.\gradlew.bat test --no-daemon`: superato dopo catalogo e test lifecycle.
- Secondo comando `.\gradlew.bat test --no-daemon`: superato dopo shell SixNode e codec persistente.
- Comando finale `$env:GRADLE_USER_HOME = (Resolve-Path '.\.gradle').Path; .\gradlew.bat build --no-daemon`: superato in 28 s, 13 task (9 eseguiti e 4 aggiornati).
- Risultato suite finale ricavato dai report XML: 147 test, 0 fallimenti, 0 errori, 30 suite.
- Parità coperta: sei facce indipendenti, ordine degli indici legacy, rifiuto della sostituzione su faccia occupata, codici LRDU e fallback storico, id canonici, collisioni catalogo e round-trip dello schema versionato.
- Robustezza persistente: gli id namespaced validi sconosciuti sopravvivono al round-trip; una versione non supportata lascia intatto lo stato corrente.
- Limiti dichiarati: non sono ancora registrati blocco, block entity o item; non è stato eseguito save/reload nel mondo, chunk reload, rendering, GameTest o multiplayer. Nessun asset e nessun placeholder estetico è stato aggiunto.

## 13 settembre 2026 — Registrazione host SixNode e block entity

- Portata verificata: registri NeoForge `eln:six_node` per blocco e block entity, ownership delle sei facce nel block entity e composizione del codec versionato.
- Primo comando `$env:GRADLE_USER_HOME = (Resolve-Path '.gradle').Path; .\gradlew.bat test --no-daemon`: fallito prima di una compilazione attendibile per `AccessDeniedException` sulla cache Gradle; gli errori di classpath a cascata non sono stati trattati come difetti del codice.
- Secondo comando identico con accesso coerente alla cache: compilazione principale superata, compilazione test fallita per inferenza generica del tipo fittizio.
- Terzo comando identico: 149 test eseguiti, 2 falliti perché il runner aveva già congelato il registro e i test tentavano di creare un blocco non registrato.
- I test sono stati riallineati a usare il blocco e il block entity type realmente registrati, aumentando la copertura del wiring anziché aggirare il registro.
- Comando finale `$env:GRADLE_USER_HOME = (Resolve-Path '.gradle').Path; .\gradlew.bat test --no-daemon`: superato, 149 test e 0 fallimenti/errori.
- Primo comando `$env:GRADLE_USER_HOME = (Resolve-Path '.gradle').Path; .\gradlew.bat build --no-daemon`: superato, 13 task aggiornati.
- Comando `$env:GRADLE_USER_HOME = (Resolve-Path '.gradle').Path; .\gradlew.bat runServer --no-daemon`: mod caricato e dedicated server arrivato a `Done (2.388s)`; owner della simulazione creato e nessun errore di registro osservato.
- Limite dichiarato: la console Gradle non ha inoltrato `stop`; il processo è stato terminato dal wrapper. Save/reload e chunk reload di un blocco piazzato non sono ancora dichiarati verificati.
- Comando finale `$env:GRADLE_USER_HOME = (Resolve-Path '.gradle').Path; .\gradlew.bat build --no-daemon` dopo asserzioni sugli id e pulizia dell'API: superato in 15 s, 13 task (6 eseguiti e 7 aggiornati), 149 test e 0 fallimenti/errori in 31 suite.

## 13 settembre 2026 — GameTest mondo e identità item SixNode

- Portata verificata: piazzamento dell'host in un vero `ServerLevel`, dispatch persistente vanilla del block entity, item contenitore e data component del tipo SixNode.
- Primo comando `$env:GRADLE_USER_HOME = (Resolve-Path '.gradle').Path; .\gradlew.bat runGameTestServer --no-daemon`: fallito perché la struttura `eln:empty` non era ancora copiata dalla radice sorgente alla directory runtime `run/gameteststructures`; test registrato ma non eseguito.
- Aggiunto il task riproducibile `prepareGameTestStructures`, dipendenza della sola preparazione GameTest server.
- Secondo comando identico: superato, 1/1 GameTest richiesto. Il test ha piazzato `eln:six_node`, montato un cavo, serializzato `id=eln:six_node`, rimosso il BE, ricostruito tramite `BlockEntity.loadStatic` e verificato lo stato della faccia reinserita.
- Il GameTest server ha inoltre osservato creazione e rimozione dell'owner, arresto normale e salvataggio di tutti i chunk; il test non equivale ancora a riaprire da disco lo stesso mondo in un secondo processo.
- Primo comando `$env:GRADLE_USER_HOME = (Resolve-Path '.gradle').Path; .\gradlew.bat test --no-daemon` dopo l'item: superato con un warning per l'helper `createDataComponents(String)` deprecato; sostituito con l'overload esplicito `Registries.DATA_COMPONENT_TYPE`.
- Comando finale `test --no-daemon`: superato in 14 s, 152 test e 0 fallimenti/errori.
- Comando finale `runGameTestServer --no-daemon`: superato in 13 s, 1/1 GameTest, shutdown e save chunk normali.
- Comando `$env:GRADLE_USER_HOME = (Resolve-Path '.gradle').Path; .\gradlew.bat build --no-daemon`: superato in 6 s, 13 task aggiornati.
- Identità verificate: item `eln:six_node_component`, data component `eln:six_node_component_type`, mapping `eln:low_voltage_cable` e round-trip completo dell'`ItemStack`; id validi sconosciuti vengono conservati senza essere trattati come definizioni note.

## 13 settembre 2026 — Montaggio item del cavo bassa tensione

- Portata verificata: uso reale dell'item tipizzato nel `ServerLevel`, creazione dell'host, montaggio sulla faccia inversa, rotazione LRDU base, consumo survival e rifiuti atomici.
- Due esecuzioni iniziali di `test --no-daemon` nella sandbox sono fallite per `AccessDeniedException` sul JAR della cache Gradle; gli errori di classpath a cascata non sono stati attribuiti al codice. Lo stesso comando con accesso coerente alla cache è superato.
- Primo `runGameTestServer --no-daemon`: 3/4 superati; il caso positivo aveva usato erroneamente `GameTestHelper.placeAt` senza mettere lo stack nella mano del mock player e con la coordinata precedente al blocco di supporto.
- Corretto esclusivamente il setup del test sulla base del bytecode dell'helper vanilla: mano principale valorizzata e clic effettivo sul supporto.
- Comando finale `runGameTestServer --no-daemon`: superato in 16 s, 4/4 GameTest, shutdown e salvataggio chunk normali.
- Comando finale `build --no-daemon`: superato in 11 s, 13 task (2 eseguiti, 11 aggiornati); report XML: 154 test, 0 fallimenti, 0 errori, 33 suite.
- Limiti dichiarati: non sono ancora verificati riapertura in un secondo processo, chunk unload/reload, rimozione/drop, suono, rendering, grafo o multiplayer.

## 13 settembre 2026 — Riapertura file regione e chunk unload/reload

- Portata verificata: persistenza del SixNode attraverso due processi dedicated distinti e ricostruzione dopo unload/reload runtime dello stesso chunk.
- Aggiunte le run `sixNodePersistenceWrite` e `sixNodePersistenceVerify` sulla directory isolata `run-six-node-persistence`, aggregate dal task `verifySixNodeDiskPersistence`; l'EULA della sola directory di prova viene preparata automaticamente.
- La fase `write` carica il chunk lontano `(100, 100)`, crea l'host a `1608,300,1608`, monta un cavo `DOWN` con rotazione `RIGHT`, forza `saveEverything` e termina normalmente.
- La fase `verify` avvia un secondo processo sullo stesso file regione, verifica blocco, tipo BE, unica faccia, id `eln:low_voltage_cable` e rotazione; rimuove poi il force-load, osserva `getChunkNow == null`, ricarica e ripete le asserzioni.
- Primo comando `verifySixNodeDiskPersistence --no-daemon`: superato in 34 s con marker `SIX_NODE_PERSISTENCE_WRITE_OK` e `SIX_NODE_PERSISTENCE_VERIFY_OK`.
- Secondo comando dopo l'aggiunta del controllo runtime: superato in 26 s con anche `SIX_NODE_CHUNK_UNLOAD_RELOAD_OK`.
- Comando finale ripetuto sul mondo già esistente dopo aver reso esplicito il reset dell'host: superato in 30 s, 13 task (4 eseguiti, 9 aggiornati), confermando la ripetibilità.
- Comando finale `build --no-daemon` dopo il vincolo non-production del probe: superato in 15 s, 13 task (4 eseguiti, 9 aggiornati); 154 test, 0 fallimenti/errori.
- Limiti dichiarati: rimozione/drop, grafo, sincronizzazione multiplayer, rendering e feedback audiovisivo restano aperti.

## 13 settembre 2026 — Selezione, rimozione e drop per faccia SixNode

- Portata verificata: sagome di selezione canoniche, risoluzione legacy della faccia, rimozione selettiva, perdita supporto, drop tipizzato e sincronizzazione del block entity.
- Comando `$env:GRADLE_USER_HOME = (Resolve-Path .gradle).Path; .\gradlew.bat test --no-daemon`: superato in 17 s dopo l'introduzione del resolver; i report finali contano 156 test, 0 fallimenti, 0 errori e 34 suite.
- Primo comando `runGameTestServer --no-daemon` sullo slice: superato in 17 s, 7/7 GameTest.
- Aggiunta la copertura della sostituzione esterna dell'host, che deve rilasciare ogni faccia residua.
- Comando combinato `$env:GRADLE_USER_HOME = (Resolve-Path .gradle).Path; .\gradlew.bat test runGameTestServer --no-daemon`: superato in 21 s, 17 task (7 eseguiti, 10 aggiornati), 156 test unitari e 8/8 GameTest.
- Il primo rilancio confinato di `verifySixNodeDiskPersistence --no-daemon` è fallito nel bootstrap launcher con `NoSuchElementException`, prima dell'avvio di Minecraft e senza asserzioni eseguite; due rilanci confinati del solo processo `write` hanno riprodotto lo stesso limite d'ambiente.
- Lo stesso comando `$env:GRADLE_USER_HOME = (Resolve-Path .gradle).Path; .\gradlew.bat verifySixNodeDiskPersistence --no-daemon` fuori sandbox è superato in 26 s: marker di scrittura, riapertura e chunk unload/reload presenti, 13 task (2 eseguiti, 11 aggiornati).
- Comando finale `$env:GRADLE_USER_HOME = (Resolve-Path .gradle).Path; .\gradlew.bat build --no-daemon`: superato in 3 s, 13 task aggiornati.
- Parità coperta: lastre non-volume `0.02..0.20`/`0.80..0.98`, ordine e intervalli diretti del ray legacy, fallback opposto alla vista, permanenza dell'host con altre facce, rimozione dell'host vuoto, drop survival, assenza drop creative e drop completo su sostituzione.
- Limiti dichiarati: non sono ancora verificati rendering del cavo, suoni/particelle, grafo elettrico, lifecycle elettrico load/unload o multiplayer reale.

## 15 settembre 2026 — Grafo elettrico server-side del primo cavo

- Portata verificata: ownership per `ServerLevel`, carico MNA per faccia LV, resistenza legacy, tratte complanari/interne/diagonali, montaggio/rimozione e ricostruzione durante chunk load/unload.
- Primo `test --no-daemon` confinato: non avviato per accesso rete negato durante il refresh dei metadata Maven; lo stesso comando fuori sandbox ha compilato il main e rilevato esclusivamente import assertion Kotlin mancanti nei nuovi test.
- Dopo il passaggio alle assertion JUnit, un'esecuzione confinata ha raggiunto `compileTestJava` ma è fallita per `AccessDeniedException` sul JAR `mergetool-2.0.3-api.jar`; lo stesso comando fuori sandbox è superato.
- Primo GameTest del grafo: 8/9 superati; il nuovo caso creava due host vuoti consecutivi e il primo veniva correttamente rimosso dall'aggiornamento del vicino. Corretto il solo ordine del setup montando il primo cavo prima di creare il secondo host.
- Secondo comando `runGameTestServer --no-daemon`: superato, 9/9 GameTest.
- Primo ampliamento di `verifySixNodeDiskPersistence`: la riapertura del contenuto era corretta ma il runtime non veniva ricostruito usando il solo `onLoad`; aggiunto `ChunkEvent.Load`.
- Secondo ampliamento: la ricostruzione da disco era verde, ma l'unload osservava ancora il carico perché la mappa dei block entity era già vuota nell'evento; introdotto teardown per coordinate di chunk e pruning a `Post` tick.
- Terzo ampliamento: teardown verde, ma la mappa dei block entity non era ancora popolata nel primo callback di load; introdotta una coda dei chunk caricati elaborata al tick stabile successivo.
- Comando finale `$env:GRADLE_USER_HOME = (Resolve-Path '.gradle').Path; .\gradlew.bat verifySixNodeDiskPersistence --no-daemon`: superato in 28 s con marker di scrittura, riapertura e `SIX_NODE_CHUNK_UNLOAD_RELOAD_OK`; verificati anche assenza del carico durante unload e sua ricostruzione dopo reload.
- Comando finale `$env:GRADLE_USER_HOME = (Resolve-Path '.gradle').Path; .\gradlew.bat test runGameTestServer --no-daemon`: superato in 25 s; report XML con 162 test, 0 fallimenti/errori in 35 suite e 9/9 GameTest.
- Comando finale `$env:GRADLE_USER_HOME = (Resolve-Path '.gradle').Path; .\gradlew.bat build --no-daemon`: superato in 6 s, 13 task aggiornati.
- Limiti dichiarati: sorgente e resistore non hanno ancora runtime/terminali moderni; il circuito DC nel mondo, rendering, audio e multiplayer restano aperti.

## 15 settembre 2026 — Terminali orientati e primo circuito DC SixNode

- Analisi legacy: verificati `ElectricalSourceElement`, `ResistorElement`, `Direction` e `LRDU`; la sorgente usa un carico con `0,0125 Ω` e chiave NBT `voltage`, il resistore vuoto vale `0,01 Ω`, espone soltanto `front.right()`/`front.left()` e i suoi due carichi usano `noImpedance = 1e-9 Ω`.
- Portata verificata: terminali tangenti espliciti per le tre topologie SixNode, runtime MNA di cavo/sorgente/resistore, parametri finiti opzionali nel codec, piazzamento di sorgente e resistore e rotazione aggiuntiva `left()` del resistore.
- Primo comando mirato confinato: fallito prima della build perché eseguito dalla directory `modern/` con `Resolve-Path '.\modern\.gradle'`, che puntava erroneamente a `modern/modern/.gradle`.
- Secondo tentativo: fallito perché PowerShell ha interpretato in modo errato l'argomento `-Pkotlin.compiler.execution.strategy=in-process`; la proprietà era già configurata nel progetto ed è stata omessa nei comandi successivi.
- Primo test mirato corretto confinato: arrivato alla compilazione ma fallito per `AccessDeniedException` sulla cache `mergetool`; lo stesso comando fuori sandbox è superato.
- Comando mirato `$env:GRADLE_USER_HOME = (Resolve-Path '.gradle').Path; .\gradlew.bat test --tests mods.eln.node.six.SixNodeElectricalGraphTest --tests mods.eln.platform.persistence.SixNodeContentsTagCodecTest --no-daemon`: superato.
- Primo comando `test runGameTestServer --no-daemon`: test unitari verdi, 9/10 GameTest; il nuovo circuito creava tre host vuoti prima del montaggio e il primo veniva correttamente rimosso dagli aggiornamenti dei vicini. Corretto soltanto il setup montando ogni faccia subito dopo la creazione dell'host.
- Secondo comando `$env:GRADLE_USER_HOME = (Resolve-Path '.gradle').Path; .\gradlew.bat runGameTestServer --no-daemon`: superato in 18 s, 10/10 GameTest. Il circuito fra sorgenti da 50 V e 0 V misura `1428,5713469 A`, uguale a `50 / (0,01 + 2×0,0125 + 2×10⁻⁹)`.
- Primo harness dedicated ampliato: contenuto persistente corretto ma runtime assente dopo il reload del chunk. Un'attesa di 40 tick ha escluso una semplice gara; mantenere forzato il chunk ha poi rivelato che la verifica d'identità tramite `level.getBlockEntity` durante `setRemoved` poteva innescare un caricamento ricorsivo mentre il chunk veniva scaricato.
- Correzione: il contesto server traccia per livello e posizione l'identità dei SixNode caricati, ignora il teardown di un'istanza sostituita e non interroga il mondo durante l'unload; il probe mantiene il chunk forzato soltanto durante la verifica di ricostruzione.
- Comando finale `$env:GRADLE_USER_HOME = (Resolve-Path '.gradle').Path; .\gradlew.bat verifySixNodeDiskPersistence --no-daemon`: superato in 28 s con marker `SIX_NODE_PERSISTENCE_WRITE_OK`, `SIX_NODE_PERSISTENCE_VERIFY_OK` e `SIX_NODE_CHUNK_UNLOAD_RELOAD_OK`.
- Comando finale `$env:GRADLE_USER_HOME = (Resolve-Path '.gradle').Path; .\gradlew.bat test runGameTestServer build --no-daemon`: superato in 25 s; report XML con 165 test, 0 fallimenti/errori in 35 suite, 10/10 GameTest e build completa verde.
- `git diff --check`: nessun errore; soli avvisi attesi di conversione LF→CRLF.
- Limiti dichiarati: rendering canonico, audio, configurazione/inventario, termica e distruzione del resistore, prova client e multiplayer restano aperti.

## 16 settembre 2026 — Primo renderer client SixNode

- Portata verificata: registrazione client-only del renderer, trasformazioni delle sei facce e LRDU, caricamento dei modelli OBJ/MTL originali di sorgente e resistore, geometria procedurale del cavo e condivisione del contratto dei terminali col grafo.
- Il primo comando confinato `compileJava compileKotlin --no-daemon` è fallito per il noto `AccessDeniedException` sulla cache `mergetool`; lo stesso comando fuori sandbox è superato con due soli warning di deprecazione dell'annotazione event bus.
- Il comando `build --no-daemon`, rieseguito fuori sandbox per lo stesso lock, è superato; i report contano 167 test, 0 fallimenti e 0 errori.
- Il primo `runClient --no-daemon` ha registrato il renderer e caricato la sorgente, ma ha rifiutato il modello del resistore perché l'OBJ riferiva `PowerElectricPrimitives.mtl` con maiuscole. Il riferimento interno è stato corretto in lowercase.
- Il secondo `runClient --no-daemon` ha completato il caricamento risorse e raggiunto il menu principale senza errori ELN relativi a OBJ, MTL o blockstate. Rimane il warning del modello item `eln:item/six_node_component`, tracciato come P-021. Il processo è stato terminato manualmente dopo l'osservazione, quindi il relativo exit code Gradle non è considerato un successo del task.
- Il comando `$env:GRADLE_USER_HOME = (Resolve-Path '.gradle').Path; .\gradlew.bat runGameTestServer --no-daemon` è superato fuori sandbox: 10/10 GameTest e nessun caricamento delle classi client sul dedicated server.
- Il controllo automatico della finestra tramite UI non è stato possibile perché il processo Java non era esposto tra le superfici disponibili. Non esistono quindi screenshot o confronto affiancato: lo stato estetico resta parziale.
- Evidenza manuale successiva: gli screenshot dell'utente mostrano modelli mondo caricati ma cavo bianco, cap su ogni host rettilineo, raccordo sorgente-rete assente e tre item missing-texture. Il confronto col codice 1.24.8 ha confermato tinta dye `0 = 0,2`, cap soltanto con zero/una connessione, curva o diramazione e `super.draw()` della sorgente per i cavi automatici.
- Correzione: tinta cavo portata a `51/255`, cap esclusi dai due casi rettilinei opposti, raccordi della sorgente aggiunti per i terminali connessi, proprietà item basata sul data component, tre modelli canonici, localizzazioni e inserimento delle varianti nella scheda Redstone/ricerca.
- Comando `test --no-daemon` fuori sandbox: superato; 168 test, 0 fallimenti/errori. Il nuovo test copre segmento orizzontale/verticale, estremità, curva e diramazione.
- Nuovo `runClient --no-daemon`: caricamento risorse completato senza il precedente warning `eln:item/six_node_component`; processo terminato manualmente dopo l'osservazione dei log. La finestra resta indisponibile all'automazione, quindi serve ancora una nuova evidenza visiva in gioco.
- Nuovo `runGameTestServer --no-daemon`: superato in 14 s, 10/10 GameTest; la registrazione creative comune non introduce dipendenze client sul dedicated server.
- Smoke test visivo manuale post-correzione: screenshot fornito dall'utente e giudicato conforme per la scena mostrata; osservati cavi scuri e continui, assenza dei cap sui segmenti rettilinei, cap conservati su estremità/diramazioni e raccordi chiusi verso sorgente e resistore. Esito: superato per questa configurazione; non sostituisce il confronto affiancato completo con la 1.24.8 su tutte le facce e rotazioni.
- Nuova evidenza su spigolo esterno: lo screenshot mostra la mancata unione fra cavo superiore e laterale. L'audit legacy conferma che non è comportamento normale: il target diagonale è a `pos + face + edge` sulla faccia `edge.opposite`.
- Correzione applicata nel grafo e nel renderer; fixture aggiornata per richiedere la faccia opposta e rifiutare quella precedente.
- Comando mirato `test --tests mods.eln.node.six.SixNodeElectricalGraphTest --tests mods.eln.client.render.SixNodeBlockEntityRendererTest --no-daemon`: non eseguito, perché il client Minecraft aperto (`PID 25504`) bloccava `build/moddev/artifacts/neoforge-21.1.250.jar` durante `createMinecraftArtifacts`. Nessun test è stato contato come superato per questa correzione.
- Dopo la chiusura del client, lo stesso comando mirato è superato in 22 s. La fixture diagonale crea la connessione sulla faccia `edge.opposite` e verifica che la vecchia faccia `edge` non sia considerata vicina.
- Comando `runGameTestServer --no-daemon`: superato in 14 s, 10/10 GameTest.
- Comando `build --no-daemon`: superato in 12 s; suite completa invariata a 168 test, 0 fallimenti/errori. Resta da ripetere soltanto la prova visiva dello spigolo nel client riavviato.
- Screenshot successivo: la connessione è presente ma mostra un gradino sullo spigolo. L'audit di `CableRenderTypeMethodType.Etend/Internal` conferma che la 1.24.8 modifica un solo braccio di `heightPixel/16`; portata la stessa scelta usando l'ordine numerico legacy delle facce.
- Nuovo comando mirato dopo la rifinitura: non eseguito perché Minecraft era nuovamente aperto (`PID 43340`) e bloccava `neoforge-21.1.250.jar` durante `createMinecraftArtifacts`. Nessuna asserzione della rifinitura è stata ancora eseguita.
- Dopo la chiusura del client, comando `test --tests mods.eln.client.render.SixNodeBlockEntityRendererTest --tests mods.eln.node.six.SixNodeElectricalGraphTest --no-daemon`: superato in 18 s; il nuovo test verifica che per ogni coppia considerata un solo braccio riceva l'estensione o l'accorciamento legacy.
- Comando `build --no-daemon`: superato in 16 s; report XML con 169 test, 0 fallimenti/errori.
- Comando `runGameTestServer --no-daemon`: superato in 17 s; tutti i 10 GameTest richiesti passano. La conferma visiva in-world della curva rifinita resta aperta.
- Smoke test visivo finale dello spigolo esterno: superato. Lo screenshot dell'utente mostra continuità sui tre segmenti, raccordi aderenti allo spigolo e assenza del precedente gradino; P-022 chiuso per la configurazione provata.

## Modello di registrazione

```text
### AAAA-MM-GG — Titolo

- Revisione/stato:
- Ambiente:
- Comando o procedura:
- Esito: superato | fallito | parziale
- Risultato osservato:
- Log/artifact:
- Problema collegato:
```

Per le prove manuali indicare mondo, lato client/server, sequenza delle azioni e risultato atteso. “Si avvia” non sostituisce una prova dedicated server.
