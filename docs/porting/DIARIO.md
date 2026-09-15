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

## 12 settembre 2026 — M1, scheduler multi-rate puro

- Portato il `Simulator` come owner indipendente dall'event bus, conservando accumulatori temporali, oversampling MNA e ordine delle fasi legacy.
- Collegata la validazione degli inizializzatori al periodo termico dello scheduler, eliminando il precedente accesso globale a `Eln.simulator`.
- Portati forno, diodo, conversione resistiva e utility di integrazione/derivazione.
- Fissati P-013 sul doppio solve del primo tick e P-014 sul reset incompleto del differenziatore, entrambi preservati per fedeltà.
- Aggiunte 18 fixture; suite da 86 test e clean build superate.

Punto di ripresa: portare curve numeriche, batteria, invecchiamento configurabile e regolatori, separando lo stato persistente dagli adapter NeoForge.

## 12 settembre 2026 — M1, curve, batterie e regolatori

- Portate le tabelle di funzione con interpolazione, estrapolazione, protezione Y e duplicazione legacy.
- Portate batteria e aging con policy esplicita al posto del singleton `SaveConfig`; la distruzione resta un confine astratto.
- Portati regolatore None/Manual/OnOff/Analog e adapter termici, mantenendo le formule PID non convenzionali.
- Aggiunti snapshot puri e registrati gli schemi NBT legacy senza importare API Minecraft nel core.
- Registrati P-015, P-016 e P-017 per impedire correzioni silenziose di sovraccarica, derivata e cache della curva.
- Aggiunte 22 fixture; suite da 108 test e clean build superate.

Punto di ripresa: portare watchdog/distruzione puri, auditare i processi M1 ancora mancanti e progettare gli adapter `CompoundTag` fuori dal core.

## 12 settembre 2026 — M1, watchdog e audit processi

- Portata la logica comune dei watchdog con policy e casualità iniettate, preservando joker, timeout, recupero e trip ripetuti.
- Portati watchdog elettrici, resistivi e termici con telemetria/dump astratti, più distruzione ritardata e `TimeRemover` con owner esplicito.
- Rimandati senza placeholder `WorldExplosion` e watchdog meccanico ai rispettivi layer.
- Creato `PROCESS_AUDIT.md`: 32 dei 42 tipi legacy esaminati hanno ora una controparte pura; i 10 restanti hanno una destinazione motivata.
- Registrati P-018 e P-019 per reset incompleto e ripetizione della distruzione.
- Aggiunte 18 fixture; suite da 126 test e clean build superate.

Punto di ripresa: implementare codec `CompoundTag` esterni al core, collegare il tick server NeoForge a un owner esplicito e verificare il lifecycle su dedicated server.

## 12 settembre 2026 — M1, persistenza di piattaforma e lifecycle server

- Implementati codec `CompoundTag` esterni al core per gli stati persistenti già auditati, mantenendo schema e precisione della 1.24.8.
- Conservata e testata l'anomalia di `NbtResistor`, registrata come P-020 invece di correggerla silenziosamente.
- Abilitato il supporto unit test ModDevGradle, necessario per usare il vero `CompoundTag`; adattato il test di isolamento al diverso working directory del runner.
- Collegato il simulatore puro a `ServerTickEvent.Pre` con ownership per identità di server e cleanup previsto su `ServerStoppingEvent`; registrata D-016.
- Suite da 134 test e build completa superate; dedicated server arrivato a `Done` e owner creato.
- La rimozione allo stop non è stata osservata perché la console Gradle non inoltrava il comando al processo Minecraft; questa verifica resta esplicitamente aperta.

Punto di ripresa: osservare il callback di stop con un harness affidabile, poi iniziare M2 dalla mappa degli id e dallo scheletro persistente del SixNode host.

## 13 settembre 2026 — M1 chiusa, M2 avviata con lo shell SixNode

- Coperti start, tick e stop di `ServerSimulationLifecycle` con gli oggetti evento NeoForge reali, verificando la rimozione dell'owner.
- Ricavati dalla registrazione 1.24.8 gli id esatti dei tre componenti della prima vertical slice e associati a id moderni stabili.
- Implementato uno shell SixNode a sei facce con mappa direzioni e rotazioni LRDU esplicite, senza dipendenze dal renderer o dal networking.
- Aggiunto un codec `CompoundTag` versionato e conservativo per identità e orientamento; payload specifici e importer legacy restano separati.
- Registrati gli asset canonici necessari, senza copiarli né introdurre placeholder estetici.
- Build completa superata con 147 test, 0 fallimenti e 0 errori.

Punto di ripresa: registrare il blocco host `eln:six_node` e la relativa block entity, collegare lo shell al lifecycle del livello e verificarne il save/reload reale prima di montare i tre dispositivi.

## 13 settembre 2026 — Host SixNode registrato nel mondo

- Registrati blocco e block entity con l'id stabile `eln:six_node` e collegati all'entry point NeoForge.
- Il block entity possiede `SixNodeContents`, espone mutazioni controllate che chiamano `setChanged()` e delega lettura/scrittura al codec versionato.
- L'host vuoto non ha ancora item, collisione o rendering: evita un modello provvisorio e resta accessibile soltanto a comandi e test finché non esistono componenti montabili.
- I test usano i registri NeoForge reali e verificano id, round-trip di due facce, rifiuto della sostituzione e conservazione dello stato con schema futuro.
- Suite da 149 test, build completa e avvio dedicated server fino a `Done` superati.

Punto di ripresa: aggiungere una prova reale di save/reload e chunk reload del block entity, quindi introdurre l'identità item/data component e il montaggio server-side del primo componente.

## 13 settembre 2026 — GameTest SixNode e sostituzione del damage item

- Aggiunto un GameTest con struttura SNBT minima, copiato nella directory runtime da un task Gradle dedicato.
- Verificato nel `ServerLevel` il ciclo piazzamento, montaggio, metadata completi, rimozione e ricostruzione vanilla del block entity; 1/1 GameTest superato.
- Registrati l'item generico `eln:six_node_component` e il data component persistente/sincronizzato `eln:six_node_component_type`.
- Aggiunta una factory di stack tipizzati che mantiene separata l'identità moderna dal catalogo e conserva id futuri sconosciuti.
- Suite salita a 152 test; GameTest server, cleanup, salvataggio chunk e build completa superati.
- Nessun modello o asset provvisorio aggiunto; item e host restano fuori dai normali flussi di gioco finché non viene portata l'interazione fedele.
- Auditato `SixNodeItem` legacy: offset della coordinata, faccia inversa, supporto opaco, rifiuto faccia occupata, esecuzione server-side, consumo solo su successo e orientamento LRDU dipendente dal giocatore per pavimento/soffitto.

Punto di ripresa: auditare il piazzamento 1.24.8 e implementare il montaggio server-authoritative del cavo con consumo stack e gestione atomica della faccia occupata, quindi aggiungere una prova di riapertura del mondo da disco.

## 13 settembre 2026 — Primo cavo montabile

- Sostituito l'item contenitore passivo con `SixNodeComponentItem`, mantenendo invariati registry id e data component.
- Portata per il cavo bassa tensione la sequenza legacy: offset sul blocco non sostituibile, faccia inversa, supporto non-air/opaco, controllo faccia libera, mutazione solo server e consumo dopo il successo.
- Portato il mapping di rotazione base LRDU: `UP` sui lati e orientamento dipendente dalla vista del giocatore su pavimento/soffitto.
- Le identità di sorgente e resistore restano persistibili ma vengono rifiutate all'uso finché non esiste la loro implementazione completa.
- Suite salita a 154 test; 4/4 GameTest e build completa superati. Il primo tentativo positivo ha inoltre rilevato e corretto un setup errato dell'helper, senza mascherare il fallimento.
- Nessun asset provvisorio aggiunto; feedback audio/visivo, rimozione/drop, grafo e sincronizzazione restano da portare.

Punto di ripresa: automatizzare la riapertura da disco e il chunk unload/reload con un cavo montato, poi portare rimozione/drop e ricostruzione del grafo.

## 13 settembre 2026 — Persistenza mondo verificata tra processi

- Creato il task `verifySixNodeDiskPersistence`, composto da due processi dedicated sulla stessa directory di prova isolata.
- Il primo processo crea e salva un cavo montato in un chunk lontano; il secondo riapre il file regione e ne verifica blocco, block entity, faccia, tipo e rotazione.
- Nel secondo processo il chunk viene inoltre tolto dal force-load, osservato realmente scaricato e ricaricato prima di ripetere la verifica.
- Il probe è registrato soltanto in ambiente non-production, si attiva tramite proprietà JVM di sviluppo e termina autonomamente il server; il normale runtime non espone i listener.
- La prova è passata più volte, anche riusando il mondo già creato; build completa e 154 test restano verdi.

Punto di ripresa: portare la rimozione con drop fedele e il lifecycle load/unload del componente, quindi costruire la prima rappresentazione del grafo elettrico server-side.

## 13 settembre 2026 — Rimozione e drop SixNode fedeli per faccia

- Centralizzata la regola legacy del supporto e aggiunte le sei lastre canoniche di selezione per le facce occupate.
- Portato il resolver di rottura a otto blocchi con ordine, intervalli diretti e fallback della 1.24.8, conservando intenzionalmente la selezione possibile della faccia di uscita.
- La rottura survival restituisce l'item tipizzato della sola faccia, creative non produce drop e l'host sopravvive finché contiene altri componenti.
- La perdita del supporto rimuove soltanto le facce non valide; la sostituzione esterna dell'host rilascia tutte le facce residue.
- Aggiunti update tag/packet del block entity affinché il client riceva le facce necessarie alle sagome dinamiche.
- Suite salita a 156 test; 8/8 GameTest, harness dedicated a due processi e build completa superati. Nessun asset o placeholder visivo aggiunto.

Punto di ripresa: definire ownership del grafo elettrico per livello/chunk e ricostruire il componente durante load/unload, quindi chiudere il primo circuito DC nel mondo collegandolo al solver.

## 15 settembre 2026 — Primo grafo elettrico SixNode nel mondo

- Creato un grafo runtime distinto per `ServerLevel`, posseduto dal contesto della simulazione server e popolato dai block entity SixNode caricati.
- Ogni faccia di cavo LV possiede un `ElectricalLoad` da `0,0125 Ω`; due estremità collegate formano la tratta legacy da `0,025 Ω`.
- Ricostruite adiacenze complanari, connessioni interne fra facce ortogonali e passaggi diagonali attorno agli spigoli senza scansione quadratica dell'intera rete.
- Collegati montaggio, rimozione, load, unload e stop server; il teardown rimuove le connessioni prima dei carichi per rispettare la semantica MNA preservata.
- L'harness a due processi ha fatto emergere l'ordine reale del lifecycle chunk: la soluzione finale accoda i chunk caricati e li riconcilia al `Post` tick stabile, eliminando inoltre runtime di chunk non più caricati.
- Suite finale a 162 test, 9/9 GameTest, harness dedicated con ricostruzione del grafo e build completa tutti superati.

Punto di ripresa: introdurre terminali orientati, runtime della sorgente elettrica e del resistore di potenza, quindi verificare il primo circuito DC chiuso nel mondo.

## 15 settembre 2026 — Primo circuito DC SixNode chiuso

- Reso il grafo consapevole delle porte tangenti: cavo e sorgente condividono un carico sui quattro lati, il resistore espone i soli terminali legacy destro e sinistro.
- Portati i runtime della sorgente monopolo e del resistore, con resistenze originali `0,0125 Ω`, `1e-9 Ω` e valore vuoto E12 `0,01 Ω`.
- Esteso lo stato persistente della faccia con parametri numerici opzionali e verificato il round-trip della tensione `voltage`.
- Abilitato il piazzamento degli altri due descriptor della vertical slice, conservando il `left()` specifico del resistore.
- Chiuso nel GameTest un circuito 50 V → resistore → 0 V e verificata la corrente analitica `1428,5713469 A`.
- Il reload dedicated ha scoperto un teardown tardivo del vecchio block entity: il lifecycle ora conserva l'identità dell'istanza caricata e ignora callback stantie senza forzare accessi al chunk durante l'unload.
- Verifica finale: 165 test, 10/10 GameTest, harness dedicated a due processi e build completa superati.

Punto di ripresa: portare geometrie e texture canoniche dei tre componenti, avviare il client e acquisire il primo confronto visivo con la 1.24.8; lasciare menu e networking di configurazione a M3.

## 16 settembre 2026 — Primo renderer client SixNode

- Registrato un block entity renderer esclusivamente client per tutte le sei facce, con le trasformazioni `glRotateXnRef` e LRDU ricostruite dalla 1.24.8.
- Integrati OBJ, MTL e texture originali della sorgente e del resistore; la visibilità dei gruppi conserva soltanto le parti usate dai rispettivi descriptor legacy.
- Portato il cavo come geometria procedurale con larghezza `1,95/16`, altezza `0,95/16` e texture originale; i bracci usano lo stesso contratto dei terminali del grafo per le topologie complanare, interna e diagonale.
- Il primo caricamento client ha scoperto il riferimento interno con maiuscole a `PowerElectricPrimitives.mtl`; il path è stato normalizzato e il secondo caricamento non mostra errori OBJ/MTL o blockstate ELN.
- Build completa e suite da 167 test superate; 10/10 GameTest dedicated superati senza caricare classi client.
- L'automazione UI non ha esposto la finestra Java del client, quindi non è stato acquisito né dichiarato un confronto visivo. Registrato P-021 per il modello dinamico dell'item ancora mancante.
- Gli screenshot forniti dall'utente hanno poi mostrato la tinta troppo chiara dei tratti, i cap bianchi presenti anche sui segmenti rettilinei e il raccordo assente fra sorgente e rete. Il confronto con `CableRender` ed `ElectricalSourceRender` ha portato la tinta predefinita a `0,2`, ripristinato la condizione legacy dei cap e aggiunto gli spezzoni automatici della sorgente.
- Risolto P-021 con una proprietà item client legata al tipo persistente, tre modelli/texture canonici, nomi italiano/inglese e tre stack nella scheda Redstone e nella ricerca creativa.
- Verifica aggiornata: 168 test unitari, caricamento client senza warning del modello item e 10/10 GameTest dedicated.
- Ricevuto lo screenshot post-correzione: nella scena provata i cavi sono continui e scuri, i cap compaiono solo nei punti previsti e i raccordi verso sorgente e resistore risultano chiusi. Registrato come smoke test visivo positivo, non ancora come confronto completo affiancato sulle sei facce.

Punto di ripresa: costruire una scena client riproducibile con i tre componenti sulle sei facce, confrontarla nuovamente con la 1.24.8 e procedere alla verifica multiplayer.

## 16 settembre 2026 — Correzione della curva su spigolo esterno

- Uno screenshot dell'utente ha mostrato due cavi non uniti fra piano orizzontale e verticale sullo stesso spigolo esterno.
- L'audit di `NodeBase.connectJob` e `CableRender.connectionType` ha confermato che il vicino diagonale deve esporre `edge.opposite`; il port usava erroneamente `edge` sia nel grafo sia nel renderer.
- Corretti target elettrico e ricerca visiva e riallineato il test della topologia diagonale.
- Il primo test mirato non è partito perché il client Minecraft aperto teneva bloccato `build/moddev/artifacts/neoforge-21.1.250.jar`; nessuna asserzione è stata eseguita e la verifica resta in attesa del riavvio client.
- Dopo la chiusura del client, test mirati e build completa sono superati; il dedicated conferma 10/10 GameTest. La prova ora distingue esplicitamente la faccia diagonale corretta da quella errata precedente.
- Il primo screenshot dopo la connessione mostra ancora un gradino: portata la selezione legacy `Extend/Internal` basata sull'ordine storico `WEST, EAST, DOWN, UP, NORTH, SOUTH`, con un solo braccio esteso o accorciato di `0,95/16`.
- Il test di questa rifinitura non è partito perché il client era stato riaperto e il nuovo processo Minecraft (`PID 43340`) bloccava nuovamente il JAR NeoForge.
- Dopo la seconda chiusura del client, il test mirato della scelta `Extend/Internal`, la suite completa da 169 test, la build e i 10/10 GameTest sono superati. Resta soltanto il controllo visivo aggiornato dello spigolo.
- Lo screenshot finale dell'utente conferma una curva continua e senza gradino visibile tra piano superiore, parete e piano inferiore; P-022 è chiuso.
- Creati i README pubblici inglese e italiano del repository e riallineato il README tecnico di `modern/` allo stato M2 verificato.
