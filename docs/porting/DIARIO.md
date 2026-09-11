# Diario del porting

Cronologia sintetica. I dettagli tecnici appartengono agli altri documenti; qui si annotano cosa è cambiato, perché e dove riprendere.

## 11 settembre 2026 — Nuova partenza

- Scelto di abbandonare il precedente tentativo e ripartire da Electrical Age 1.24.8.
- Confermato il target Minecraft 1.21.1, NeoForge e Java 21.
- Analizzati dimensioni, build, dipendenze, solver, nodi, persistenza, networking, GUI, rendering e risorse.
- Stabilito che CoFH Core non farà parte delle dipendenze del port moderno.
- Confermata la fedeltà estetica alla 1.24.8 come requisito del port, oltre alla fedeltà funzionale.
- Individuata la causa del requisito CoFH nell'originale: integrazione RF diretta e `Other.modIdTe = "Eln"`.
- Definita una roadmap basata su core indipendente e primo SixNode verticale.
- Creata la struttura documentale operativa del porting.
- Nessun codice del port implementato; `modern/` resta vuota.

Punto di ripresa: creare e verificare la baseline NeoForge del milestone M0.
