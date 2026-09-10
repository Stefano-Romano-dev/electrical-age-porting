# Analisi preliminare del porting di Electrical Age

Data dell'analisi: 7 settembre 2026.

## Decisione consigliata

La piattaforma consigliata per il primo port funzionante è:

- Minecraft 1.21.1
- NeoForge
- Java 21
- Mojang mappings
- una sola piattaforma supportata inizialmente

Il simulatore dovrà comunque essere separato dalle API Minecraft e NeoForge, in modo da rendere possibile un futuro aggiornamento a una versione più recente o l'aggiunta di un altro loader.

### Decisioni confermate

- Non è richiesta la compatibilità con i mondi o i salvataggi di Electrical Age per Minecraft 1.7.10.
- Il nuovo formato di salvataggio può essere progettato liberamente per la versione moderna.
- Non verranno realizzati importatori, conversioni NBT o DataFixer per i vecchi mondi durante il porting iniziale.

## Dimensioni e stato del progetto originale

Il progetto originale è una mod Forge per Minecraft 1.7.10. Il repository è fermo al 5 dicembre 2016, sul ramo `releases/1.13`.

L'analisi statica iniziale ha rilevato:

- 828 file Java;
- 37 file Kotlin;
- circa 79.700 righe di codice;
- 564 file che importano direttamente classi Minecraft;
- 129 file che importano direttamente Forge o FML;
- 116 file che importano LWJGL;
- 133 file interessati dal rendering legacy;
- 164 file con riferimenti a NBT o ai metodi storici di serializzazione;
- 1.002 risorse per circa 77 MB;
- 117 modelli OBJ, 117 file MTL e 121 file Blender;
- una classe principale `Eln.java` di oltre 6.200 righe, che concentra registrazioni, configurazione dei dispositivi e ricette.

Il sistema di build usa ForgeGradle 1.2, compatibilità Java 6, Kotlin 1.0.5, Forge 1.7.10 e una copia inclusa di Apache Commons Math 3.3. Le integrazioni comprendono Waila, ComputerCraft, OpenComputers, IndustrialCraft e CoFH/Redstone Flux.

Questi numeri indicano che non è possibile fare un semplice aggiornamento delle importazioni. Il porting sarà una riscrittura progressiva, nella quale si recuperano il modello matematico, le regole di gioco e le risorse ancora utilizzabili.

## Motivazione della scelta di NeoForge

NeoForge conserva un modello concettuale vicino a quello del vecchio Forge: registri, event bus, block entity, capability, networking e separazione fra client e server. Le API moderne sono molto diverse da Forge 1.7.10, ma la traduzione architetturale è più diretta rispetto a Fabric.

NeoForge 1.21.1 fornisce capability standard per:

- energia tramite `IEnergyStorage`;
- fluidi tramite `IFluidHandler`;
- inventari tramite `IItemHandler`.

L'interfaccia energetica deriva dal modello Redstone Flux e costituisce quindi la sostituzione naturale delle vecchie integrazioni CoFH presenti in Electrical Age.

Il networking moderno basato su payload registrati offre inoltre una destinazione chiara per sostituire `SimpleNetworkWrapper`, `IMessage` e i vecchi handler della mod.

Forge moderno non dà un vantaggio sostanziale soltanto perché il progetto originale usava Forge: la distanza fra Forge 1.7.10 e Forge 1.21.1 rimane enorme. NeoForge ha avuto una forte adozione sulla 1.21.1 e offre le astrazioni più utili a una mod tecnica di questo tipo.

Fabric rimane una possibilità futura, ma per la prima versione richiederebbe più decisioni e più adattatori, soprattutto per energia, fluidi, inventari e integrazioni con altre mod. Non è consigliato introdurre Architectury o un'altra astrazione multiloader prima di aver ottenuto una versione NeoForge giocabile.

## Motivazione della scelta di Minecraft 1.21.1

Minecraft 1.21.1 è stata una versione stabile e molto adottata nell'ecosistema NeoForge. Offre una base conosciuta, documentazione consolidata e una vasta disponibilità di mod compatibili.

La serie Minecraft 26.1 è destinata a sostituirla e dispone ormai di build NeoForge stabili. Per un progetto nuovo sarebbe una candidata forte. Per il recupero di Electrical Age, però, la 1.21.1 riduce il rischio iniziale perché:

- ha un ecosistema più maturo;
- dispone di più esempi e integrazioni già collaudate;
- evita le ulteriori trasformazioni del rendering introdotte dopo Minecraft 1.21.6;
- consente di fissare una piattaforma durante un porting lungo e complesso.

La versione di NeoForge dovrà essere almeno `21.1.229`, che include una correzione di sicurezza relativa ai pacchetti di rete. Prima di creare il nuovo progetto andrà comunque selezionata l'ultima release stabile disponibile della linea `21.1.x`.

## Parti recuperabili

Il nucleo più prezioso da conservare è la simulazione elettrica e termica:

- `mods.eln.sim`;
- `mods.eln.sim.mna`;
- `mods.eln.solver`;
- modelli di carichi, componenti, processi e connessioni.

Queste aree non sono ancora indipendenti da Minecraft. Il simulatore si registra direttamente sul vecchio event bus FML e alcune classi usano NBT, inventari, entità o operazioni sul mondo. La matematica e buona parte del modello elettrico possono però essere estratte dietro interfacce neutrali.

## Parti da riscrivere

Richiederanno una riscrittura sostanziale:

- registrazione di blocchi, item, menu e block entity;
- sistema dei descriptor basato sul metadata o damage degli item;
- rendering immediato con OpenGL, `GL11` e `Tessellator`;
- caricamento e uso dei vecchi modelli OBJ;
- GUI e container;
- pacchetti client-server;
- persistenza basata su `WorldSavedData` e vecchie API NBT;
- gestione del grafo rispetto a caricamento e scaricamento dei chunk;
- generazione dei minerali;
- ricette definite direttamente in Java;
- ore dictionary, da sostituire con tag;
- integrazioni Waila, ComputerCraft, OpenComputers, IndustrialCraft e CoFH.

Il `NodeManager` merita particolare attenzione: conserva globalmente i nodi, eredita da `WorldSavedData` e usa nomi di classe e riflessione durante il caricamento. Nel port moderno serviranno identificatori stabili, dati versionati e una politica esplicita per nodi appartenenti a chunk non caricati.

## Architettura proposta

Il nuovo progetto dovrebbe essere separato almeno in tre moduli logici:

```text
electrical-age/
|-- simulation/    Solver, MNA, termica e processi in Java puro
|-- common/        Modello logico dei dispositivi e serializzazione astratta
`-- neoforge/      Blocchi, block entity, rete, rendering, GUI e capability
```

Il nuovo codice dovrebbe essere scritto in Java 21. I 37 file Kotlin originali sono concentrati soprattutto in integrazioni e networking; riscriverli in Java evita di aggiungere un language loader durante la fase iniziale.

La separazione deve impedire al modulo `simulation` di importare tipi Minecraft, NeoForge, NBT o classi di rendering. Tick, persistenza ed effetti sul mondo dovranno essere forniti dal livello di integrazione tramite interfacce.

## Prima fetta verticale

Prima di migrare tutto il catalogo di Electrical Age bisogna produrre una fetta giocabile composta da:

1. un cavo;
2. una sorgente;
3. un resistore o una lampada;
4. uno strumento per misurare la tensione;
5. collegamento e scollegamento dei nodi;
6. salvataggio e ricaricamento del circuito;
7. sincronizzazione server-client;
8. rendering delle connessioni;
9. prova in single player e su server dedicato.

Questa fetta verifica subito i rischi principali: integrazione fra mondo e simulatore, grafo elettrico, ciclo di tick, persistenza, networking e rendering. Il catalogo completo dei dispositivi va migrato solo dopo che questa base è stabile.

## Rischi principali

1. **Rendering:** il progetto contiene una grande quantità di OpenGL immediato e modelli OBJ legacy. È probabilmente l'area con il costo maggiore.
2. **Identità dei contenuti:** molti dispositivi sono varianti conservate nel metadata di pochi item o blocchi. Andranno assegnati identificatori moderni e stabili.
3. **Persistenza:** bisogna progettare dati moderni e versionati prima di creare mondi di prova destinati a durare. Non è necessario interpretare o convertire il formato usato dalla versione 1.7.10.
4. **Chunk e simulazione:** il comportamento dei circuiti che attraversano chunk caricati e non caricati deve essere deciso e verificato esplicitamente.
5. **Prestazioni:** la simulazione MNA deve essere profilata su server, evitando di bloccare il tick principale con reti grandi.
6. **Client e server:** le classi di rendering e GUI devono restare completamente fuori dal caricamento del server dedicato.
7. **Licenze:** il codice è LGPL 3.0; grafica e modelli sono CC BY-NC-SA 3.0. La licenza non commerciale degli asset deve essere considerata prima della distribuzione e di qualsiasi monetizzazione.

## Conclusione operativa

La base da fissare per il port è **NeoForge 1.21.1 su Java 21**. Il lavoro deve iniziare con un progetto nuovo e una piccola fetta verticale; il repository originale va mantenuto come riferimento e fonte da cui trasferire selettivamente simulazione, comportamento e risorse.

Non conviene copiare subito l'intero albero sorgente nel nuovo ambiente. La prima attività di implementazione dovrebbe essere l'estrazione del solver e del modello MNA in un modulo Java puro, accompagnata da test numerici su piccoli circuiti noti.

## Stato del porting minimale

L'8 settembre 2026 è stata creata una prima base in `modern/` usando il template ufficiale ModDevGradle per NeoForge 1.21.1.

La base comprende:

- mod ID `eln` e versione iniziale `0.1.0-alpha.1`;
- Java 21, NeoForge 21.1.249 e Parchment mappings per Minecraft 1.21.1;
- registrazione dell'item componente `eln:resistor` e del contenitore tecnico `eln:six_node`;
- creative tab, traduzioni inglese e italiana, modello minimale, loot table, tag dello strumento e ricetta;
- un primo solver DC MNA indipendente da Minecraft;
- componenti iniziali per nodi, resistori e sorgenti di tensione;
- test numerici per un partitore da 12 V e per il rilevamento di un circuito flottante.

La build Gradle completa e i due test sono passati. È stato inoltre avviato il client di sviluppo: Electrical Age è stato riconosciuto e inizializzato senza errori nel log. Il successivo prototipo ha introdotto una block entity `SixNode` con sei posizioni indipendenti, installazione del resistore sulle facce, modello orientato, persistenza e rimozione del singolo elemento. Il componente non è ancora collegato al solver; rete elettrica, sorgente e cavi saranno la fetta successiva.

## Riferimenti

- [Capability NeoForge 1.21.1](https://docs.neoforged.net/docs/1.21.1/inventories/capabilities/)
- [Networking NeoForge 1.21.1](https://docs.neoforged.net/docs/1.21.1/networking/)
- [Adozione e supporto di NeoForge 1.21.1](https://neoforged.net/news/2024-retrospection/)
- [Correzione di sicurezza per NeoForge 1.21.1](https://neoforged.net/news/mitigating-vulnerabilities-network/)
- [Cambiamenti di NeoForge per Minecraft 26.1](https://neoforged.net/news/26.1release/)
