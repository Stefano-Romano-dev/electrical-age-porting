# Roadmap

Aggiornata: 20 settembre 2026

## M0 — Baseline NeoForge

Stato: **completato il 12 settembre 2026**.

Obiettivo: progetto vuoto riproducibile per Minecraft 1.21.1, NeoForge e Java 21.

Criteri di uscita:

- [x] build Gradle riuscita;
- [x] mod `eln` caricata;
- [x] client avviato;
- [x] dedicated server avviato senza classloading client;
- [x] versioni della toolchain fissate e registrate;
- [x] test e comandi locali riproducibili.

## M1 — Core di simulazione

Stato: **completato il 13 settembre 2026 per il perimetro M1; i tipi dipendenti dal gameplay restano assegnati alle rispettive vertical slice**.

Obiettivo: solver MNA e processi fisici essenziali indipendenti dal gioco.

Criteri di uscita:

- [x] nessun import Minecraft/NeoForge nel core iniziale;
- [x] test originali rilevanti per il perimetro M1 portati; i tipi dipendenti da nodo, inventario, mondo e meccanica sono instradati e non simulati con placeholder;
- [x] fixture numeriche di confronto con la 1.24.8 per DC, inter-system, RC/RL, trasformazione, potenza, termica, scheduler, batteria, regolazione e watchdog;
- [x] lifecycle base del simulatore comandabile senza event bus tramite un `RootSystem` posseduto esplicitamente;
- [x] dipendenza matematica e packaging definiti per il primo slice.
- [x] primo strato fisico puro: carichi e connessioni elettriche/termiche, conversione Joule→calore e passo termico deterministico.
- [x] scheduler multi-rate puro con inizializzatori termici, fasi slow e validazione di stabilità.
- [x] curve numeriche, batteria, aging e regolatori con snapshot persistenti puri.
- [x] watchdog, timer e distruzione astratta; audit strutturale dei processi M1.
- [x] codec `CompoundTag` legacy fuori dal core, testati con il runtime NeoForge.
- [x] owner `Simulator` per istanza server e collegamento a `ServerTickEvent.Pre`.

Il lifecycle è coperto sia nel core sia sugli handler start/tick/stop con eventi NeoForge reali; la prova dedicated ha inoltre raggiunto `Done` e osservato la creazione dell'owner.

## M2 — Primo SixNode verticale

Stato: **completato il 16 settembre 2026 per il perimetro M2**.

Obiettivo: rete DC minima costruibile e persistente nel mondo.

Contenuti minimi:

- blocco host SixNode e block entity;
- registro dei tipi di componente;
- item cavo, resistore e sorgente;
- componenti montabili sulle sei facce;
- grafo server-side e collegamento al solver;
- rendering minimale;
- salvataggio, ricaricamento e ricostruzione della rete.

Criteri di uscita:

- circuito chiuso con risultato numerico verificato;
- piazzamento e rimozione aggiornano il grafo;
- save/reload e chunk unload/reload superati;
- test unitari e GameTest principali superati;
- prova client e dedicated server superata.

Avanzamento verificato:

- [x] catalogo iniziale dei tipi con id namespaced e mapping numerico legacy;
- [x] modello a sei facce e rotazione LRDU fedele;
- [x] codec versionato dello shell persistente con round-trip e compatibilità conservativa;
- [x] registrazione del blocco host e della block entity;
- [x] item contenitore e data component con identità namespaced persistente e sincronizzata;
- [x] GameTest di piazzamento e ricostruzione vanilla del block entity nel `ServerLevel`;
- [x] montaggio server-authoritative del cavo con faccia inversa, rotazione base, supporto opaco e consumo atomico;
- [x] riapertura dello stesso mondo in un secondo processo dedicated e chunk unload/reload;
- [x] selezione, rimozione per faccia, perdita supporto e drop survival/creative del primo cavo;
- [x] primo grafo elettrico server-side del cavo con ownership per livello, topologie legacy e lifecycle load/unload verificato;
- [x] terminali orientati, runtime MNA di sorgente/resistore, piazzamento e primo circuito DC numerico verificato nel mondo;
- [x] primo renderer client con geometrie e texture canoniche caricato senza errori di risorsa;
- [x] modello dinamico dell'item, nomi localizzati e varianti nella ricerca creativa;
- [x] scena visiva riproducibile dei tre componenti sulle sei facce e verifica multiplayer dedicated→client.

Esito: 169 test unitari, 11/11 GameTest, build completa, persistenza su file regione/chunk e probe multiplayer reale superati. Lo screenshot automatizzato copre i 18 montaggi del perimetro M2; non sostituisce la futura validazione dell'intero catalogo.

Prossimo slice: avviare M3 con payload tipizzati per la configurazione server-authoritative e il primo menu, mantenendo la scena M2 come regressione multiplayer.

## M3 — Networking, menu e strumenti

Stato: **in corso dal 16 settembre 2026**.

Obiettivo: interazione completa server-authoritative.

- [x] primo payload tipizzato e validato: configurazione della tensione della sorgente;
- [x] sincronizzazione client minima tramite stato persistente del block entity;
- [x] primo menu macchina: campo singolo della sorgente elettrica;
- [ ] multimetro: item, misura server e GameTest completati; clic dedicated→client dopo la correzione del dispatch da verificare;
- [x] primo widget numerico comune con parsing locale, commit su Invio/perdita focus e ripristino degli input non validi;
- [x] prima prova multiplayer del percorso menu→server→stato client.

Prossimo controllo: chiudere la prova multiplayer del clic multimetro e confrontare l'icona con la 1.24.8; inventario/termica del resistore restano fuori da questo slice.

## M4 — Catalogo fondamentale

Port progressivo per famiglie complete:

1. cavi e distribuzione;
2. misura e protezione;
3. segnali e controllo;
4. batterie e conversione;
5. generazione elettrica;
6. macchine termiche;
7. illuminazione;
8. meccanica.

Ogni famiglia comprende codice, id, ricette, tag, loot, traduzioni, modelli, suoni e test.

## M5 — Sistemi avanzati

- TransparentNode rimanenti;
- grid node e multiblocchi;
- ghost block;
- worldgen e minerali;
- entità e minecart;
- tutorial e documentazione in gioco;
- profiling e ottimizzazione di reti grandi.

## M6 — Integrazioni opzionali e API

- nuova API elettrica pubblica;
- capability/adapter energetici opzionali;
- Jade, CC:Tweaked o altre integrazioni selezionate;
- MQTT, Modbus e seriale soltanto dopo revisione di sicurezza e threading;
- valutazione di un aggiornamento dalla 1.21.1 alla linea Minecraft stabile successiva.
