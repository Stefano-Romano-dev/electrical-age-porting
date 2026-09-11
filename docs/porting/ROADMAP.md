# Roadmap

Aggiornata: 12 settembre 2026

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

Stato: **prossimo milestone**.

Obiettivo: solver MNA e processi fisici essenziali indipendenti dal gioco.

Criteri di uscita:

- nessun import Minecraft/NeoForge nel core;
- test originali rilevanti portati;
- fixture numeriche di confronto con la 1.24.8;
- lifecycle del simulatore comandabile senza event bus;
- dipendenza matematica e packaging definiti.

## M2 — Primo SixNode verticale

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

## M3 — Networking, menu e strumenti

Obiettivo: interazione completa server-authoritative.

- payload tipizzati e validati;
- sincronizzazione client minima;
- primo menu macchina;
- multimetro o strumento equivalente;
- widget comuni;
- prova multiplayer.

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
