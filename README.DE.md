<div align="center">

[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![IntelliJ Platform](https://img.shields.io/badge/IntelliJ%20Platform-2.18.1-000000?style=for-the-badge&logo=intellijidea&logoColor=white)](https://github.com/JetBrains/intellij-platform-gradle-plugin)
[![IDEA](https://img.shields.io/badge/IDEA-2025.2%E2%80%932025.3-FE315D?style=for-the-badge&logo=intellijidea&logoColor=white)](https://www.jetbrains.com/idea/)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org)
[![License](https://img.shields.io/badge/License-MIT-4B5563?style=for-the-badge)](LICENSE)
[![Sacred](https://img.shields.io/badge/Sacred-Community-8B1A1A?style=for-the-badge&labelColor=1C1410)](https://ancaria.dev)

[Русский](README.md) · [English](README.EN.md)

</div>

# Sacred Mod Development

Ein Plugin für IntelliJ IDEA, mit dem du einen Mod für Sacred Gold direkt in
der IDE anlegst und startest.

Ein Assistent legt ein fertiges Gradle-Projekt für deinen Mod an. Der Knopf
Run Sacred baut den Mod, installiert ihn ins Spiel und startet den Launcher.
Icons am Rand markieren den Einstiegspunkt deines Mods und seine
Event-Listener.

Eigene Templates hat das Plugin nicht. Der Assistent liest sie aus demselben
Generator wie `coderpack new`, beide Wege liefern also dasselbe Projekt.

## Erste Schritte

1. Installier das Plugin: Öffne **Settings | Plugins | Marketplace** und such
   nach **Sacred Mod Development** oder öffne die
   [Plugin-Seite](https://plugins.jetbrains.com/plugin/34165-sacred-mod-development).
2. Leg ein Projekt an: **File | New | Project | Sacred Mod**.
3. Gib den Spielordner an: **Settings | Tools | Sacred Mod Development**.
4. Drück auf **Run Sacred**.

Das Plugin läuft in IntelliJ IDEA 2025.2 und 2025.3, Community und Ultimate.
Willst du es aus einer Datei installieren, lade `sacred-idea-<Version>.zip`
aus den Releases herunter und nimm **Settings | Plugins | ⚙ | Install Plugin
from Disk**.

## Funktionen

### Assistent für neue Projekte

Der Assistent fragt nach Gruppe, Mod-Name, Beschreibung, Template, Sprache des
Mods und Sprache des Build-Skripts. Die Mod-ID leitet er aus dem Namen ab.
Autor, Paket und Version findest du unter **Advanced**.

Die Sprache des Mods und die des Builds wählst du unabhängig voneinander:

- **Language** ist die Sprache des Mods: Java, Kotlin oder Groovy. Kotlin und
  Groovy packen ihre Laufzeit ins JAR des Mods, Java braucht keine.
- **Build script** ist `build.gradle.kts` oder `build.gradle`.

Zwei Kontrollkästchen sind standardmäßig aktiv. **Create a Git repository**
legt ein Repository an und trägt die **Repository URL** als `origin` ein, ohne
etwas zu committen. **Create an SRML mod repository** fügt `registry.toml` und
einen Workflow hinzu, der jede neue Version des Mods veröffentlicht. Danach
können Spieler den Mod aus dem Launcher installieren.

Templates und Sprachen kommen aus dem Generator in `build`. Ein neues Template
erscheint im Assistenten, sobald das Plugin neu gebaut ist, und jedes Template
bietet nur die Sprachen an, die es unterstützt.

### Run Sacred

Das Plugin legt die Konfiguration **Run Sacred** von selbst an, wenn es
`dev.ancaria.coderpack` im Build-Skript des Wurzelordners oder eines direkten
Unterordners findet. Ist das Projekt noch nicht mit Gradle verknüpft,
verknüpft das Plugin es.

Beim Start macht Run Sacred Folgendes:

1. Es baut den Mod und installiert ihn ins Spiel mit
   `gradlew installSacredMod -PsacredDir=<Spielordner> --console=plain`.
2. Es lädt das gewählte Release von Sacred Mod Loader herunter, falls es nicht
   im Cache liegt.
3. Es kopiert den Launcher in den Spielordner, wenn sich seine SHA-256-Summe
   von der im Cache unterscheidet.
4. Es startet den Launcher.

Ist kein Spielordner eingestellt, öffnet Run Sacred die Einstellungen.

Die Konfiguration hat zwei Kontrollkästchen. **Build and install the mod
first** schaltet den Build ein: Die Konsole zeigt die Ausgabe von Gradle, und
der Launcher startet eigenständig, Stop beendet das Spiel also nicht. Ohne
Build oder ohne Gradle Wrapper folgt die Konsole dem Launcher, und Stop
beendet ihn. **Show the loader console** hängt `--debug` an.

Spielordner und Launcher-Version liegen in den IDE-Einstellungen, nicht in der
Konfiguration. Eine geteilte Konfigurationsdatei enthält deshalb keine Pfade
von deinem Rechner.

### Icons am Rand

Ein Icon für den Einstiegspunkt steht an jeder nicht abstrakten Klasse, die
`SacredMod` erbt, direkt oder über eine eigene abstrakte Klasse. Der Tooltip
sagt, ob die Klasse zum `entrypoint` im Build-Skript passt. Ein Klick öffnet
diese Angabe.

Ein Event-Icon steht an einer Methode mit `@Subscribe` und genau einem
Event-Parameter. Der Tooltip nennt das Event und die Priorität, falls sie vom
Standard abweicht. Ein Listener mit `MONITOR` bekommt ein graues Icon. Ein
Klick öffnet die Event-Klasse. Den Rückgabetyp prüft das Icon nicht, das
erledigt der Linter beim Build.

Die Icons funktionieren in Java, Kotlin und Groovy.

### Einstellungen

**Settings | Tools | Sacred Mod Development** speichert den Ordner von Sacred
Gold und das Release von Sacred Mod Loader. Im Ordner muss `pureHD.exe`,
`Sacred.exe` oder `Game.exe` liegen.

Die Liste der Releases holt das Plugin von GitHub und zeigt nur stabile
Releases mit `Sacred Mod Loader.exe`. Solange sie lädt, stehen die Versionen
aus dem Cache bereit. **Refresh** fragt sie neu ab. Ein leerer Wert heißt
„neuestes Release“ und erscheint nach dem Laden als `Latest (<Version>)`. Ist
GitHub nicht erreichbar, nimmt das Plugin die neueste Version aus dem Cache.
**Apply** lädt das gewählte Release herunter.

Die Launcher liegen unter
`<gradle user home>/caches/ancaria/launcher/<Version>/Sacred Mod Loader.exe`.
Alte Versionen löscht das Plugin nicht.

## Bauen

Gradle braucht ein JDK im `PATH`. Das Plugin wird für Java 21 gebaut, und fehlt
es, lädt Gradle Temurin 21 selbst herunter.

```
./gradlew build          # kompilieren, testen und das Zip bauen
./gradlew runIde         # eine Sandbox-IDE mit dem Plugin
./gradlew verifyPlugin   # dieselbe Prüfung wie im Marketplace
```

Der erste Build lädt eine IDE-Distribution von etwa einem Gigabyte in den
Gradle-Cache.

Die Templates kommen als `dev.ancaria.coderpack:templates` aus Maven Central.
Willst du ein noch nicht veröffentlichtes Template ausprobieren, führ
`publishToMavenLocal` im Repository `build` aus: Maven Local wird zuerst
gefragt.

## Releases

Die Version des Plugins steht in `pluginVersion` in `gradle.properties`. Hat
`master` eine Version ohne Tag `v<Version>`, veröffentlicht die CI das Plugin
im JetBrains Marketplace und legt ein GitHub-Release mit demselben Zip an.
Gibt es den Tag schon, baut und prüft die CI das Plugin nur.

Zum Veröffentlichen braucht es ein Secret, `PUBLISH_TOKEN`. Ein eigenes
Zertifikat hat das Plugin nicht, signiert wird es vom Marketplace. Wer einmal
selbst signiert, kommt davon nicht mehr zurück.

## Lizenz

MIT, siehe [LICENSE](LICENSE). Das Plugin enthält keine Spieldateien. Du
brauchst deine eigene Kopie von Sacred Gold.
