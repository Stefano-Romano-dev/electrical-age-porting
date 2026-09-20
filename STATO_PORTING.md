# Stato del porting di Electrical Age

Aggiornato: 20 settembre 2026

## Stato generale

**Fase corrente: milestone M2 completata; M3 avviata con il primo menu e payload di configurazione server-authoritative.**

Target confermato:

- sorgente: Electrical Age 1.24.8 / Forge 1.7.10;
- destinazione: Minecraft 1.21.1 / NeoForge / Java 21;
- sorgente legacy in `original/`;
- nuovo progetto in `modern/`.

`modern/` contiene una baseline NeoForge funzionante derivata dall'MDK ufficiale. Nessun file sotto `original/` è stato modificato durante la creazione della baseline.

Toolchain fissata:

- Minecraft 1.21.1;
- NeoForge 21.1.250;
- ModDevGradle 2.0.146;
- Parchment 2024.11.17;
- Gradle 9.2.1;
- Java 21;
- Kotlin 2.4.20, senza KotlinForForge;
- mod id `eln`, versione iniziale `0.1.0-alpha.1`.

## Avanzamento

- [x] Individuati e letti i file di build e le istruzioni di `original/Agents.md`.
- [x] Inventariati sorgenti, test, dipendenze e risorse.
- [x] Identificati entry point, lifecycle, registrazioni e sistemi di networking legacy.
- [x] Analizzati solver, tick di simulazione, node manager e persistenza.
- [x] Identificato il catalogo descriptor-driven basato su ItemStack damage/metadata.
- [x] Valutati GUI, rendering OBJ/LWJGL e problemi dei resource path.
- [x] Confrontata l'architettura con le API ufficiali NeoForge 1.21.1.
- [x] Definita la strategia a milestone e il primo vertical slice.
- [x] Creata la documentazione operativa per roadmap, decisioni, inventario, verifiche, problemi, risorse e diario.
- [x] Creato `AGENTS.md` con le regole persistenti del workspace.
- [x] Creato il workspace NeoForge 1.21.1 in `modern/`.
- [x] Ottenuta una clean build riproducibile e un test JUnit 5 minimale.
- [x] Avviati client e dedicated server con caricamento del mod `eln`.
- [x] Estrarre e portare il core di simulazione M1 con test di parità, scheduler, processi fisici essenziali e codec persistenti esterni.
- [x] Implementare il primo SixNode verticale con runtime server, persistenza, rendering, item dinamici e prova multiplayer dedicated→client.
- [ ] Completare M3 networking, menu e strumenti (configurazione e widget verificati end-to-end; multimetro implementato e verificato in GameTest, prova multiplayer dell'interazione finale ancora aperta).

## Decisioni registrate

1. Il port riparte da zero in `modern/`; il precedente contenuto moderno non viene recuperato automaticamente.
2. `original/` resta una sorgente di riferimento e non viene adattata in place.
3. Il core solver/MNA viene preservato prima di portare il catalogo di contenuti.
4. I numeric descriptor id legacy non diventano registry id moderni: si useranno id namespaced stabili e una mappa di migrazione separata.
5. Il SixNode rimane un blocco host multi-faccia; non verrà trasformato banalmente in un blocco separato per ogni componente.
6. Networking, GUI, registrazione, rendering e persistenza saranno riscritti sulle API NeoForge 1.21.1.
7. Le integrazioni esterne sono rimandate finché il nucleo standalone non è giocabile.
8. La compatibilità diretta dei mondi 1.7.10 non è parte del primo milestone.
9. Il dedicated server è un criterio di verifica obbligatorio, non un test finale opzionale.
10. Il port deve essere fedele anche esteticamente alla 1.24.8; placeholder e semplificazioni non costituiscono asset finali.
11. La parità percepita con la 1.24.8 è la definizione di completamento; ogni differenza intenzionale richiede approvazione esplicita e tracciamento.

## Rilevazioni importanti

- Il progetto principale contiene 835 sorgenti e circa 88.000 righe.
- 607 file dipendono direttamente da API Minecraft/Forge o integrazioni legacy.
- 72 dei 90 test sono privi di import diretti Minecraft/Forge e costituiscono la base migliore per validare il core.
- Sono presenti almeno 409 registrazioni descriptor/element statiche rilevate.
- 144 file usano LWJGL direttamente e circa 173 sono legati al rendering legacy.
- 708 resource path contengono maiuscole o spazi e richiedono normalizzazione tracciata.
- `NodeManager` è globale e `NodeManagerNbt.writeToNBT` non scrive lo stato: l'ownership e la persistenza devono essere ridisegnate.
- CoFH Core è effettivamente necessario nella 1.24.8: la relazione Modrinth è `required` e `Other.modIdTe = "Eln"` rende sempre attiva l'integrazione RF che implementa `IEnergyHandler`. Il JAR moderno verificato non lo dichiara.
- La macchina dispone già di Java 21 a 64 bit (Temurin 21.0.11).
- `original/` non ha un `.git` proprio; il repository Git effettivo è la radice del workspace e contiene modifiche/cancellazioni preesistenti che non devono essere alterate.

## Milestone M1 completata: core di simulazione

Deliverable previsto per M1:

- struttura `mods.eln.sim` senza import Minecraft/NeoForge;
- port incrementale del solver MNA e delle quantità fisiche necessarie;
- test originali rilevanti adattati e fixture numeriche di confronto;
- lifecycle del simulatore controllabile nei test;
- scelta e documentazione del supporto Kotlin senza imporre KotlinForForge come dipendenza del mod.

Avanzamento M1 verificato:

- Kotlin JVM 2.4.20 configurato su Java 21;
- Kotlin stdlib 2.4.20 e Commons Math 3.6.1 incorporati nel JAR con Jar-in-Jar;
- `mods.eln.sim.mna` non importa Minecraft o NeoForge, verificato anche da test automatico;
- portati `SubSystem`, `State`, `VoltageState`, `CurrentState`, `Component`, `Bipole`, `Resistor`, `CurrentSource` e `VoltageSource`;
- superati i due circuiti esempio della 1.24.8, un partitore, il caso singolare e l'invalidazione dinamica della matrice/RHS;
- portati ownership esplicita di `RootSystem`, generazione deterministica dei sottosistemi con confini privati, break/rebuild, oversampling pre-step, flush globale e distruttori;
- aggiunti nove test del lifecycle di root; l'audit successivo ha riallineato la riscoperta legacy dei componenti rimossi ma non scollegati;
- portate la compressione delle catene resistive in `Line`, la soglia legacy di 100 stati, `InterSystemAbstraction` e l'equivalente di Thévenin;
- verificata la convergenza dell'esempio legacy a due reti prima e dopo l'aggiunta di un carico;
- portati `Capacitor`, `Inductor` e `Delay` con semantica temporale backward Euler e stato interno legacy;
- verificate traiettorie RC/RL campione per campione e la progressione interna di `Delay`;
- portati `ResistorSwitch`, `Transformer`, `PowerSource`, `PowerSourceBipole` e `TransformerInterSystemProcess`;
- verificati rapporti del trasformatore, limiti tensione/corrente, potenza effettiva, fallback NaN e lifecycle dei processi root;
- completata la corrispondenza dei 30 tipi MNA legacy con `Monopole` e `SubSystemDebugSnapshot`;
- riallineate semantiche di teardown e connettività emerse dall'audit; gli adapter persistenti e i chiamanti reali restano da verificare;
- portati `ElectricalLoad`, `ElectricalConnection`, carichi/connessioni/resistori termici e i tre processi base di conversione Joule→calore;
- estratto il passo termico nell'esecutore puro `ThermalSimulator`, conservando ordine delle fasi, formule e accumulatori legacy e isolando lo scambio termico con la stanza dietro un adapter;
- aggiunte 19 fixture fisiche; la suite completa raggiunge 68 test superati;
- portato lo scheduler `Simulator` senza event bus, con periodi elettrico/termico, fasi slow, coda distruzioni e classificazione fast/slow;
- portati gli inizializzatori termici con validazione di stabilità posseduta dallo scheduler, `FurnaceProcess`, `DiodeProcess`, conversione resistiva legacy, `Integrator` e `Differentiator`;
- fissate con fixture le sequenze multi-rate e le particolarità di startup/reset; la suite completa raggiunge 86 test superati;
- portate `FunctionTable`/`FunctionTableYProtect`, batteria, invecchiamento configurabile, regolatore on-off/analogico e adapter puri forno/resistore;
- introdotti snapshot puri per batteria e regolatore con schema legacy conservato, rimandando la codifica NBT al layer di persistenza moderno;
- aggiunte 22 fixture per curve, carica, energia, aging, distruzione e regolazione; la suite completa raggiunge 108 test superati;
- portati watchdog generico, elettrico, resistivo e termico, distruzione ritardata e `TimeRemover`, con policy, casualità e diagnostica iniettate;
- auditati 42 tipi tra `mods.eln.sim` e `sim.process`: 32 hanno una controparte pura, 10 sono instradati alle vertical slice che ne possiedono le dipendenze;
- aggiunte 18 fixture per soglie, timeout, isteresi di guasto, categorie, diagnostica e timer; la suite completa raggiunge 126 test superati;
- dedicated server avviato con caricamento effettivo del runtime Kotlin 2.4.20.
- aggiunti codec `CompoundTag` esterni al core per batteria, regolatore, stati elettrici/termici, forno e componenti MNA persistenti, conservando chiavi e precisione della 1.24.8;
- abilitato il runner unit test ModDevGradle per verificare i codec contro il vero `CompoundTag`; suite completa a 134 test superati;
- collegato `Simulator.tick()` a `ServerTickEvent.Pre` tramite un owner per identità di `MinecraftServer`, creato all'avvio e rimosso allo stop senza singleton del server corrente;
- dedicated server verificato fino a `Done` con creazione dell'owner; start, tick e cleanup dello stesso handler sono inoltre coperti con le classi evento NeoForge reali.

## Milestone M2 completata: primo SixNode verticale

- catalogo canonico iniziale con id moderni stabili e mapping esatto degli id legacy: sorgente `192`, cavo bassa tensione `2052`, resistore di potenza `6180`;
- shell SixNode con sei slot indipendenti e corrispondenza esplicita degli indici legacy `WEST, EAST, DOWN, UP, NORTH, SOUTH`;
- rotazioni LRDU conservate nei codici legacy `0..3`, incluso il fallback storico a `LEFT` per valori non validi;
- codec `CompoundTag` moderno versionato per identità e orientamento delle facce, con preservazione dei tipi namespaced validi non ancora conosciuti dal catalogo;
- suite completa salita a 147 test e build completa superata;
- registrati blocco host e block entity con id `eln:six_node`, senza `BlockItem` né modello provvisorio;
- il block entity possiede lo shell, marca le mutazioni persistenti e compone il codec versionato già verificato;
- round-trip e rifiuto di schemi futuri verificati sul tipo realmente registrato; suite completa salita a 149 test;
- build completa e caricamento dedicated server fino a `Done` superati;
- aggiunto un GameTest server-side che piazza l'host, monta una faccia, serializza con metadata completi, rimuove e ricostruisce il block entity tramite il dispatcher vanilla; 1/1 test superato;
- registrati l'item contenitore `eln:six_node_component` e il data component persistente/sincronizzato `eln:six_node_component_type`, equivalente moderno del damage value legacy;
- portato il montaggio server-authoritative del cavo bassa tensione: offset del bersaglio sostituibile, faccia inversa, supporto non-air/opaco, rotazione LRDU base e consumo solo dopo il successo;
- facce occupate, supporti trasparenti e definizioni non ancora portate vengono rifiutati senza creare host né consumare item;
- la suite completa raggiunge 154 test, 4/4 GameTest passano e la build completa è superata;
- aggiunto un harness dedicated riproducibile a due processi: il primo salva il cavo nel file regione, il secondo riapre lo stesso mondo e verifica blocco, BE, faccia, tipo e rotazione;
- verificato inoltre unload e reload effettivo dello stesso chunk nel secondo processo; il task è ripetibile su un mondo di prova già esistente;
- portate le sagome di selezione canoniche per faccia e la selezione legacy del componente da rompere, compresa la particolarità del ray test diretto che può scegliere la faccia di uscita;
- portati rimozione e drop server-side: una faccia rimossa lascia l'host finché contiene altri componenti, la perdita del supporto rimuove soltanto le facce coinvolte e la sostituzione esterna dell'host rilascia tutte le facce;
- survival restituisce l'item tipizzato, creative non produce drop; gli id namespaced validi sconosciuti restano rappresentabili anche nel drop;
- gli aggiornamenti del block entity vengono sincronizzati ai client con update tag/packet per rendere disponibile lo stato delle sagome dinamiche;
- la suite completa raggiunge 156 test, 8/8 GameTest passano, il controllo dedicated a due processi resta verde e la build completa è superata;
- nessun asset copiato e nessun placeholder estetico introdotto: a quel punto grafo, lifecycle elettrico load/unload, verifica multiplayer e rendering restavano aperti;
- introdotto `SixNodeElectricalGraph`, posseduto per `ServerLevel` dal contesto della simulazione server, con un `ElectricalLoad` per faccia di cavo caricata;
- derivata e fissata la resistenza legacy del cavo LV: `0,0125 Ω` per carico e `0,025 Ω` per tratta fra due cavi;
- ricostruite le tre topologie legacy iniziali: adiacenza complanare, collegamento interno fra facce ortogonali e passaggio diagonale attorno a uno spigolo;
- montaggio, rimozione, sostituzione, chunk unload e reload aggiornano ora il runtime MNA, rimuovendo sempre le connessioni prima dei carichi;
- la ricostruzione da file regione usa una coda dei chunk caricati elaborata al successivo `Post` tick, mentre una riconciliazione finale elimina gli host di chunk non più presenti;
- suite completa salita a 162 test, 9/9 GameTest superati, harness dedicated a due processi verde anche per teardown/ricostruzione del grafo e build completa superata;
- il grafo è ora consapevole dei terminali orientati: cavo e sorgente espongono i quattro lati, il resistore soltanto i lati legacy `front.right()`/`front.left()`;
- portati i runtime MNA della sorgente elettrica e del resistore di potenza, inclusi `0,0125 Ω` seriali della sorgente e il valore legacy vuoto `0,01 Ω` del resistore;
- sorgente e resistore sono piazzabili tramite l'item tipizzato; il resistore applica la rotazione aggiuntiva `left()` del descriptor 1.24.8;
- aggiunti parametri numerici finiti opzionali al payload persistente della faccia; la tensione usa la chiave legacy `voltage` e il formato 1 resta retrocompatibile perché il campo è opzionale;
- il GameTest chiude un circuito fra sorgenti da `50 V` e `0 V` attraverso il resistore e verifica `1428,5713469 A`, includendo le due impedenze terminali `1e-9 Ω`;
- il lifecycle traccia l'identità dei block entity caricati per impedire a un `setRemoved` stantio di eliminare il runtime appena ricostruito dopo un reload chunk;
- suite completa salita a 165 test, 10/10 GameTest superati, harness dedicated a due processi e build completa nuovamente verdi;
- aggiunto un renderer client-only del SixNode con le trasformazioni legacy per le sei facce e le quattro rotazioni LRDU;
- integrati gli OBJ/MTL e le texture originali di sorgente e resistore, filtrando i gruppi legacy pertinenti, senza introdurre geometrie sostitutive;
- integrato il cavo procedurale con le misure canoniche `1,95/16` × `0,95/16`, usando lo stesso contratto dei terminali del grafo per i bracci complanari, interni e diagonali;
- un primo avvio client ha rilevato il riferimento case-sensitive `PowerElectricPrimitives.mtl`; dopo la normalizzazione lowercase, il secondo caricamento risorse non ha prodotto errori OBJ/MTL o blockstate ELN;
- un primo screenshot in-world ha evidenziato tre scostamenti dal renderer legacy: tinta del cavo assente, nodo bianco disegnato anche sui tratti rettilinei e raccordi della sorgente mancanti; tutti sono stati corretti dalle regole originali (`20%` di tinta, cap solo su estremità/curve/diramazioni e spezzoni automatici verso i terminali connessi);
- aggiunti modelli item dinamici con le tre texture canoniche, nomi localizzati e tre stack distinti nella scheda Redstone/ricerca creativa;
- suite completa salita a 169 test e build/test superati; i 10/10 GameTest dedicati restano verdi e il dedicated server non carica classi client;
- un nuovo caricamento client non produce più il warning del modello item; uno screenshot in-world post-correzione conferma continuità, tinta e cap attesi per la configurazione provata. Configurazione GUI/inventario del resistore e feedback audiovisivo restano fuori dal perimetro M2.
- corretto inoltre il vicino diagonale sullo spigolo esterno: grafo e renderer cercano ora `edge.opposite` come la 1.24.8 e la geometria applica la scelta legacy `Extend/Internal` a un solo braccio; test mirati, suite da 169 test, build completa e 10/10 GameTest sono verdi, e lo screenshot finale conferma la curva continua senza gradino visibile.
- aggiunti README pubblici in inglese e italiano con obiettivo, stato verificato, limiti correnti e accesso alla documentazione tecnica; aggiornato anche il README del sottoprogetto moderno.
- aggiunto un GameTest di presentazione che monta cavo, sorgente e resistore su tutte le sei facce; la batteria dedicated sale a 11/11 GameTest.
- aggiunto un probe di sviluppo dedicated→client: il server genera 18 host, il client verifica faccia, tipo, rotazione e parametri ricevuti, acquisisce uno screenshot diurno e termina solo dopo 60 tick sincronizzati.
- la prova multiplayer reale è superata con i marker `SIX_NODE_MULTIPLAYER_SERVER_PLAYER_JOINED` e `SIX_NODE_MULTIPLAYER_CLIENT_SYNC_OK`; build, 169 test unitari, 11/11 GameTest e persistenza a due processi sono verdi.

## Milestone M3 avviata: networking, menu e strumenti

- registrato il primo `MenuType` slotless per la sorgente elettrica e una screen client localizzata con il singolo campo `Output voltage` della 1.24.8;
- introdotto il payload tipizzato server-bound `eln:set_electrical_source_voltage`, conservando il valore float trasmesso dal client e la chiave persistente double `voltage`;
- il server accetta l'aggiornamento soltanto dal menu corretto, per posizione/faccia corrispondenti, entro otto blocchi, sulla sorgente prevista e con valore finito;
- la modifica ricostruisce immediatamente il runtime elettrico, marca il block entity persistente e usa il normale update packet per restituire lo stato ai client;
- probe dedicated→client Superflat verificato con apertura menu, invio di `123,5 V`, ritorno dello stato configurato e successiva coerenza delle 18 facce;
- estratto `ElnNumericEditBox`, riutilizzabile per i menu successivi: parsing locale, precisione visiva legacy, commit soltanto su Invio/perdita focus e ripristino dell'ultimo valore per input non valido;
- il probe configura ora la sorgente passando dal widget e dalla perdita di focus, incluso il locale host `it_IT`, anziché inviare direttamente il payload;
- suite salita a 174 test unitari; build completa, 11/11 GameTest e probe multiplayer 1920×1080 su Superflat restano verdi.
- aggiunto `eln:multimeter` con id legacy `896`, texture originale 16×16 e lettura server-side di cavo, sorgente e resistore attraverso il grafo; i valori sono mostrati nella chat con la formattazione ingegneristica legacy;
- il blocco SixNode dà precedenza al multimetro rispetto all'apertura del menu, riproducendo l'ordine dell'interazione legacy;
- suite salita a 177 test unitari, 11/11 GameTest e build completa superati; un primo probe client ha sincronizzato l'item ma non ha attivato la misura a causa della precedenza del blocco, corretta nel codice corrente. La prova multiplayer dopo tale correzione non è ancora registrata come superata.

Il dettaglio tecnico e la roadmap completa sono in [ANALISI_PORTING.md](./ANALISI_PORTING.md).

L'indice di tutti i documenti operativi è in [docs/porting/README.md](./docs/porting/README.md).
