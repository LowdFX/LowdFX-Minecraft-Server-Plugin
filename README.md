# LowdFX Minecraft Plugin

**Version**: 1.0  
**Minecraft API-Version**: 1.21  
**Soft Dependency**: [ProtocolLib](https://github.com/dmulloy2/ProtocolLib)  
**ACHTUNG**: Soft Dependency ist nicht notwendig, da diese nur für Zukünftige Projekt eingebunden ist!

## Beschreibung
Das LowdFX Plugin erweitert deinen Minecraft-Server um zahlreiche wie auch hilfreiche Befehle und Funktionen.
Von Spielerinteraktionen über Home, Warp oder Spawn bis hin zu Invsee und Kistensperr-Befehlen bietet es ein umfangreiches Funktionsspektrum.
Des weitern wurden Standard Commands wie Gamemode und der gleichen vereinfacht um einen besseren Umgang zu haben.
Personalisierte Wilkommensnachricht wie auch Serverleave Nachricht, es wurde auch mit einem einfachen Vanish Befehl erweitert.

---

## Befehle

### Allgemein
| **Befehl**         | **Beschreibung**                                        | **Berechtigung**          | **Nutzung**           |
|--------------------|---------------------------------------------------------|---------------------------|-----------------------|
| `/spawn`           | Teleportiert dich zum Spawn.                            | `lowdfx.spawn` (TRUE)     | `/spawn`              |
| `/home`            | Teleportiert dich zu deinem Home.                       | `lowdfx.home` (TRUE)      | `/home`               |
| `/warp`            | Teleportiert dich zu einem angegebenen Warp.            | `lowdfx.warp` (TRUE)      | `/warp <warp>`        |
| `/gm`              | Ändert deinen Gamemode.                                 | `lowdfx.gm` (OP)          | `/gm <mode>`          |
| `/fly`             | Aktiviert oder deaktiviert den Fly-Modus.               | `lowdfx.fly` (OP)         | `/fly`                |
| `/heal`            | Heilt dich vollständig.                                 | `lowdfx.heal` (OP)        | `/heal`               |
| `/feed`            | Stillt deinen Hunger.                                   | `lowdfx.feed` (OP)        | `/feed`               |

### Inventar und Werkzeuge
| **Befehl**         | **Beschreibung**                                        | **Berechtigung**          | **Nutzung**            |
|--------------------|---------------------------------------------------------|---------------------------|------------------------|
| `/anvil`           | Öffnet einen Amboss.                                    | `lowdfx.anvil` (OP)       | `/anvil`               |
| `/workbench`       | Öffnet eine Werkbank.                                   | `lowdfx.workbench` (OP)   | `/workbench`           |
| `/endersee`        | Öffnet die Enderchest eines Spielers.                   | `lowdfx.endersee` (OP)    | `/endersee <Spieler>`  |
| `/invsee`          | Öffnet das Inventar eines Spielers.                     | `lowdfx.invsee` (OP)      | `/invsee <Spieler>`    |
| `/trash`           | Öffnet einen Mülleimer.                                 | `lowdfx.trash` (OP)       | `/trash`               |

### Spezialfunktionen
| **Befehl**         | **Beschreibung**                                        | **Berechtigung**          | **Nutzung**            |
|--------------------|---------------------------------------------------------|---------------------------|------------------------|
| `/vanish`          | Macht dich unsichtbar.                                  | `lowdfx.vanish` (OP)      | `/vanish` oder `/v`    |
| `/lock`            | Sperrt Kisten und schützt sie vor Fremdzugriff.         | `lowdfx.lock` (TRUE)      | `/lock`                |
| `/warn`            | Zugriff auf das Warnsystem.                             | `lowdfx.warn` (TRUE)      | `/warn`                |

### Plugin-Hauptbefehl
| **Befehl**         | **Beschreibung**                                        | **Berechtigung**              | **Nutzung**        |
|--------------------|---------------------------------------------------------|-------------------------------|--------------------|
| `/low`             | Hauptbefehl des Plugins.                                | `lowdfx.low` (TRUE)           | `/low`             |
| `/low help`        | Zeigt Hilfe über das Plugin.                            | `lowdfx.low.help` (TRUE)      | `/low help`        |
| `/low info`        | Zeigt Informationen über das Plugin.                    | `lowdfx.low.info` (OP)        | `/low info`        |
| `/low starterkit`  | Gibt ein Starterkit aus.                                | `lowdfx.low.starterkit` (TRUE)| `/low starterkit`  |
| `/low opkit`       | Gibt ein OP Kit aus.                                    | `lowdfx.low.opkit` (OP)       | `/low opkit`       |

---

## Berechtigungen

Die Berechtigungen und ihre Standardwerte (TRUE, OP, FALSE, usw.) steuern, wer welche Befehle nutzen kann. Hier eine vollständige Übersicht:
Diese Permissions Anleitung ist in der permissions.yml nochmal dargestellt wie diese zu nutzen sind, dies muss nur verändert werden, wenn kein Permissions Plugin verwendet wird.
Es wurden bereits voreingestellte Optionen angegeben.

| **Berechtigung**          | **Beschreibung**                                                   | **Standard** |
|---------------------------|--------------------------------------------------------------------|--------------|
| `lowdfx.gm`               | Erlaubt die Nutzung von `/gm`.                                     | OP           |
| `lowdfx.fly`              | Erlaubt die Nutzung von `/fly`.                                    | OP           |
| `lowdfx.heal`             | Erlaubt die Nutzung von `/heal`.                                   | OP           |
| `lowdfx.feed`             | Erlaubt die Nutzung von `/feed`.                                   | OP           |
| `lowdfx.vanish`           | Erlaubt die Nutzung von `/vanish`.                                 | OP           |
| `lowdfx.trash`            | Erlaubt die Nutzung von `/trash`.                                  | OP           |
| `lowdfx.invsee`           | Erlaubt die Nutzung von `/invsee`.                                 | OP           |
| `lowdfx.endersee`         | Erlaubt die Nutzung von `/endersee`.                               | OP           |
| `lowdfx.anvil`            | Erlaubt die Nutzung von `/anvil`.                                  | OP           |
| `lowdfx.workbench`        | Erlaubt die Nutzung von `/workbench`.                              | OP           |
| `lowdfx.spawn`            | Erlaubt die Nutzung von `/spawn`.                                  | TRUE         |
| `lowdfx.home`             | Erlaubt die Nutzung von `/home`.                                   | TRUE         |
| `lowdfx.home.admin`       | Erlaubt das Setzen, Entfernen und Teleportieren zu anderen Homes.  | OP           |
| `lowdfx.warp`             | Erlaubt die Nutzung von `/warp`.                                   | TRUE         |
| `lowdfx.warn`             | Erlaubt die Nutzung von `/warn`.                                   | TRUE         |
| `lowdfx.lock`             | Erlaubt die Nutzung von `/lock`.                                   | TRUE         |
| `lowdfx.low`              | Erlaubt die Nutzung des Hauptkommandos `/low`.                     | TRUE         |
| `lowdfx.low.starterkit`   | Erlaubt die Nutzung von `/low starterkit`.                         | TRUE         |
| `lowdfx.low.opkit`        | Erlaubt die Nutzung von `/low opkit`.                              | OP           |

---

## Konfigurationsdateien
- **`config.yml`**: Konfigurationseinstellungen für das Plugin.
- **`permissions.yml`**: Übersicht aller Berechtigungen.
- **`VanishedPlayers.yml`**: Speicherung unsichtbarer Spieler um nach dem Server neustart vanished zu bleiben.

---

## Installation
1. Lade die `.jar`-Datei in den `plugins`-Ordner deines Minecraft-Servers.
2. Starte den Server neu.
3. Konfiguriere das Plugin bei Bedarf in der `config.yml`.

---

## Soft Dependency
- Das Plugin kann mit [ProtocolLib](https://github.com/dmulloy2/ProtocolLib) erweitert werden, um zusätzliche Funktionen zu nutzen.
- Wird aber NICHT benötigt, dies ist nur für Zukünftige Pläne integriert!
---

Viel Spaß mit meinem Plugin!
