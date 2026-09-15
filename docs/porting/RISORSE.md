# Migrazione delle risorse

## Inventario iniziale

| Tipo | Quantità |
|---|---:|
| PNG | 735 |
| OBJ | 157 |
| MTL | 157 |
| Blender | 158 |
| XCF | 85 |
| OGG | 44 |
| WAV | 2 |
| File lingua `.lang` | 19 |
| Percorsi con maiuscole o spazi | 708 |

Dimensione complessiva delle risorse: circa 107,88 MiB. La directory `model` rappresenta circa 95 MiB.

## Asset canonici della prima vertical slice M2

| Contenuto | Percorsi originali | Stato |
|---|---|---|
| Sorgente elettrica | `model/voltagesource/voltagesource.obj`, `model/voltagesource/voltagesource.png` | copiati e caricati dal renderer; confronto visivo aperto |
| Cavo bassa tensione | `sprites/cable.png` | copiato e usato dalla geometria procedurale; confronto visivo aperto |
| Resistore di potenza | `model/PowerElectricPrimitives/PowerElectricPrimitives.obj`, `model/PowerElectricPrimitives/PowerElectricPrimitives.png`, `textures/blocks/powerresistor.png` | OBJ e texture condivisa copiati e caricati; icona item separata ancora aperta |

Il percorso `PowerElectricPrimitives` è stato normalizzato in lowercase, incluso il riferimento `mtllib` interno case-sensitive. Il renderer usa gli asset canonici senza placeholder; i sorgenti `.blend` non sono copiati nel runtime. Il caricamento client è verificato, mentre il confronto estetico in gioco resta aperto.

## Licenze

- Codice: LGPL v3 secondo `original/LICENSE.md`.
- Grafica e modelli: CC BY-NC-SA 3.0 salvo eccezioni indicate nella licenza originale.
- Alcune texture hanno attribuzioni specifiche; non perdere i relativi crediti.
- Alcuni asset sono dichiarati public domain nel file di licenza originale.

Ogni asset copiato nel port deve mantenere provenienza e attribuzione. Prima di distribuire il nuovo JAR deve essere valutato esplicitamente l'effetto della clausola NonCommercial sugli asset.

## Politica di migrazione

- Non copiare indiscriminatamente tutto `assets/eln` nel JAR moderno.
- Separare asset runtime dai sorgenti grafici.
- Conservare l'estetica della 1.24.8: forma, scala, proporzioni, texture, palette, UV, animazioni, GUI, particelle e suoni sono requisiti funzionali del port.
- Non sostituire definitivamente gli asset originali con reinterpretazioni moderne o modelli vanilla-style senza una decisione esplicita dell'utente.
- Usare placeholder soltanto per sbloccare una vertical slice e marcarli chiaramente come incompleti nell'inventario e nello stato.
- Normalizzare i resource location in lowercase ASCII con underscore.
- Registrare ogni rinomina nel manifest sottostante.
- Evitare rinomini case-only in un singolo passaggio su Windows: usare un nome temporaneo oppure una patch verificabile.
- Convertire i `.lang` in JSON lowercase.
- Preferire modelli baked per geometria statica e renderer dinamici soltanto quando necessari.
- Verificare texture, UV, materiali, culling, luce e orientamento in gioco.

## Verifica della fedeltà estetica

Per ogni contenuto portato conservare immagini o video di riferimento della versione 1.24.8 e confrontare almeno:

- silhouette e dimensioni nel mondo;
- punto di montaggio e orientamento su ogni faccia applicabile;
- texture, colori, emissività e trasparenza;
- parti dinamiche, indicatori e animazioni;
- aspetto dell'item in inventario e in mano;
- GUI, tipografia, icone e disposizione dei controlli;
- suoni, volume, pitch, loop e distanza udibile;
- particelle e feedback di attivazione o guasto.

Un contenuto non passa allo stato `verificato` finché il confronto estetico non è stato eseguito, oltre ai test logici.

## Manifest delle rinomine

| Percorso originale | Percorso moderno | Contenuto associato | Stato | Note/licenza |
|---|---|---|---|---|
| `model/voltagesource/voltagesource.obj` | `models/six_node/voltagesource.obj` | sorgente elettrica | integrato | CC BY-NC-SA 3.0, provenienza legacy |
| `model/voltagesource/voltagesource.mtl` | `models/six_node/voltagesource.mtl` | sorgente elettrica | integrato | riferimenti texture normalizzati |
| `model/voltagesource/voltagesource.png` | `textures/block/six_node/voltagesource.png` | sorgente elettrica | integrato | texture canonica |
| `sprites/cable.png` | `textures/six_node/cable.png` | cavo bassa tensione | integrato | texture canonica |
| `model/PowerElectricPrimitives/PowerElectricPrimitives.obj` | `models/six_node/power_electric_primitives.obj` | resistore di potenza | integrato | path e `mtllib` lowercase |
| `model/PowerElectricPrimitives/PowerElectricPrimitives.mtl` | `models/six_node/power_electric_primitives.mtl` | resistore di potenza | integrato | riferimenti texture normalizzati |
| `model/PowerElectricPrimitives/PowerElectricPrimitives.png` | `textures/block/six_node/power_electric_primitives.png` | resistore di potenza | integrato | texture canonica condivisa |

## Registro delle conversioni

| Asset | Metodo/strumento | Input | Output | Verifica visiva | Note |
|---|---|---|---|---|---|
| Sorgente elettrica OBJ | copia selettiva + loader OBJ NeoForge | OBJ/MTL/PNG legacy | modello baked `six_node/electrical_source` | aperta | mostrato il gruppo `main`; LED dinamico ancora aperto |
| Cavo bassa tensione | geometria procedurale del renderer | dimensioni descriptor + `sprites/cable.png` | quads dinamici SixNode | aperta | larghezza `1,95/16`, altezza `0,95/16` |
| Resistore di potenza OBJ | copia selettiva + visibilità gruppi | OBJ/MTL/PNG legacy condivisi | modello baked `six_node/power_resistor` | aperta | visibili solo i quattro gruppi del resistore |
| Item della vertical slice | override per proprietà del data component | tre PNG item legacy | tre varianti di `six_node_component` | caricamento verificato; confronto aperto | nessuna icona provvisoria; nomi localizzati |

## Controlli automatici da introdurre

- percorsi solo lowercase e senza spazi;
- assenza di riferimenti a file inesistenti;
- JSON valido;
- inventario degli asset orfani;
- esclusione dal JAR di `.blend`, `.xcf`, `.xlsx`, `.aseprite`, `.ai`, `.vsd` e materiale non integrato;
- presenza di crediti e licenze richiesti.
