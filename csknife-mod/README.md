# 🔪 CS Karambit Knife — Fabric Mod (1.21.1)

Ein **client-only** Fabric Mod der alle Schwerter durch ein CS2-style Karambit Messer ersetzt — komplett mit Inspect- und Trick-Animationen.

## ✅ Server-kompatibel

Der Mod läuft **nur auf deinem Client**. Auf dem Server wird nichts registriert, keine Items, keine Rezepte. Du kannst ihn auf **jedem Vanilla- oder Fabric-Server** verwenden — andere Spieler sehen bei dir normal ein Schwert, du siehst das Karambit.

## Features

- **Alle Schwerter** (Holz, Stein, Eisen, Gold, Diamant, Netherit + Modded) werden durch das Karambit ersetzt
- **3D Karambit Modell** mit Finger-Ring, im Minecraft-Blockstil
- **CS2 Doppler Finish** — Blau/Pink Farbverlauf auf der Klinge
- **Inspect Animation** (`V`) — hebt das Messer an, dreht es 360°, zeigt Klinge + Ring
- **Trick Animation** (`B`) — wirft das Messer mit Spin & Flip, fängt es wieder
- **Anpassbare Tasten** im Steuerungen-Menü

## Steuerung

| Taste | Aktion |
|-------|--------|
| `V`   | CS2 Inspect Animation |
| `B`   | Karambit Trick Spin |

Änderbar unter **Steuerung → CS Karambit Knife**.

## Build via GitHub Actions (kein lokales Gradle nötig)

1. Repo auf GitHub pushen (inkl. `gradlew` Wrapper — einmalig `gradle wrapper` lokal ausführen oder von einem anderen Fabric-Projekt kopieren)
2. Actions tab → Workflow läuft automatisch bei jedem Push
3. Artifact `csknife-mod.jar` herunterladen

## Installation

1. [Fabric Loader](https://fabricmc.net/use/) für 1.21.1 installieren
2. [Fabric API](https://modrinth.com/mod/fabric-api) herunterladen
3. Beide JARs in den `mods/` Ordner → fertig 🔪

## Wie es funktioniert (technisch)

- **`environment: "client"`** in `fabric.mod.json` — Server ignorieren diesen Mod komplett
- **`HeldItemRendererMixin`** — Interceptet das Rendering aller `SwordItem`-Subklassen, cancelt das Vanilla-Modell und rendert stattdessen das Karambit-Modell
- **`ModelLoaderMixin`** — Registriert das Karambit-Modell beim Baking, obwohl kein Item dafür existiert
- **`KarambithAnimationController`** — Steuert alle Animationszustände (IDLE, INSPECT, TRICK) mit easing functions

## Dateistruktur

```
src/main/java/com/csknife/
├── CsKnifeMod.java                          ← Minimaler Server-safe Entrypoint
├── client/
│   ├── CsKnifeClientMod.java               ← Keybindings, Sword-Erkennung
│   └── animation/
│       └── KarambithAnimationController.java ← CS2-Animationen
└── mixin/
    ├── HeldItemRendererMixin.java           ← Ersetzt Schwert-Rendering durch Karambit
    └── ModelLoaderMixin.java               ← Lädt Karambit-Modell ohne Item-Registration

src/main/resources/
├── assets/csknife/
│   ├── models/item/karambit.json           ← 3D Karambit Modell
│   ├── textures/item/karambit.png          ← Doppler Textur (64x64)
│   └── lang/en_us.json                     ← Tastennamen
└── fabric.mod.json                         ← environment: "client"
```
