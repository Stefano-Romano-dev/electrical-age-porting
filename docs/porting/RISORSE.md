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
| — | — | — | non iniziato | — |

## Registro delle conversioni

| Asset | Metodo/strumento | Input | Output | Verifica visiva | Note |
|---|---|---|---|---|---|
| — | — | — | — | — | — |

## Controlli automatici da introdurre

- percorsi solo lowercase e senza spazi;
- assenza di riferimenti a file inesistenti;
- JSON valido;
- inventario degli asset orfani;
- esclusione dal JAR di `.blend`, `.xcf`, `.xlsx`, `.aseprite`, `.ai`, `.vsd` e materiale non integrato;
- presenza di crediti e licenze richiesti.
