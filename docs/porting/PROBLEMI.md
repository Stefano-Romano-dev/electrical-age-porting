# Problemi, rischi concreti e debito tecnico

Gli elementi ricevono un id stabile. Quando risolti, conservarli con stato `risolto`, prova e riferimento alla decisione o modifica pertinente.

## P-001 — Repository originale non indipendente

- Stato: aperto, non bloccante
- Gravità: media
- Area: provenienza
- Descrizione: `original/` non contiene `.git`; Git risale alla radice del workspace.
- Impatto: il tag locale 1.24.8 non è verificabile con `git describe` dalla cartella originale.
- Mitigazione: trattare la release indicata dall'utente e il commit GitHub `46ab422` come riferimento; non alterare lo stato Git preesistente.

## P-002 — Dipendenza CoFH accidentalmente obbligatoria nell'originale

- Stato: compreso; da non replicare
- Gravità: media
- Area: integrazioni
- Descrizione: `Other.modIdTe` vale `"Eln"`, quindi l'integrazione RF è considerata sempre caricata mentre una classe implementa direttamente `IEnergyHandler`.
- Impatto: l'originale può richiedere CoFH Core all'avvio.
- Mitigazione: nessuna dipendenza CoFH nel core moderno; adapter opzionale separato se necessario.

## P-003 — Persistenza globale dei nodi

- Stato: aperto
- Gravità: critica
- Area: ownership/persistenza
- Descrizione: `NodeManager` è singleton e `NodeManagerNbt.writeToNBT` non salva lo stato perché la chiamata è commentata.
- Impatto: non è un modello affidabile da trasporre fra dimensioni, chunk e lifecycle server moderni.
- Mitigazione prevista: block entity per stato locale, servizi level/server-scoped e `SavedData` soltanto per dati realmente globali.

## P-004 — Catalogo basato su damage/metadata

- Stato: aperto
- Gravità: critica
- Area: registri/contenuti
- Descrizione: centinaia di descriptor condividono pochi item/blocchi e usano valori numerici.
- Mitigazione prevista: decisione D-004 e inventario legacy -> moderno.

## P-005 — Rendering immediato legacy

- Stato: aperto
- Gravità: alta
- Area: client/rendering
- Descrizione: 144 file usano LWJGL direttamente e circa 173 sono coinvolti nel rendering legacy.
- Mitigazione prevista: proof of concept precoce; baked geometry per parti statiche e BER per parti dinamiche.

## P-006 — Resource path incompatibili

- Stato: aperto
- Gravità: alta
- Area: risorse
- Descrizione: 708 percorsi contengono maiuscole o spazi.
- Mitigazione prevista: manifest di rinomina, audit automatico e verifica su ambiente case-sensitive/CI.

## P-007 — Deprecazioni Gradle nella toolchain corrente

- Stato: aperto, non bloccante
- Gravità: bassa
- Area: build
- Rilevato: 12 settembre 2026
- Descrizione: test e build segnalano API deprecate che diventeranno incompatibili con Gradle 10.
- Evidenza: warning emesso da Gradle 9.2.1 anche sulla baseline derivata dall'MDK ufficiale.
- Impatto: nessuno sul target attuale; possibile lavoro richiesto quando ModDevGradle o il wrapper verranno aggiornati.
- Mitigazione prevista: mantenere Gradle 9.2.1 fissato per il port e rieseguire periodicamente con `--warning-mode all` dopo aggiornamenti di ModDevGradle.

## P-008 — Contributi RHS sovrascritti nel solver legacy

- Stato: aperto, comportamento preservato temporaneamente
- Gravità: alta
- Area: solver MNA
- Rilevato: 12 settembre 2026
- Descrizione: `SubSystem.addToI` nella 1.24.8 usa assegnazione; più processi che contribuiscono allo stesso stato possono sovrascriversi invece di sommarsi.
- Impatto: il risultato può dipendere dall'ordine dei processi per circuiti con più sorgenti equivalenti sullo stesso nodo.
- Mitigazione prevista: D-008; prima fixture di parità legacy, poi correzione isolata con test della somma e controllo dei circuiti esistenti.

## P-009 — Lock Windows durante la clean build

- Stato: aperto, ambientale e non bloccante
- Gravità: bassa
- Area: build locale
- Rilevato: 12 settembre 2026
- Descrizione: un processo Java esterno già attivo ha mantenuto aperto un report della configuration cache; due tentativi di `clean` non hanno potuto eliminare `modern/build/reports`.
- Impatto: la clean build M1 non è stata completata in questa sessione; test e build forzata con `--rerun-tasks` sono invece riusciti.
- Mitigazione prevista: chiudere i client/daemon Gradle esterni prima della prossima clean build; non terminare automaticamente processi potenzialmente appartenenti all'utente.

## Modello

```text
## P-NNN — Titolo

- Stato: aperto | mitigato | bloccante | risolto | non applicabile
- Gravità: bassa | media | alta | critica
- Area:
- Rilevato:
- Descrizione:
- Riproduzione/evidenza:
- Impatto:
- Mitigazione o soluzione:
- Verifica di chiusura:
```
