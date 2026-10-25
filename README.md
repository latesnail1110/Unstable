# Unstable SMP Ultimate – Phase 1 (Fabric, Minecraft 26.3)

Privates Projekt, nicht zur Veroeffentlichung gedacht.

## Was Phase 1 enthaelt
- Energiesystem (Regeneration, Anzeige in der Aktionsleiste) und Cooldowns, beides **im Spielstand gespeichert** (Fabric Attachments, bleibt nach Neustart und Tod erhalten)
- 6 Faehigkeiten, serverseitig geprueft, jeweils mit eigener Mechanik
- 3 Artefakte und 3 Waffen (Rechtsklick loest die Faehigkeit aus)
- Befehle mit Tab-Vervollstaendigung, Admin-Befehle nur ab Op-Stufe Gamemaster
- Config `config/unstable_smp.json` (wird beim ersten Start erzeugt, Werte werden begrenzt)
- Selbst erzeugte 16x16-Texturen, Modelle, Sprachdateien (en_us, de_de)

**Noch nicht enthalten:** Awakening, Herzen, GUI, Quests, Events, Bosse, Dimension, Strukturen, eigene Sounds
(es werden Vanilla-Sounds verwendet), `/unstable awaken|quest|event|boss`.

## Voraussetzungen
- **JDK 25** (Minecraft 26.x laeuft auf Java 25)
- Gradle 9.x installiert **oder** IntelliJ IDEA (erzeugt den Wrapper selbst)
- Internet beim ersten Build

## Bauen und starten
1. Ordner entpacken, im Ordner `gradle wrapper` ausfuehren (erzeugt `gradlew`). IntelliJ: Ordner oeffnen, Gradle-Import abwarten.
2. `./gradlew build` -> JAR in `build/libs/unstable-smp-0.1.0.jar`
3. Zum Testen mit Entwicklungsclient: `./gradlew runClient`
4. Fuer den normalen Launcher: JAR in `mods/` legen, dazu **Fabric Loader 0.19.5+** und **Fabric API 0.161.0+26.3**.

Die Versionen stehen in `gradle.properties`. Falls der Build Loom/Fabric API nicht findet, aktuelle Werte auf https://fabricmc.net/develop/ nachschlagen.

## Befehle
| Befehl | Wer | Wirkung |
|---|---|---|
| `/unstable help` | alle | Befehlsliste |
| `/unstable stats` | Spieler | eigene Energie |
| `/unstable abilities` | alle | Kosten und Cooldowns aller Faehigkeiten |
| `/unstable give <spieler> <artefakt>` | Op 2+ | Artefakt geben |
| `/unstable weapon <spieler> <waffe>` | Op 2+ | Waffe geben |
| `/unstable reload` | Op 2+ | Config neu laden |

## Artefakte und Waffen
| Item | Faehigkeit | Wirkung | Standard (Energie / Cooldown) |
|---|---|---|---|
| `rift_core` | rift_blink | Teleport bis 10 Bloecke in Blickrichtung, stoppt vor Waenden | 25 / 5 s |
| `chaos_orb` | chaos_burst | Energieexplosion am Zielpunkt, **zerstoert keine Bloecke** | 40 / 8 s |
| `storm_crystal` | chain_lightning | Blitz springt bis zu 5 Ziele weit, Schaden sinkt pro Sprung | 30 / 6 s |
| `void_blade` | void_slash | Kegelschnitt vor dir, zieht Ziele leicht heran | 20 / 4 s |
| `stormbreaker` | thunder_slam | Donnerschlag um dich, schleudert Gegner weg | 35 / 7 s |
| `bloodfang_sword` | blood_drain | Entzieht dem anvisierten Ziel Leben, heilt dich um 50 % des Schadens | 25 / 5 s |

## Config (Auszug)
`maxEnergy`, `energyRegenPerSecond`, `pvpAbilityDamage` (false = Faehigkeiten treffen keine Spieler),
`globalDamageMultiplier`, `maxAffectedEntities`, `maxParticlesPerCast` und pro Faehigkeit
`energyCost`, `cooldownTicks`, `damage`, `range`.

## Sicherheit / Multiplayer (wie umgesetzt)
- Es gibt **kein** Client->Server-Paket fuer Faehigkeiten. Nur das echte Item-Use-Event auf dem Server loest sie aus, daher gibt es nichts zu faelschen.
- Energie, Cooldown und Schaden werden nur auf dem Server berechnet und gespeichert.
- Keine neuen Entitaeten, keine Blockzerstoerung; Partikel und Zielanzahl sind per Config begrenzt.

## Bekannte Einschraenkungen
- **Nicht kompiliert und nicht im Spiel getestet.** Der Code wurde ohne Netzwerkzugriff und ohne Minecraft-26.3-Quellen geschrieben.
  Einzelne Klassen-/Methodennamen koennen in 26.3 anders heissen. Die wahrscheinlichsten Stellen:
  `Identifier` (frueher ResourceLocation), `Commands.hasPermission/LEVEL_GAMEMASTERS` (Berechtigungs-API wurde ueberarbeitet),
  `Item.Properties#sword`, `hurtServer`, `sendSystemMessage(Component, boolean)`, `randomTeleport`, `LivingEntity#knockback`.
- Das Pack-Format `97` in der Resource-Pack-ZIP stammt aus einem Mod-Changelog, nicht aus eigener Pruefung.
- Texturen sind einfache generierte Pixel-Art, keine handgemalten Assets.
- Das Gradle-Wrapper-JAR liegt nicht bei (kein Netzwerk).
