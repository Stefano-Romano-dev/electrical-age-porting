# Contratto di fedeltà alla 1.24.8

Electrical Age 1.24.8 è il riferimento canonico del port. L'obiettivo finale è che un giocatore esperto dell'originale non percepisca differenze attribuibili al mod durante il normale gioco su Minecraft 1.21.1.

## Ambiti vincolanti

Per ogni contenuto devono essere preservati, quando applicabili:

- comportamento elettrico, termico, meccanico e logico, compresi risultati numerici e condizioni limite;
- velocità, ritardi, frequenze, animazioni, transizioni e ordine degli eventi percepibili;
- piazzamento, orientamento, collisioni, connessioni, interazioni, strumenti e feedback;
- costi, ricette, progressione, bilanciamento, drop e condizioni di guasto;
- geometrie, proporzioni, texture, UV, palette, emissività, particelle e illuminazione;
- GUI, disposizione, scala, icone, testi, controlli e flusso d'uso;
- suoni, volume, pitch, loop, attenuazione e sincronizzazione;
- stato persistente, save/reload, chunk reload e comportamento multiplayer;
- integrazioni esterne previste, tramite adapter opzionali anziché dipendenze obbligatorie.

Una compilazione riuscita o una somiglianza generica non costituiscono prova di fedeltà.

## Regola sulle differenze

- Modernizzare API, formati, registri e implementazione interna non autorizza cambiamenti percepibili.
- Le particolarità legacy vengono inizialmente conservate, anche quando sembrano correggibili.
- Una correzione, semplificazione o modifica di bilanciamento richiede una proposta separata e l'approvazione esplicita dell'utente.
- Ogni differenza inevitabile dovuta a Minecraft 1.21.1 o NeoForge deve essere ridotta al minimo, documentata qui e coperta da una verifica equivalente.
- Crash, perdita dati, sicurezza o incompatibilità tecnica non risolvibile devono essere segnalati prima di scegliere un comportamento alternativo.

## Metodo di accettazione

Prima di dichiarare `verificato` un contenuto si raccolgono evidenze proporzionate:

1. scenario riproducibile sulla 1.24.8;
2. valori, tempi o sequenza di riferimento;
3. stessa prova sul port moderno;
4. confronto visivo affiancato per tutto ciò che viene renderizzato;
5. confronto audio quando sono presenti suoni;
6. save/reload, chunk reload e multiplayer quando lo stato o il networking sono coinvolti;
7. registrazione dell'esito nella matrice seguente e in `VERIFICHE.md`.

Le tolleranze numeriche devono essere motivate e sufficientemente strette da non produrre differenze di gameplay.

## Matrice di parità

| Sistema o contenuto | Logica/numeri | Tempi | Aspetto/GUI | Audio/particelle | Persistenza/rete | Evidenza | Stato |
|---|---|---|---|---|---|---|---|
| Solver MNA DC iniziale | fixture legacy presenti | non applicabile | non applicabile | non applicabile | non applicabile | test automatici M1 | parziale |
| Lifecycle, Line e inter-system | fixture strutturali e numeriche presenti, incluso teardown pin legacy | oversampling legacy coperto | non applicabile | non applicabile | non applicabile | test automatici M1 e `MNA_AUDIT.md` | parziale |
| Condensatore, induttore e delay | energia e stato interno coperti | traiettorie RC/RL e primi campioni delay coperti | non applicabile | non applicabile | persistenza induttore da integrare | test automatici M1 | parziale |
| Switch, trasformatore e sorgenti di potenza | rapporti, limiti, fallback e potenza coperti | regolazione pre-step coperta | non applicabile | non applicabile | adapter di persistenza da integrare | test automatici M1 | parziale |
| Primo strato elettrico/termico | resistenze seriali, trasferimenti termici, dispersione e calore Joule coperti | ordine del passo e integrazione a `dt` coperti | non applicabile | non applicabile | adapter stanza e persistenza da integrare | 19 test automatici M1 | parziale |
| Scheduler e inizializzazione termica | formule di stabilità, forno, diodo e utility numeriche coperti | multi-rate, fasi slow e startup coperti | non applicabile | non applicabile | tick NeoForge e stato persistente da integrare | 18 test automatici M1 | parziale |

`parziale` significa che l'area coperta è equivalente per le prove indicate, ma il sistema complessivo non è ancora completo.

## Differenze già autorizzate

| Area | Differenza | Motivo | Impatto percepibile previsto |
|---|---|---|---|
| Piattaforma | Minecraft 1.21.1, NeoForge e Java 21 al posto di Forge 1.7.10 | obiettivo del port | interfaccia base di Minecraft moderna; il mod deve restare fedele |
| Identificatori interni | id namespaced al posto di metadata/damage | requisito delle API moderne | nessuno nel normale gameplay |
| CoFH Core | non obbligatorio; integrazioni energetiche come adapter opzionali | D-003 | nessuno senza altre mod; con adapter il comportamento dovrà essere equivalente |
| Mondi legacy | caricamento diretto dei mondi 1.7.10 escluso dal primo port | obiettivo concordato | i vecchi salvataggi richiederanno un eventuale importer futuro |

## Registro delle differenze intenzionali

| Id | Contenuto | Differenza proposta | Stato approvazione | Evidenza/decisione |
|---|---|---|---|---|
| — | — | — | nessuna | — |
