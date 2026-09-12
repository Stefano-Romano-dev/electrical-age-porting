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
