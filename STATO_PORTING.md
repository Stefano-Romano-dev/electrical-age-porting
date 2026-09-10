# Stato del porting di Electrical Age

Ultimo aggiornamento: 8 settembre 2026.

Questo documento tiene traccia del lavoro completato, delle verifiche effettuate e delle attività ancora necessarie. Va aggiornato dopo ogni modifica significativa.

## Legenda

- `[x]` completato e verificato
- `[~]` implementato parzialmente o ancora da verificare
- `[ ]` non iniziato
- `[!]` problema noto o decisione necessaria

## Obiettivo tecnico

- Minecraft 1.21.1
- NeoForge 21.1.x
- Java 21
- Mod ID `eln`
- Primo supporto esclusivamente NeoForge
- Nessuna compatibilità con i mondi di Minecraft 1.7.10
- Simulatore separato dalle API Minecraft e NeoForge

## Struttura del workspace

- `original/`: sorgente Electrical Age 1.7.10, conservato senza modifiche come riferimento.
- `modern/`: nuovo port per Minecraft 1.21.1.
- `ANALISI_PORTING.md`: analisi iniziale, scelta della piattaforma e rischi principali.
- `STATO_PORTING.md`: questo registro operativo.

## Infrastruttura e build

- [x] Creato il progetto da template ufficiale NeoForge 1.21.1 ModDevGradle.
- [x] Configurato Java 21.
- [x] Configurato Minecraft 1.21.1.
- [x] Configurato NeoForge 21.1.249.
- [x] Configurati Parchment mappings `2024.11.17`.
- [x] Impostati mod ID `eln`, package `net.electricalage.eln` e nome Electrical Age.
- [x] Impostata la versione iniziale `0.1.0-alpha.1`.
- [x] Rimossi classi e asset dimostrativi del template.
- [x] Aggiunto JUnit 5.
- [x] Aggiunta la licenza al JAR generato.
- [x] Build completa eseguita con successo.
- [x] Client di sviluppo avviato con successo.
- [x] Electrical Age riconosciuto e inizializzato senza errori nel log.
- [ ] Verifica su server dedicato.
- [ ] Pipeline CI.
- [ ] Pubblicazione automatica degli artefatti.

## Simulatore elettrico indipendente

- [x] Creata l'astrazione `CircuitNode`.
- [x] Creato il componente `Resistor`.
- [x] Creato il componente `VoltageSource`.
- [x] Creato `DcCircuit` con analisi nodale modificata per circuiti DC.
- [x] Implementata soluzione di sistemi lineari con pivoting parziale.
- [x] Creato `DcSolution` per tensioni, correnti e potenza.
- [x] Testato un partitore da 12 V con risultato atteso di 8 V.
- [x] Testate corrente e potenza dei resistori.
- [x] Testata la corrente della sorgente.
- [x] Testato il rilevamento di un circuito flottante senza soluzione unica.
- [ ] Confronto numerico sistematico con il solver originale.
- [ ] Resistori con valore modificabile nel mondo.
- [ ] Condensatori e simulazione transitoria.
- [ ] Induttori.
- [ ] Sorgenti di corrente.
- [ ] Trasformatori.
- [ ] Interruttori e componenti non lineari.
- [ ] Simulazione termica.
- [ ] Protezioni, sovracorrente e distruzione termica.
- [ ] Profilazione di reti grandi.

## Sistema SixNode moderno

- [x] Creato il blocco contenitore tecnico `eln:six_node`.
- [x] Creata la relativa block entity.
- [x] Aggiunte sei posizioni indipendenti, una per ogni direzione Minecraft.
- [x] Trasformato `eln:resistor` in un componente-item installabile.
- [x] Installazione del resistore sulla faccia cliccata.
- [x] Possibilità di collocare più resistori nello stesso spazio.
- [x] Stato visivo sincronizzato attraverso proprietà del blocco.
- [x] Salvataggio delle facce occupate nella block entity.
- [x] Rimozione del singolo resistore con Shift e clic destro a mano vuota.
- [x] Restituzione degli item quando il contenitore viene distrutto dal giocatore.
- [x] Sagoma di selezione e collisione calcolata dalle facce occupate.
- [x] Modello multipart orientato sulle sei facce.
- [x] Corretta l'inversione iniziale degli orientamenti nord e sud.
- [x] Verifica visiva in gioco con più resistori nello stesso spazio.
- [ ] Rimozione automatica di un componente quando manca il blocco di supporto.
- [ ] Drop corretto in caso di esplosione o distruzione non causata da un giocatore.
- [ ] Selezione precisa di componenti sovrapposti tramite punto colpito.
- [~] Formato generico con resistore, cavo e sorgente nello stesso SixNode; da verificare visivamente in gioco.
- [~] Orientamento locale calcolato, persistito e applicato dal renderer; pavimento e asse Z verificati, restano pareti laterali e soffitto.
- [~] Renderer dinamico della block entity basato su tipo e orientamento; i modelli sono ancora provvisori.

## Contenuti disponibili in gioco

### Resistore

- [x] Item registrato come `eln:resistor`.
- [x] Inserito nella scheda creativa Electrical Age.
- [x] Nome inglese e italiano.
- [x] Ricetta con due lingotti di rame e redstone.
- [x] Modello 3D provvisorio con corpo, terminali e bande.
- [x] Installabile su pavimento, soffitto e pareti.
- [~] Forma e dimensioni ancora provvisorie.
- [ ] Valore di resistenza configurabile.
- [ ] Inventario o interfaccia di configurazione.
- [~] Definiti due terminali sui bordi opposti della faccia; manca il collegamento al grafo elettrico.
- [ ] Collegamento al solver.
- [ ] Calcolo di corrente, tensione e potenza nel mondo.
- [ ] Riscaldamento e guasto.

### Cavo elettrico minimale

- [x] Item registrato come `eln:cable` e aggiunto alla scheda creativa.
- [x] Ricetta provvisoria con tre lingotti di rame per otto cavi.
- [x] Modello 3D provvisorio distinto.
- [x] Installabile come componente del SixNode.
- [x] I due terminali appartengono alla stessa rete elettrica.
- [ ] Verifica visiva in gioco e persistenza manuale.

### Sorgente di tensione minimale

- [x] Item registrato come `eln:voltage_source` e aggiunto alla scheda creativa.
- [x] Ricetta provvisoria con rame, ferro e un blocco di redstone.
- [x] Modello 3D provvisorio con poli rosso e nero.
- [x] Installabile come componente del SixNode.
- [x] Valore nominale iniziale impostato e persistito a 12 V.
- [ ] Verifica visiva in gioco e persistenza manuale.
- [ ] Applicazione dei 12 V al solver DC.

## Risorse e grafica

- [x] Creato un modello JSON provvisorio compatibile con il renderer vanilla.
- [x] Evitate texture mancanti durante il caricamento del client.
- [x] Verificato il multipart sulle diverse facce.
- [ ] Valutazione del caricamento dei vecchi OBJ.
- [ ] Conversione o ricostruzione di `PowerElectricPrimitives`.
- [ ] Texture definitiva del resistore.
- [ ] Standard per modelli e orientamenti degli altri componenti.
- [ ] Particelle di rottura appropriate.
- [ ] Icona e logo moderni della mod.

## Persistenza e rete

- [x] Persistenza iniziale delle facce occupate dal SixNode.
- [x] Pacchetto standard di aggiornamento della block entity.
- [x] Rendering dei componenti pilotato dallo stato del blocco.
- [x] Formato NBT iniziale versionato per i componenti montati.
- [x] Identificatore stabile del tipo di componente per ogni faccia.
- [~] Persistenza del valore nominale del resistore; lo stato elettrico non esiste ancora.
- [~] Registrazione e rimozione dei `SixNode` durante caricamento e scaricamento; da verificare con reti distribuite su più chunk.
- [x] Grafo elettrico ricostruito in modo deterministico dai componenti caricati.
- [ ] Payload di rete specifici di Electrical Age.
- [ ] Validazione dei pacchetti inviati dal client.

## Prima rete elettrica giocabile

- [x] Definita l'astrazione dei terminali elettrici nello spazio locale della faccia, indipendente dalle API Minecraft.
- [x] Assegnati due terminali opposti a ogni resistore in base al suo orientamento.
- [x] Implementato il rilevamento dei collegamenti fra componenti adiacenti e la raccolta automatica dal mondo.
- [x] Creato un gestore server-side separato per ogni dimensione.
- [x] Ricostruzione del grafo quando un componente viene aggiunto, modificato, rimosso, caricato o scaricato.
- [ ] Aggiungere un cavo elettrico minimale.
- [ ] Aggiungere una sorgente di tensione minimale.
- [ ] Collegare resistore, cavo e sorgente al solver DC.
- [ ] Eseguire la simulazione sul tick del server.
- [ ] Sincronizzare verso il client soltanto i risultati necessari.
- [ ] Mostrare tensione e corrente al giocatore.
- [ ] Salvare e ricaricare una rete funzionante.
- [ ] Verificare il circuito in multiplayer e su server dedicato.

## Porting futuro dei sistemi originali

- [ ] Cavi elettrici e segnali.
- [ ] Lampade e illuminazione.
- [ ] Batterie e caricabatterie.
- [ ] Generatori e pannelli solari.
- [ ] Trasformatori e conversione di tensione.
- [ ] Relè, interruttori, diodi e fusibili.
- [ ] Motori e sistema meccanico.
- [ ] Macchine e lavorazione dei materiali.
- [ ] Minerali e generazione del mondo.
- [ ] GUI e strumenti di misura.
- [ ] Suoni.
- [ ] Tutorial e documentazione in gioco.
- [ ] Integrazione Jade.
- [ ] Integrazione CC:Tweaked.
- [ ] Compatibilità energetica NeoForge tramite `IEnergyStorage`.
- [ ] Compatibilità fluidi tramite `IFluidHandler`.
- [ ] Compatibilità inventari tramite `IItemHandler`.

## Problemi e limiti noti

- [!] Il resistore non partecipa ancora a una rete elettrica.
- [!] Il modello corrente è provvisorio e più grande del componente originale.
- [!] I tre tipi di componente sono registrati, ma i nuovi modelli e il renderer dinamico attendono verifica visiva.
- [!] Il grafo elettrico viene aggiornato dal mondo, ma non è ancora trasformato in un circuito risolvibile da `DcCircuit`.
- [!] La rimozione con Shift e clic destro usa la faccia colpita; casi con geometrie sovrapposte richiederanno un ray trace più preciso.
- [!] Non è ancora stato verificato cosa accade quando viene distrutto il blocco che sostiene un componente.
- [!] Non è stata ancora eseguita una prova su server dedicato.
- [!] Gli asset originali sono distribuiti con licenza CC BY-NC-SA 3.0 e richiedono attribuzione, con limitazione all'uso commerciale.

## Verifiche eseguite

- [x] `gradlew build`
- [x] Test JUnit: 11 eseguiti, 0 errori, 0 fallimenti.
- [x] Testata la convenzione delle direzioni locali sulle facce.
- [x] Testati i due terminali opposti del resistore.
- [x] Testata la validazione del valore di resistenza.
- [x] Testato il collegamento sullo stesso piano fra `SixNode` adiacenti.
- [x] Testato il collegamento interno fra facce adiacenti dello stesso `SixNode`.
- [x] Testata la separazione di terminali con orientamenti incompatibili.
- [x] Testata la ricostruzione della rete dopo aggiunta, modifica e rimozione dei `SixNode` caricati.
- [x] Testati i terminali della sorgente e del cavo.
- [x] Testata la continuità elettrica interna del cavo.
- [x] Avvio client con registrazione del renderer dinamico senza errori di modelli o texture nel log.
- [x] Verifica visiva su pavimento di resistore, cavo e sorgente: modelli distinti, appoggio e allineamento corretti.
- [x] Corretta e verificata manualmente l'inversione nord/sud introdotta dal renderer dinamico.
- [x] Avvio client NeoForge 21.1.249 su Minecraft 1.21.1.
- [x] Caricamento del mod `eln` versione `0.1.0-alpha.1`.
- [x] Caricamento di modello e risorse senza errori.
- [x] Verifica manuale del crafting del resistore.
- [x] Verifica manuale del primo modello 3D.
- [x] Verifica manuale di più resistori nello stesso spazio.
- [x] Verifica manuale della correzione dell'asse Z.
- [x] Verifica manuale della persistenza dei resistori con il nuovo formato generico dopo uscita e rientro nel mondo.

## Prossimo passo consigliato

La prossima attività è costruire il grafo elettrico a partire dai terminali appena definiti. Il risultato minimo atteso è un circuito chiuso composto da sorgente, cavo e resistore, costruito nel mondo e risolto dal server tramite `DcCircuit`.

Ordine suggerito:

1. verificare visivamente cavo, sorgente e orientamenti sulle facce;
2. collegare il grafo al solver DC;
3. mostrare tensione e corrente per verificare il risultato.

## Comandi utili

Dalla cartella `modern/`:

```powershell
.\gradlew.bat build
.\gradlew.bat test
.\gradlew.bat runClient
```

Artefatto generato:

```text
modern/build/libs/eln-0.1.0-alpha.1.jar
```

## Punto di ripresa della prossima sessione

Stato confermato al termine dell'8 settembre 2026:

- resistore, cavo e sorgente da 12 V sono visibili e installabili come componenti SixNode;
- il renderer dinamico distingue i tre modelli e la correzione dell'asse Z è verificata;
- persistenza del nuovo formato SixNode verificata manualmente per i resistori;
- grafo server-side aggiornato automaticamente e coperto da test;
- build completa riuscita con 11 test su 11;
- ultimo JAR: `modern/build/libs/eln-0.1.0-alpha.1.jar`.

Alla ripresa:

1. verificare rapidamente pareti laterali, soffitto e persistenza di cavo e sorgente;
2. trasformare le reti del grafo in nodi di `DcCircuit`;
3. applicare la sorgente da 12 V e il resistore al solver;
4. mostrare in gioco almeno tensione e corrente per provare il primo circuito chiuso.
