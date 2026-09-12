# Documentazione operativa del porting

Questo indice separa informazioni con cicli di vita diversi, evitando che un unico documento diventi ambiguo o obsoleto.

| Documento | Scopo | Quando aggiornarlo |
|---|---|---|
| [`../../ANALISI_PORTING.md`](../../ANALISI_PORTING.md) | Analisi consolidata del progetto originale, rischi e strategia | Quando emerge un fatto architetturale importante |
| [`../../STATO_PORTING.md`](../../STATO_PORTING.md) | Dashboard dello stato corrente e prossimo milestone | Dopo ogni avanzamento verificato |
| [`ROADMAP.md`](ROADMAP.md) | Ordine delle fasi, dipendenze e criteri di uscita | Quando cambia la sequenza del lavoro |
| [`DECISIONI.md`](DECISIONI.md) | Registro delle decisioni architetturali | Prima o insieme a una scelta non banale |
| [`INVENTARIO.md`](INVENTARIO.md) | Mappa legacy -> moderno di sistemi e contenuti | Durante l'analisi e il port di ogni famiglia |
| [`MNA_AUDIT.md`](MNA_AUDIT.md) | Audit file/API/schema del solver MNA legacy e moderno | Quando cambia il core MNA o viene chiusa una lacuna |
| [`PROCESS_AUDIT.md`](PROCESS_AUDIT.md) | Audit dei processi esterni a MNA e routing delle dipendenze rimandate | Quando viene portato o riclassificato un processo legacy |
| [`VERIFICHE.md`](VERIFICHE.md) | Registro riproducibile di build, test e prove | Dopo ogni verifica eseguita |
| [`PROBLEMI.md`](PROBLEMI.md) | Bug, rischi concreti, blocchi e debito tecnico | Quando un problema viene trovato o risolto |
| [`RISORSE.md`](RISORSE.md) | Migrazione, rinomina, conversione e licenze degli asset | Durante ogni operazione sulle risorse |
| [`FEDELTA.md`](FEDELTA.md) | Contratto di parità percepita e matrice delle differenze | Per ogni contenuto portato o differenza intenzionale |
| [`DIARIO.md`](DIARIO.md) | Cronologia sintetica del lavoro | Alla fine di ogni sessione significativa |

## Regola contro le duplicazioni

- Un'attività compare come checkbox soltanto in `STATO_PORTING.md` o `ROADMAP.md`.
- Una decisione vive in `DECISIONI.md`; gli altri documenti possono collegarla senza ricopiarla integralmente.
- Un comando e il suo risultato vivono in `VERIFICHE.md`.
- Un bug aperto vive in `PROBLEMI.md`.
- I dettagli per singolo dispositivo o id vivono in `INVENTARIO.md`.
- Le prove di equivalenza e le differenze approvate vivono in `FEDELTA.md`.

## Stato iniziale

La documentazione operativa è stata istituita l'11 settembre 2026, dopo l'analisi della base Electrical Age 1.24.8. La cartella `modern/` era vuota e l'implementazione non era ancora iniziata.
