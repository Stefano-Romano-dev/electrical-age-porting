# Registro delle verifiche

Registrare soltanto comandi e prove realmente eseguiti. Ogni risultato deve essere riferito al codice corrente o a un commit identificabile.

## Ambiente iniziale

- Data: 11 settembre 2026
- Sistema: Windows / PowerShell
- Java rilevato: Temurin OpenJDK 21.0.11, 64 bit
- Stato `modern/`: cartella vuota

## Analisi statica della sorgente originale

- Data: 11 settembre 2026
- Esito: completata
- Risultati principali: 478 sorgenti Java, 357 Kotlin, 90 test, 1.442 risorse, circa 88.000 righe principali.
- Nota: non è stata eseguita la build originale e non sono stati modificati file sotto `original/`.

## Tabella delle esecuzioni moderne

| Data | Revisione | Comando/prova | Esito | Note o log |
|---|---|---|---|---|
| — | — | Nessuna esecuzione | — | Il progetto moderno non esiste ancora |

## Modello di registrazione

```text
### AAAA-MM-GG — Titolo

- Revisione/stato:
- Ambiente:
- Comando o procedura:
- Esito: superato | fallito | parziale
- Risultato osservato:
- Log/artifact:
- Problema collegato:
```

Per le prove manuali indicare mondo, lato client/server, sequenza delle azioni e risultato atteso. “Si avvia” non sostituisce una prova dedicated server.
