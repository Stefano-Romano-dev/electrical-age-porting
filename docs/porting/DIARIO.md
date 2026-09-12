# Diario del porting

Cronologia sintetica. I dettagli tecnici appartengono agli altri documenti; qui si annotano cosa è cambiato, perché e dove riprendere.

## 11 settembre 2026 — Nuova partenza

- Scelto di abbandonare il precedente tentativo e ripartire da Electrical Age 1.24.8.
- Confermato il target Minecraft 1.21.1, NeoForge e Java 21.
- Analizzati dimensioni, build, dipendenze, solver, nodi, persistenza, networking, GUI, rendering e risorse.
- Stabilito che CoFH Core non farà parte delle dipendenze del port moderno.
- Confermata la fedeltà estetica alla 1.24.8 come requisito del port, oltre alla fedeltà funzionale.
- Individuata la causa del requisito CoFH nell'originale: integrazione RF diretta e `Other.modIdTe = "Eln"`.
- Definita una roadmap basata su core indipendente e primo SixNode verticale.
- Creata la struttura documentale operativa del porting.
- Nessun codice del port implementato; `modern/` resta vuota.

Punto di ripresa: creare e verificare la baseline NeoForge del milestone M0.

## 12 settembre 2026 — Milestone M0 completato

- Creato `modern/` dall'MDK ufficiale NeoForge per Minecraft 1.21.1.
- Fissate le versioni NeoForge 21.1.250, ModDevGradle 2.0.146, Gradle 9.2.1, Parchment 2024.11.17 e Java 21.
- Impostati mod id `eln`, package `mods.eln`, metadata, licenze e traduzioni minime.
- Aggiunti un entry point minimale e un test JUnit 5 della baseline.
- Corrette la dipendenza runtime del launcher JUnit e la rinomina della licenza nel JAR.
- Superati test, clean build, avvio client e avvio dedicated server.
- Aggiunto il workflow GitHub Actions di build nella radice del repository, con Java 21 e working directory `modern/`.
- Ispezionato il JAR: nessun residuo `examplemod` e nessuna dipendenza CoFH.
- Registrata D-007: entry point Java e introduzione controllata di Kotlin nel milestone M1.

Punto di ripresa: definire il confine del modulo di simulazione e portare il primo nucleo MNA con test numerici, senza dipendenze Minecraft/NeoForge.

### Conferma manuale M0

- L'utente ha confermato che il client si avvia e che Electrical Age compare nell'elenco mod.

## 12 settembre 2026 — M1, primo slice DC del solver

- Configurato Kotlin JVM 2.4.20 senza KotlinForForge e mantenuto l'entry point NeoForge in Java.
- Incorporati Kotlin stdlib e Commons Math tramite Jar-in-Jar, con runtime aggiuntivo per i run NeoForge 1.21.1.
- Portato in Kotlin il nucleo MNA DC: subsystem, stati, componenti bipolari, resistore e sorgenti di corrente/tensione.
- Mantenuti decomposizione QR, convenzioni di segno e semantica RHS legacy per stabilire la parità prima del refactoring.
- Aggiunti cinque test numerici e un test automatico di isolamento dalla piattaforma.
- Superati test, build forzata e dedicated server con caricamento del runtime Kotlin 2.4.20.
- Registrati D-008 sul metodo di porting, P-008 sulla sovrascrittura del RHS e P-009 sul lock Windows della clean build.
- Dopo la chiusura del client manuale, identificato e arrestato il relativo daemon Gradle rimasto attivo; la clean build M1 è stata poi superata con accesso coerente agli output e P-009 è stato chiuso.

Punto di ripresa: portare `RootSystem`, generazione dei sottosistemi e lifecycle di aggiunta/rimozione, poi linee e inter-system.

## 12 settembre 2026 — M1, lifecycle base di RootSystem

- Portato `RootSystem` come oggetto posseduto esplicitamente, senza dipendenze da Minecraft, NeoForge o event bus.
- Implementate generazione deterministica dei sottosistemi con confini privati, rottura e rigenerazione della topologia, oversampling pre-step, flush e distruttori.
- Estesi stati e componenti con gli hook minimi necessari al lifecycle.
- Impedita la riscoperta di un componente rimosso attraverso collegamenti obsoleti ancora presenti nello stato.
- Aggiunti nove test dedicati; l'intera suite contiene 16 test superati.
- Registrata D-009 sull'ownership e sulla ricostruzione deterministica delle reti.

Punto di ripresa: portare linee e collegamenti inter-system con le relative fixture legacy, senza anticipare il refactoring numerico.

## 12 settembre 2026 — M1, linee e inter-system

- Portata la compressione delle catene resistive tramite `Line`, incluso il flush delle tensioni intermedie e il ripristino della topologia al break.
- Portati `IAbstractor`, `InterSystem`, `InterSystemAbstraction`, `VoltageStateLineReady` e il calcolo equivalente di Thévenin.
- Conservati il limite legacy di 100 stati e la priorità degli stati che devono restare lontani dai bridge inter-system.
- Verificata la convergenza numerica dell'esempio originale a due reti, anche dopo una modifica dinamica del carico.
- Registrata D-010 per mantenere queste astrazioni prima di qualunque sostituzione del solver.

Punto di ripresa: portare condensatore, induttore e componenti/processi dinamici essenziali con fixture temporali della 1.24.8.

## 12 settembre 2026 — Contratto di fedeltà rafforzato

- L'utente ha chiarito che il port completo non deve presentare differenze percepibili rispetto alla 1.24.8 durante il gioco.
- Registrata D-011 e creato `FEDELTA.md` come matrice permanente delle prove di parità e delle sole differenze esplicitamente autorizzate.
- Rafforzate le istruzioni del workspace: niente miglioramenti, semplificazioni o correzioni legacy silenziose.

Punto di ripresa invariato: componenti dinamici essenziali, misurando anche la parità temporale oltre a quella numerica.

## 12 settembre 2026 — M1, primo nucleo dinamico

- Portati condensatore, induttore e delay senza dipendenze dalla piattaforma.
- Conservati integrazione backward Euler, nome legacy `coulombs`, corrente del condensatore sempre zero e accumulo interno del delay.
- Aggiunte sette fixture per energia, lifecycle e traiettorie temporali RC/RL.
- La persistenza della corrente dell'induttore resta esplicitamente da collegare al formato moderno quando verrà introdotto lo stato del mondo.

Punto di ripresa: switch resistivo, trasformatore, sorgente di potenza e processi accoppiati rimanenti del nucleo MNA.

## 12 settembre 2026 — M1, componenti MNA avanzati

- Portati switch resistivo, trasformatore, sorgente di potenza monopolo e processi bipolo/inter-system.
- Conservati formule Thévenin, ordine dei clamp, fallback NaN e mancata invalidazione automatica del rapporto del trasformatore.
- Registrato P-010 per impedire che quest'ultima particolarità venga corretta senza una decisione esplicita.
- Aggiunte fixture per trasformazione, potenza costante, limiti, NaN e rimozione dei processi root.

Punto di ripresa: audit dei sorgenti e test MNA ancora mancanti, poi primo strato fisico puro sopra il solver.

## 12 settembre 2026 — M1, audit di completezza MNA

- Confrontati tutti i tipi del package legacy con il port: raggiunta la copertura strutturale 30/30.
- Aggiunti `Monopole`, snapshot diagnostico e accessori di ispezione del sottosistema.
- Aumentata la copertura a 49 test con coefficienti diretti di matrice e RHS.
- Corrette divergenze della traduzione su pin, duplicati di connessione, current source, gestione errori QR e riscoperta dei componenti non scollegati.
- Registrato P-011 e creato `MNA_AUDIT.md` con schemi persistenti e criteri ancora aperti.

Punto di ripresa: analizzare i chiamanti puri in `mods.eln.sim` e portare il primo strato fisico sopra MNA.

## 12 settembre 2026 — M1, primo strato elettrico e termico

- Portati i carichi elettrici con resistenza seriale, i bridge inter-system e la convenzione legacy per la corrente osservata.
- Portati carichi, connessioni e resistori termici con accumulatori e coordinate, più i processi base che convertono la dissipazione elettrica in calore.
- Estratto il passo termico completo in un esecutore puro, lasciando lo scambio stanza dietro un adapter per il futuro layer NeoForge.
- Conservata e testata la particolarità per cui `movePowerTo` aggiunge a `PspTemp` una potenza con segno; registrato P-012.
- Aggiunte 19 fixture; suite da 68 test e clean build entrambe superate.

Punto di ripresa: portare inizializzatori termici e processi energetici puri successivi, poi ricostruire lo scheduler multi-rate del `Simulator` senza event bus.
