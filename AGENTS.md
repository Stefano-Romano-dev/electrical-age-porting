# Istruzioni del workspace Electrical Age

## Obiettivo

Portare Electrical Age 1.24.8 da Minecraft Forge 1.7.10 a Minecraft 1.21.1 con NeoForge e Java 21.

## Confini del workspace

- `original/` è la sorgente legacy di riferimento e deve essere trattata come sola lettura, salvo richiesta esplicita dell'utente.
- `modern/` contiene esclusivamente il nuovo port. Non recuperare automaticamente il precedente tentativo cancellato.
- Il mod id moderno è `eln`.
- CoFH Core non è una dipendenza del port. Eventuali ponti energetici verso altre mod devono essere adapter opzionali.
- Non è richiesta compatibilità diretta con mondi 1.7.10 nel primo port. Conservare comunque id e schemi legacy utili a un eventuale importer futuro.
- Il repository Git effettivo è la radice del workspace. Esistono modifiche e cancellazioni preesistenti: non ripristinarle e non alterare lavoro estraneo.

## Principi tecnici

- Preservare il comportamento e i risultati numerici del solver prima di rifattorizzarlo.
- Preservare anche l'identità estetica originale: geometrie, proporzioni, texture, palette, animazioni, GUI, particelle e suoni. Le sostituzioni provvisorie non sono asset finali.
- Considerare Electrical Age 1.24.8 il riferimento canonico dell'esperienza: a port completato il giocatore non deve percepire differenze nel comportamento, nei tempi, nelle interazioni, nella progressione o nella presentazione del mod, salvo eccezioni imposte dalla piattaforma e documentate.
- Non correggere, semplificare o reinterpretare automaticamente comportamenti e particolarità legacy. Ogni differenza intenzionale richiede approvazione esplicita dell'utente e registrazione in `docs/porting/FEDELTA.md`.
- Tenere il core di simulazione indipendente da Minecraft, NeoForge, rendering e networking.
- Evitare singleton globali legati al server; rendere esplicita l'ownership per server, livello e chunk.
- Usare id namespaced stabili al posto di metadata e ItemStack damage legacy.
- Implementare prima vertical slice complete e verificabili, poi ampliare il catalogo.
- Separare rigorosamente codice client e common/server e verificare sempre il dedicated server.
- Registri, ricette, tag, loot e worldgen devono seguire i sistemi data-driven di NeoForge 1.21.1.
- Non rendere obbligatorie integrazioni con mod esterne durante il primo milestone.
- Modernizzare le API e l'implementazione, non ridisegnare arbitrariamente l'aspetto o l'esperienza del mod.

## Disciplina di documentazione

Consultare [l'indice della documentazione](docs/porting/README.md) prima di modifiche significative.

Dopo ogni modifica significativa:

1. aggiornare `STATO_PORTING.md` con lo stato reale, senza segnare attività non verificate;
2. registrare in `docs/porting/VERIFICHE.md` i comandi eseguiti e il loro esito;
3. aggiungere al `docs/porting/DIARIO.md` una nota sintetica e datata;
4. aggiornare decisioni, problemi, inventario o risorse soltanto se la modifica li riguarda;
5. mantenere `ANALISI_PORTING.md` come analisi consolidata, non come diario giornaliero.

Le decisioni architetturali non banali devono indicare contesto, scelta, motivazione e conseguenze in `docs/porting/DECISIONI.md`.

## Verifica minima

Quando esisterà la build moderna, ogni milestone deve includere in proporzione al rischio:

- test unitari del core;
- build completa;
- avvio client;
- avvio dedicated server;
- GameTest o verifica manuale per il comportamento nel mondo;
- save/reload e chunk unload/reload per lo stato persistente;
- prova multiplayer per networking o sincronizzazione.
- confronto di parità con la 1.24.8 secondo `docs/porting/FEDELTA.md`, includendo evidenze visive o numeriche pertinenti.

Annotare sempre il comando esatto e l'esito. Non descrivere una verifica come superata se non è stata eseguita nella versione corrente del codice.

## Sandbox e build

- Impostare `GRADLE_USER_HOME` su `modern/.gradle` durante le esecuzioni locali dell'agente.
- La compilazione Kotlin usa `kotlin.compiler.execution.strategy=in-process`: il daemon Kotlin tenta altrimenti di scrivere marcatori sotto `AppData` e può causare `AccessDeniedException` o lock persistenti nella sandbox Windows.
- Se output sotto `modern/build` sono stati creati da un'esecuzione elevata, anche `clean` può richiedere lo stesso livello di accesso per rimuoverli.
