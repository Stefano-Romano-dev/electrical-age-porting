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
