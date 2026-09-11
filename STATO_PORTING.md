# Stato del porting di Electrical Age

Aggiornato: 11 settembre 2026

## Stato generale

**Fase corrente: analisi iniziale completata; implementazione non iniziata.**

Target confermato:

- sorgente: Electrical Age 1.24.8 / Forge 1.7.10;
- destinazione: Minecraft 1.21.1 / NeoForge / Java 21;
- sorgente legacy in `original/`;
- nuovo progetto in `modern/`.

`modern/` è attualmente vuota. Nessun file dell'implementazione originale è stato modificato.

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
- [ ] Creare il workspace NeoForge 1.21.1 in `modern/`.
- [ ] Ottenere una build vuota riproducibile.
- [ ] Avviare client e dedicated server.
- [ ] Estrarre e portare il core di simulazione con i test.
- [ ] Implementare il primo SixNode verticale.

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

## Rilevazioni importanti

- Il progetto principale contiene 835 sorgenti e circa 88.000 righe.
- 607 file dipendono direttamente da API Minecraft/Forge o integrazioni legacy.
- 72 dei 90 test sono privi di import diretti Minecraft/Forge e costituiscono la base migliore per validare il core.
- Sono presenti almeno 409 registrazioni descriptor/element statiche rilevate.
- 144 file usano LWJGL direttamente e circa 173 sono legati al rendering legacy.
- 708 resource path contengono maiuscole o spazi e richiedono normalizzazione tracciata.
- `NodeManager` è globale e `NodeManagerNbt.writeToNBT` non scrive lo stato: l'ownership e la persistenza devono essere ridisegnate.
- CoFH Core è effettivamente necessario nella 1.24.8: la relazione Modrinth è `required` e `Other.modIdTe = "Eln"` rende sempre attiva l'integrazione RF che implementa `IEnergyHandler`.
- La macchina dispone già di Java 21 a 64 bit (Temurin 21.0.11).
- `original/` non ha un `.git` proprio; il repository Git effettivo è la radice del workspace e contiene modifiche/cancellazioni preesistenti che non devono essere alterate.

## Prossimo milestone: baseline NeoForge

Deliverable previsto:

- MDK NeoForge fissato a Minecraft 1.21.1 dentro `modern/`;
- mod id `eln` e package definitivi;
- build Gradle riuscita;
- avvio `runClient` riuscito;
- avvio `runServer` riuscito senza riferimenti client-only;
- test minimale eseguito in CI/localmente;
- aggiornamento di questo file con versioni esatte, comandi e risultati.

Il dettaglio tecnico e la roadmap completa sono in [ANALISI_PORTING.md](./ANALISI_PORTING.md).

L'indice di tutti i documenti operativi è in [docs/porting/README.md](./docs/porting/README.md).
