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
| Condensatore, induttore e delay | energia e stato interno coperti | traiettorie RC/RL e primi campioni delay coperti | non applicabile | non applicabile | codec induttore testato; save/reload del nodo in M2 | test automatici M1 | parziale |
| Switch, trasformatore e sorgenti di potenza | rapporti, limiti, fallback e potenza coperti | regolazione pre-step coperta | non applicabile | non applicabile | codec legacy di switch e sorgenti testati | test automatici M1 | parziale |
| Primo strato elettrico/termico | resistenze seriali, trasferimenti termici, dispersione e calore Joule coperti | ordine del passo e integrazione a `dt` coperti | non applicabile | non applicabile | adapter stanza e persistenza da integrare | 19 test automatici M1 | parziale |
| Scheduler e inizializzazione termica | formule di stabilità, forno, diodo e utility numeriche coperti | multi-rate, fasi slow, startup e fase tick server collegate | non applicabile | non applicabile | owner per server verificato all'avvio; handler start/tick/stop coperti | 18 test core + test lifecycle + prova dedicated M1 | parziale |
| Curve, batteria e regolatori | curve, carica, vita, calore, PID/on-off e soglie coperti | aging e integrazione energia coperti | non applicabile | distruzione concreta da integrare | snapshot e codec `CompoundTag` verificati | 22 test core + codec M1 | parziale |
| Watchdog e timer | soglie, categorie, callback e diagnostica coperti | casualità, timeout, joker, delay e autorimozione coperti | non applicabile | esplosione/rimozione blocco da integrare | policy server da collegare | 18 test automatici M1 | parziale |
| SixNode e primo circuito DC M2 | sei facce, montaggio atomico, supporto opaco, selezione/rimozione/drop; terminali orientati; carico LV e sorgente `0,0125 Ω`; resistore vuoto `0,01 Ω`; circuito 50→0 V a `1428,5713469 A` | orientamento base e rotazione resistore `left()`, perdita supporto, lifecycle e ricostruzione elettrica coperti | sagoma fedele; renderer sulle sei facce, cavo `1,95/16` × `0,95/16`, tinta/cap e raccordi `Extend/Internal` legacy, OBJ, texture e tre modelli item canonici; scena automatizzata con 18 montaggi verificata | suono/particelle di piazzamento e rottura rinviati al catalogo M4 | BE/update packet, `ItemStack` e parametri finiti opzionali; riapertura regione, chunk unload/reload e sync dedicated→client coperti | 169 test automatici + 11 GameTest + harness persistenza + probe multiplayer M2 | coperto per M2 |
| Configurazione sorgente M3 | conserva `voltage`, nessun clamp arbitrario; valori non finiti rifiutati al confine server | invio a ogni nuovo valore numerico valido come GUI legacy | primo menu a campo singolo localizzato; texture GUI legacy non applicabile perché l'originale usava il pannello generico | non applicabile | payload server-bound vincolato a menu/posizione/faccia/distanza; update BE di ritorno verificato | codec payload + persistenza + probe dedicated→client a `123,5 V` | coperto per il primo slice M3 |

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
