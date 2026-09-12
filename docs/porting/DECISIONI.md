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
- Conseguenze: la politica concreta di partizionamento per livello/chunk resta da decidere in M2; componenti rimossi sono esclusi dalla traversata anche se un oggetto `State` conserva temporaneamente il vecchio collegamento.
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
