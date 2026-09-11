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
