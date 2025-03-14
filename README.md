# LowdFX Minecraft Plugin

**Version**: `1.1.0`  
**Paper API-Version**: `>=1.21`

## Beschreibung

Das LowdFX Plugin erweitert deinen Minecraft-Server um zahlreiche wie auch hilfreiche Befehle und Funktionen.

Von Spielerinteraktionen über Home, Warp oder Spawn bis hin zu Inv-See und Kistensperr-Befehlen bietet es ein umfangreiches Funktionsspektrum.
Des Weiteren wurden Standard Commands wie Gamemode und der gleichen vereinfacht, um einen besseren Umgang zu haben.

---

## Befehle

### Allgemein

Es gibt keine help/man-pages, da das Plugin die Brigadier API benutzt, und damit automatisch mit dem vanilla Command-System integriert ist.
Für eine Liste an Befehlen, einfach `/lowdfx:` in die Chatbox schreiben.

Alle Befehle sind in `src/main/java/at/lowdfx/lowdfx/LowdFX.java` ab ~Z.65 zu finden und der Quellcode unter `src/main/java/at/lowdfx/lowdfx/command/`.

---

## Berechtigungen

Beim ersten Starten des Plugins eine permissions.json datei erstellt, in der alle Berechtigungen modifiziert werden können.

Alle Berechtigungen sind in `src/main/java/at/lowdfx/lowdfx/util/Perms.java` zu finden.  

---

## Dateien

- **`config.yml`**: Konfiguration für das Plugin.
- **`permissions.json`**: Konfiguration für die Berechtigungen.
- **`data/`**: Alle Daten des Plugins. **Modifiziere diese Dateien nur, wenn du weisst, was du tust!**

---

## Installation

1. Lade die `.jar`-Datei in den `plugins`-Ordner deines Minecraft-Servers.
2. Starte den Server neu.
3. Konfiguriere das Plugin bei Bedarf in der `config.yml`.

---

Viel Spaß mit dem Plugin!
