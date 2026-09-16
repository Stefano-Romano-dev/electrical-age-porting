# Electrical Age — port NeoForge

**Italiano** · [English](README.md)

Questo progetto porta **Electrical Age 1.24.8** da Minecraft Forge 1.7.10 a **Minecraft 1.21.1**, **NeoForge** e **Java 21**.

L'obiettivo è preservare la simulazione elettrica, il gameplay, la progressione, i tempi e l'identità visiva del mod originale, sostituendo l'integrazione con la vecchia piattaforma tramite API moderne. Il codice originale rimane in [`original/`](original/) come riferimento comportamentale e visivo in sola lettura; tutta la nuova implementazione si trova in [`modern/`](modern/).

## Stato attuale

Il progetto è in sviluppo attivo e non è ancora una release giocabile completa.

Sono attualmente funzionanti:

- il core di simulazione elettrica e termica indipendente dalla piattaforma;
- il solver MNA, lo scheduler, la persistenza e il lifecycle server;
- la prima vertical slice SixNode giocabile;
- cavi a bassa tensione, sorgente elettrica e resistore di potenza;
- piazzamento multi-faccia, rimozione, drop, salvataggio e ricaricamento dei chunk;
- modelli OBJ, texture, varianti degli item e rendering dei cavi originali;
- connessioni dei cavi complanari, interne e attorno agli spigoli esterni.
- sincronizzazione dedicated→client della vertical slice SixNode M2.
- primo menu server-authoritative per configurare la sorgente elettrica.

La baseline attuale supera **171 test unitari** e **11 GameTest NeoForge**, comprese le verifiche su dedicated server, persistenza e configurazione client/server reale.

Restano in sviluppo il catalogo completo dei dispositivi, le GUI e gli inventari di configurazione, audio e particelle, la verifica multiplayer dei futuri payload interattivi e le integrazioni opzionali con altre mod.

## Sviluppo

Su Windows, dalla cartella [`modern/`](modern/):

```powershell
$env:GRADLE_USER_HOME = (Resolve-Path '.gradle').Path
.\gradlew.bat test
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat runServer
```

Consulta [`STATO_PORTING.md`](STATO_PORTING.md) per lo stato corrente e [`docs/porting/README.md`](docs/porting/README.md) per la documentazione tecnica completa.

## Licenza

Consulta [`modern/LICENSE.md`](modern/LICENSE.md) e la licenza del progetto originale in [`original/LICENSE.md`](original/LICENSE.md).
