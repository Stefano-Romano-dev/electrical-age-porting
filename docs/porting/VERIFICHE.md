# Registro delle verifiche

Registrare soltanto comandi e prove realmente eseguiti. Ogni risultato deve essere riferito al codice corrente o a un commit identificabile.

## Ambiente iniziale

- Data: 11 settembre 2026
- Sistema: Windows / PowerShell
- Java rilevato: Temurin OpenJDK 21.0.11, 64 bit
- Stato `modern/`: cartella vuota

## Ambiente della baseline moderna

- Data: 12 settembre 2026
- Sistema: Windows 11 / PowerShell
- JVM osservata nei run: Eclipse Adoptium OpenJDK 21.0.11+10-LTS, 64 bit
- Minecraft: 1.21.1
- NeoForge: 21.1.250
- ModDevGradle: 2.0.146
- Gradle wrapper: 9.2.1
- Parchment: 2024.11.17
- Mod: `eln` 0.1.0-alpha.1

## Analisi statica della sorgente originale

- Data: 11 settembre 2026
- Esito: completata
- Risultati principali: 478 sorgenti Java, 357 Kotlin, 90 test, 1.442 risorse, circa 88.000 righe principali.
- Nota: non è stata eseguita la build originale e non sono stati modificati file sotto `original/`.

## Tabella delle esecuzioni moderne

| Data | Revisione | Comando/prova | Esito | Note o log |
|---|---|---|---|---|
| 12 settembre 2026 | working tree M0 | `.\\gradlew.bat test --no-daemon` | superato | JUnit 5; 6 task, build riuscita |
| 12 settembre 2026 | working tree M0 | `.\\gradlew.bat clean build --no-daemon` | superato | JAR `eln-0.1.0-alpha.1.jar`; build pulita riuscita |
| 12 settembre 2026 | working tree M0 | `.\\gradlew.bat runClient --no-daemon` | superato | Menu client raggiunto; entry point `mods.eln.ElectricalAge` caricato |
| 12 settembre 2026 | working tree M0 | `.\\gradlew.bat runServer --no-daemon` | superato | Dedicated server arrivato a `Done (3.104s)`; mod caricato lato server |

### 12 settembre 2026 — Ispezione del JAR M0

- Artefatto: `modern/build/libs/eln-0.1.0-alpha.1.jar`
- SHA-256 osservato: `345DB78208272E72AE414B22DC7D3D96EE430C47F97107D936E6DC5AB53D652E`
- Contenuto verificato: classi `mods.eln`, metadata `META-INF/neoforge.mods.toml`, lingue `en_us`/`it_it`, licenza.
- Dipendenze dichiarate: soltanto Minecraft 1.21.1 e NeoForge da 21.1.250; nessun riferimento CoFH.
- Nota: l'hash identifica questa build locale e cambierà con qualsiasi modifica successiva.

### Note sulle prime esecuzioni

- Il primo `test` è fallito perché Gradle 9 richiede esplicitamente `junit-platform-launcher`; la dipendenza runtime è stata aggiunta e la prova successiva è passata.
- Il primo `build` è fallito per il contesto della closure che rinominava la licenza; il nome è ora calcolato in configurazione e la clean build è passata.
- Il primo `runServer` in sandbox non ha potuto scaricare una dipendenza Netty; ripetuto con accesso di rete, il server è partito correttamente.
- Al primo avvio server Minecraft registra come errore l'assenza iniziale di `server.properties`, poi lo crea e raggiunge regolarmente `Done`; non è un errore del mod.
- Il workflow `.github/workflows/modern-build.yml` è configurato per Java 21 e `modern/`, ma non è ancora stato osservato su GitHub Actions in questa revisione locale.

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
