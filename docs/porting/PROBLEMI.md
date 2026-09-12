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

- Stato: risolto
- Gravità: bassa
- Area: build locale
- Rilevato: 12 settembre 2026
- Descrizione: il `runClient` manuale aveva lasciato attivo un daemon Gradle globale; inoltre il report della configuration cache era stato creato da un'esecuzione elevata e la sandbox normale non poteva rimuoverlo.
- Impatto: i primi tentativi di `clean` non hanno potuto eliminare `modern/build/reports`.
- Soluzione: identificato il PID dalla command line, arrestato esclusivamente il daemon con `gradlew --stop`, quindi eseguita `clean build` con lo stesso livello di accesso usato per creare gli output.
- Verifica di chiusura: `clean build --no-daemon` superata il 12 settembre 2026, 11 task senza errori.

## P-010 — Il rapporto del trasformatore non invalida la matrice

- Stato: aperto, comportamento legacy preservato
- Gravità: media
- Area: solver MNA / trasformatore
- Rilevato: 12 settembre 2026
- Descrizione: `Transformer.setRatio` nella 1.24.8 aggiorna il rapporto ma non chiama `dirty`; una matrice già generata continua quindi a usare il rapporto precedente finché non viene invalidata per un'altra causa.
- Riproduzione/evidenza: fixture con rapporto iniziale 1, cambio a 2 e invalidazione manuale; la tensione secondaria cambia soltanto dopo l'invalidazione.
- Impatto: un cambio dinamico del rapporto può essere ritardato e dipendere da altre modifiche della rete.
- Mitigazione prevista: mantenere la semantica per parità D-011; verificare come i dispositivi originali impostano il rapporto prima di proporre qualsiasi correzione.

## P-011 — Un componente rimosso ma collegato può essere riscoperto

- Stato: aperto, comportamento legacy preservato
- Gravità: media
- Area: lifecycle MNA
- Rilevato: 12 settembre 2026 durante l'audit di parità
- Descrizione: `RootSystem.removeComponent` rimuove il componente dall'insieme pending, ma `buildSubSystem` attraversa tutti i componenti ancora collegati agli stati; senza `breakConnection` il componente rientra quindi nella rete.
- Riproduzione/evidenza: rimuovere un resistore da una rete generata senza scollegarlo e rigenerare; il resistore appartiene nuovamente al sottosistema.
- Impatto: il teardown deve rispettare l'ordine legacy scollegamento/rimozione; cambiarlo silenziosamente produrrebbe una differenza di lifecycle.
- Mitigazione prevista: mantenere il comportamento per D-011 e coprire con test i percorsi di rimozione dei futuri nodi.

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
