# Analisi del porting di Electrical Age

Aggiornata: 11 settembre 2026

## Obiettivo

Ripartire dal codice di Electrical Age 1.24.8 per Minecraft Forge 1.7.10 e realizzare un nuovo port per:

- Minecraft 1.21.1;
- NeoForge;
- Java 21;
- mod id moderno `eln`.

Il port deve mantenere sia la fedeltà funzionale sia quella estetica. Le API e l'architettura interna saranno modernizzate, mentre geometrie, proporzioni, texture, palette, animazioni, interfacce, particelle e suoni dovranno restare riconoscibili e confrontabili con la 1.24.8.

La sorgente di riferimento è `original/`. La nuova implementazione vivrà in `modern/`. In questa prima fase non è stato copiato o modificato codice del mod.

La release indicata come base è [Electrical Age 1.24.8](https://github.com/age-series/ElectricalAge/releases/tag/1.24.8), associata da GitHub al commit abbreviato `46ab422`. La cartella locale `original/` non contiene però un repository Git indipendente: Git risale alla radice del workspace. Non è quindi possibile certificare localmente il tag con `git describe`; per il port si assume, come indicato, che il contenuto sia quello clonato dalla release 1.24.8.

## Dimensioni del progetto di partenza

| Area | Quantità |
|---|---:|
| Sorgenti Java principali | 478 file / 38.793 righe |
| Sorgenti Kotlin principali | 357 file / 49.578 righe |
| Test | 90 file / 4.314 righe |
| Test senza import diretti Minecraft/Forge | 72 |
| Risorse | 1.442 file / 107,88 MiB |
| File con import Minecraft, Forge o integrazioni legacy | 607 |
| File con import `net.minecraft.*` | 581 |
| File con import FML `cpw.mods.fml.*` | 81 |
| File con import Neo/Forge legacy | 84 |
| File con uso LWJGL diretto | 144 |
| File con lettura/scrittura NBT | 179 |
| File coinvolti nel rendering legacy | circa 173 |

Le aree più grandi sono `sixnode` (251 file), `transparentnode` (129), `sim` (85), `node` (51), `item` (46), `misc` (45), `gui` (27), `mechanical` (17) e `simplenode` (16).

La dimensione rende impraticabile un port “sostituisci gli import finché compila”. Serve una migrazione per strati e per vertical slice giocabili.

## Architettura attuale

### 1. Simulazione elettrica e termica

Il valore tecnico principale è sotto `mods.eln.sim` e `mods.eln.solver`:

- solver MNA (`RootSystem`, sottosistemi, stati e componenti);
- carichi, resistori, condensatori, induttori, sorgenti e trasformatori;
- processi elettrici e termici;
- watchdog e distruzione;
- parser delle equazioni e macchine a stati.

Circa 76 file nelle aree `sim`/`solver` non importano direttamente Minecraft o Forge. È il primo nucleo riutilizzabile. Il solver usa Apache Commons Math 3.6.1, oggi incluso e rilocato nel JAR.

Il solver legacy applica inoltre due astrazioni prestazionali che fanno parte del comportamento da preservare: comprime le catene resistive in componenti `Line` e spezza le reti pubbliche oltre 100 stati in sottosistemi accoppiati tramite equivalenti di Thévenin. Queste astrazioni devono poter essere distrutte e ricostruite quando cambia la topologia, ripristinando componenti e stati originali.

`Simulator` è agganciato al tick server e svolge più sottopassi elettrici/termici sul thread server. Questa semantica va mantenuta inizialmente: parallelizzare il solver durante il port introdurrebbe rischi di concorrenza non necessari. Prima va separato l’orologio di simulazione dall’evento NeoForge e coperto con test deterministici.

### 2. Sistema dei nodi

Il gameplay si basa su tre famiglie:

- `SixNode`: più componenti montabili sulle sei facce dello stesso blocco;
- `TransparentNode`: macchina completa rappresentata da un blocco;
- `SimpleNode`: blocchi più convenzionali e integrazioni;
- a queste si aggiungono ghost block e grid node per multiblocchi/reti.

`NodeBase` combina oggi troppe responsabilità: coordinate del mondo, topologia, connessioni elettriche/termiche, piazzamento e rottura, interazione col giocatore, GUI, suoni, networking e persistenza. Questo accoppiamento è il principale ostacolo del port.

`NodeManager` è un singleton globale basato su `WorldSavedData`, indicizza i nodi per coordinate comprensive di dimensione e ricostruisce le classi via stringa UUID e reflection. `NodeManagerNbt.writeToNBT` non salva attualmente il manager (la chiamata è commentata). In 1.21.1 la proprietà deve diventare esplicitamente server/level-scoped, usando `SavedData` quando serve persistenza globale e i `BlockEntity` per lo stato locale. Non va ricreato un singleton globale dipendente dal server corrente.

### 3. Catalogo basato su metadata/ItemStack damage

Il mod registra pochi blocchi/item contenitore e vi multiplexa centinaia di descrittori tramite il damage value dell'`ItemStack`:

- 218 chiamate statiche a `addDescriptor`;
- 191 chiamate a `addElement`;
- almeno 126 descrittori SixNode e 85 TransparentNode dichiarati direttamente nei due grandi file di registrazione;
- circa 4.700 righe nei soli file `ItemRegistration`, `SixNodeRegistration` e `TransparentNodeRegistration`.

Questo schema non è portabile direttamente. In Minecraft moderno il damage non è un identificatore generale di sottotipo e lo stato arbitrario dell'item usa i data component. La soluzione proposta è:

1. conservare un unico blocco host per i SixNode, perché il montaggio multiplo per faccia è parte essenziale del design;
2. assegnare a ogni tipo di componente un id stabile `ResourceLocation` in un registro ELN dedicato;
3. salvare l'id del componente e la configurazione nei data component dell'item e nel `BlockEntity` host;
4. registrare come blocchi distinti solo le macchine per cui identità, loot, blockstate o compatibilità lo richiedono;
5. mantenere una tabella esplicita `legacy numeric id -> modern resource id` per un futuro importer, senza promettere compatibilità diretta dei mondi 1.7.10.

Questa scelta evita sia centinaia di classi duplicate sia il ritorno nascosto ai metadata legacy.

### 4. Lifecycle, registri ed eventi

L'entry point `Eln.java` è un service locator globale molto grande. Usa `@Mod`, `@SidedProxy`, tre fasi FML, `GameRegistry`, due event bus legacy e registrazioni imperative.

In NeoForge 1.21.1 dovrà essere sostituito da:

- costruttore `@Mod("eln")` con mod event bus;
- `DeferredRegister`/`DeferredHolder` per blocchi, item, block entity type, menu, sound event, entity type, data component e altri oggetti registrati;
- servizi server-scoped creati e distrutti sugli eventi di lifecycle;
- registrazione client isolata in classi client-only;
- configurazione NeoForge suddivisa in common/server/client, senza campi globali mutabili nell'entry point.

NeoForge raccomanda `DeferredRegister` per evitare errori di ordine durante la registrazione: [documentazione dei registri 1.21.1](https://docs.neoforged.net/docs/1.21.1/concepts/registries/).

### 5. Block entity e persistenza

Le vecchie `TileEntity` diventano `BlockEntity` registrate tramite `BlockEntityType`. Tick, caricamento/scaricamento chunk, update tag e pacchetti di sincronizzazione hanno firme e lifecycle differenti.

Il port deve separare:

- stato persistente del componente;
- stato derivato della rete elettrica;
- snapshot minimo destinato al client;
- configurazione dell'item prima del piazzamento.

I riferimenti a coordinate devono diventare `BlockPos` + `ResourceKey<Level>` e non integer dimension id. Le letture/scritture NBT vanno aggiornate alle API con registry lookup. La guida di riferimento è [Block Entities in NeoForge 1.21.1](https://docs.neoforged.net/docs/1.21.1/blockentities/).

### 6. Networking

Esistono due sistemi legacy sovrapposti:

- sette coppie packet/handler su `SimpleNetworkWrapper`, soprattutto per achievement e Waila;
- un canale event-driven con byte discriminator e serializzazione manuale `DataInputStream`/`DataOutputStream` per nodi, GUI, suoni e informazioni client/server.

Vanno sostituiti con payload tipizzati (`CustomPacketPayload`), id namespaced e `StreamCodec`, registrati tramite `RegisterPayloadHandlersEvent`. Ogni payload deve dichiarare direzione, validazione, limite dimensionale e gestione sul thread corretto. Riferimento: [payload NeoForge 1.21.1](https://docs.neoforged.net/docs/1.21.1/networking/payload/).

Non conviene tradurre alla cieca i vecchi discriminator numerici: prima si definiscono i messaggi di dominio realmente necessari, poi si elimina la duplicazione con la sincronizzazione standard dei block entity/menu.

### 7. GUI

Le GUI legacy includono 27 classi infrastrutturali più molti container e schermate specifiche. Sono basate su `GuiScreen`, `GuiContainer`, `IInventory` e apertura tramite `IGuiHandler`.

La destinazione usa `MenuType`, `AbstractContainerMenu`, `Screen`/`AbstractContainerScreen` e apertura server-side tramite `MenuProvider`. Le view non devono possedere lo stato della macchina. I widget ELN riutilizzabili potranno essere riscritti sopra le primitive moderne dopo il primo menu funzionante. Riferimento: [menu NeoForge 1.21.1](https://docs.neoforged.net/docs/1.21.1/gui/menus/).

### 8. Rendering e modelli

Questa è l'area a più alto costo:

- 144 file usano direttamente LWJGL;
- il codice usa `GL11`, `Tessellator`, `IIcon`, `IItemRenderer` e `TileEntitySpecialRenderer`;
- esiste un loader OBJ personalizzato e un catalogo molto ampio di renderer descriptor-driven;
- 157 file OBJ e 157 MTL sono affiancati da sorgenti `.blend` e `.xcf`.

La migrazione richiede `PoseStack`, `VertexConsumer`, render type moderni, baked model/geometry loader oppure `BlockEntityRenderer` solo per le parti realmente dinamiche. È preferibile convertire le geometrie statiche in modelli baked e riservare il BER a indicatori, rotazioni, cavi e parti animate. Va prima creato un proof of concept con un SixNode e un modello OBJ reale.

### 9. Risorse e data-driven content

Inventario principale:

- 735 PNG;
- 157 OBJ e 157 MTL;
- 158 file Blender, 85 XCF e altri sorgenti grafici;
- 44 OGG e 2 WAV;
- 19 file `.lang`;
- solo 2 JSON moderni nell'intero albero risorse.

Sono stati rilevati 708 percorsi con maiuscole o spazi. I resource location moderni devono essere normalizzati in lowercase e i rinomini devono essere tracciati in un manifest, perché su filesystem case-insensitive gli errori possono restare nascosti fino al build o al server Linux.

Le azioni necessarie sono:

- convertire `assets/eln/lang/<locale>.lang` in JSON lowercase (`en_us.json`, `it_it.json`, ecc.);
- creare `sounds.json` e registrare i sound event;
- spostare ricette imperative in `data/eln/recipe/*.json` o datagen;
- sostituire OreDictionary con tag, usando in genere il namespace comune `c`;
- generare blockstate, modelli item/block, loot table e tag;
- escludere dal JAR di runtime i sorgenti `.blend`, `.xcf`, `.xlsx`, `.aseprite`, `.ai`, `.vsd` e `model-to-be-integrated`, conservandoli però nel repository;
- verificare licenze e attribuzioni durante ogni conversione.

Le ricette 1.21.1 sono principalmente data-driven ([documentazione ricette](https://docs.neoforged.net/docs/1.21.1/resources/server/recipes/)); i tag sostituiscono il ruolo dell'OreDictionary ([documentazione tag](https://docs.neoforged.net/docs/1.21.1/resources/server/tags/)).

### 10. Integrazioni esterne

Le dipendenze legacy sono CoFHCore/CoFHLib, Waila, IC2 Classic, OpenComputers, ComputerCraft e jSerialComm. MQTT/Modbus sono inoltre presenti nel codice.

Nella release 1.24.8 CoFH Core risulta di fatto obbligatorio, nonostante l'annotazione principale usi soltanto gli ordinamenti opzionali `after:CoFHCore`, `after:CoFHAPI` e `after:CoFHAPI|energy`. `gradle.properties` pubblica infatti `cofh-core` come relazione Modrinth richiesta e `EnergyConverterElnToOtherEntity` implementa direttamente `cofh.api.energy.IEnergyHandler`. Inoltre `Other.modIdTe` vale erroneamente `"Eln"`: ELN rileva quindi sempre l'integrazione RF come caricata e le annotazioni `@Optional` non possono rimuoverla quando CoFH manca. Questo spiega il requisito osservato avviando l'originale. Tale dipendenza accidentale non deve essere riprodotta nel port moderno.

Strategia:

- nessuna integrazione esterna nel primo milestone;
- definire API interne/capability ELN senza dipendere da una singola energy API esterna;
- valutare successivamente Jade al posto di Waila e CC:Tweaked al posto di ComputerCraft;
- verificare singolarmente disponibilità e licenza delle versioni 1.21.1 prima di abilitarle;
- mantenere MQTT, Modbus e seriale disabilitati finché lifecycle, sicurezza e thread model non sono stati riesaminati;
- non rendere obbligatorio un port di CoFHCore.

L'API pubblica `mods.eln.api.v1.electrical` è recente e ben testata, ma espone coordinate/dimensioni e strutture interne legacy. Va preservata come specifica funzionale, non come compatibilità binaria. Una nuova API v2 potrà usare tipi moderni e ownership esplicita.

## Dipendenze e toolchain di destinazione

NeoForge 1.21.1 richiede Java 21; la macchina dispone già di Temurin 21.0.11 a 64 bit. Riferimento: [Getting Started NeoForge 1.21.1](https://docs.neoforged.net/docs/1.21.1/gettingstarted/).

La build originale usa la convention GTNH/RetroFuturaGradle, Forge 1.7.10, mapping MCP e un wrapper Gradle 9.2.1. Quella configurazione non deve essere adattata: `modern/` deve partire da un MDK NeoForge 1.21.1 pulito e versionato, con versioni fissate.

Per il codice Kotlin esistono due opzioni:

- mantenere Kotlin e aggiungere esplicitamente toolchain/plugin/runtime compatibili con NeoForge 1.21.1;
- portare inizialmente lo scaffold di piattaforma in Java e conservare il core Kotlin, evitando una riscrittura linguistica contemporanea al cambio di API.

La seconda opzione minimizza il rischio iniziale. La lingua può essere uniformata in una fase successiva; riscrivere 88.000 righe per stile non è un requisito del port.

## Strategia consigliata

### Principio

Costruire un “walking skeleton” completo e poi ampliare il catalogo. Ogni fase deve terminare con build, test e avvio su client e dedicated server.

### Fase 0 — baseline riproducibile

1. Creare MDK NeoForge 1.21.1 in `modern/` con Java 21.
2. Fissare mod id `eln`, package di destinazione e versioni.
3. Aggiungere test unitari e una CI minima.
4. Avviare client e dedicated server vuoti.

### Fase 1 — core di simulazione

1. Copiare soltanto solver/MNA e processi puri.
2. Rimuovere singleton e riferimenti a `Eln` dal core tramite configurazione esplicita.
3. Portare prima i 72 test già indipendenti o stubbed.
4. Verificare risultati numerici e stabilità rispetto all'originale con fixture golden.

### Fase 2 — primo SixNode verticale

1. Registro del tipo componente con id stabile.
2. SixNode block + block entity con sei facce.
3. Un cavo, un resistore e una sorgente di tensione.
4. Salvataggio/ricaricamento, connessione/disconnessione e rebuild rete.
5. Rendering minimale e interazione item -> faccia.
6. Test GameTest e prova multiplayer.

### Fase 3 — sincronizzazione e GUI

1. Payload tipizzati e snapshot client minimi.
2. Primo menu macchina completo.
3. Widget comuni moderni.
4. Suoni e luce dinamica con aggiornamenti limitati.

### Fase 4 — TransparentNode e catalogo

Portare i contenuti per famiglie, non per ordine alfabetico: alimentazione e cavi, misure, segnali, macchine termiche, generazione, illuminazione, meccanica, multiblocchi. Ogni famiglia include ricette, loot, traduzioni, modelli, test e criteri di completamento.

### Fase 5 — mondo, entità e integrazioni

Worldgen data-driven, minecart/entità, ghost/grid node, compatibilità con mod esterne e nuova API pubblica. Queste parti non devono bloccare il nucleo elettrico giocabile.

## Rischi principali

| Rischio | Gravità | Mitigazione |
|---|---|---|
| Perdita di identità dei descriptor basati su damage | Critica | Registro ELN namespaced + mappa legacy esplicita |
| Regressioni numeriche nel solver | Critica | Portare test prima del gameplay e usare fixture golden |
| Stato globale tra dimensioni/server | Alta | Servizi server/level-scoped, `SavedData` e block entity |
| Rendering immediato non portabile | Alta | Proof of concept precoce, baked model per geometria statica |
| Catalogo enorme registrato in codice | Alta | Manifest/datagen e vertical slice |
| Risorse con case/spazi | Alta | Script di audit e manifest dei rinomini |
| Packet legacy non validati | Alta | Payload tipizzati, limiti e validazione server-side |
| Dipendenze 1.7.10 assenti | Media | Core senza dipendenze obbligatorie; adapter opzionali |
| Prestazioni del tick server | Media/Alta | Benchmark per dimensione/rete prima di ottimizzare o parallelizzare |
| Compatibilità mondi 1.7.10 | Non garantibile direttamente | Importer offline o procedura separata, dopo stabilizzazione schema moderno |

## Criteri di riuscita del primo milestone

Il primo milestone non è “tutto il mod compila”. È raggiunto quando:

- `modern/` compila con Java 21 e NeoForge per Minecraft 1.21.1;
- client e dedicated server si avviano senza classloading client sul server;
- un blocco SixNode supporta componenti su più facce;
- cavo, resistore e sorgente formano una rete DC simulata;
- la rete sopravvive a save/reload e chunk unload/reload;
- piazzamento, rimozione e riconnessione aggiornano correttamente il grafo;
- il client riceve solo lo stato necessario al rendering;
- test unitari e GameTest coprono il percorso principale.

## Conclusione

Il port è fattibile, ma equivale a una re-platforming del motore di gioco attorno a un core fisico riutilizzabile. La scelta più sicura è preservare il solver e il comportamento, ridisegnare ownership/registri/persistenza e dimostrare subito l'architettura con un SixNode verticale. Solo dopo questa prova conviene migrare le centinaia di descriptor e asset.
