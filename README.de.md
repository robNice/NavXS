# NavXS

Eine schlanke Android-App, die ein schwebendes Navigations-Overlay mit **Zurück**-, **Home**- und **Letzte Apps**-Buttons über jede beliebige App legt. Ideal für Geräte, bei denen die Gestennavigation mit einer bestimmten App kollidiert, oder wenn du einfach dauerhaft verfügbare, vollständig anpassbare Soft-Keys genau dort haben möchtest, wo du sie brauchst.

<p>
  <img src="docs/settings_light.png" width="270" alt="Einstellungen – helles Design">
  &nbsp;&nbsp;&nbsp;
  <img src="docs/settings_dark.png" width="270" alt="Einstellungen – dunkles Design">
</p>

**Erste Schritte**

1. NavXS starten und die Berechtigung für den Bedienungshilfe-Dienst erteilen.
2. Im Reiter **Apps** die Apps aktivieren, für die das Overlay angezeigt werden soll.
3. Im Reiter **Einstellungen** Aussehen und Position jedes Buttons anpassen.
4. Eine aktivierte App öffnen — das Overlay erscheint automatisch und verschwindet, wenn du die App verlässt.

---

## Inhalt

1. [Bedienungshilfe-Dienst](#1-bedienungshilfe-dienst)
2. [Apps](#2-apps)
3. [Einstellungen](#3-einstellungen)
   - [3.1 Buttons positionieren](#31-buttons-positionieren)
   - [3.2 Button auswählen](#32-button-auswählen)
   - [3.3 Aktiv](#33-aktiv)
   - [3.4 Button](#34-button)
   - [3.5 Button-Hintergrund](#35-button-hintergrund)
4. [Vorschau-Fahne](#4-vorschau-fahne)
5. [Lizenz](#5-lizenz)

---

## 1 Bedienungshilfe-Dienst

NavXS verwendet den Android-Bedienungshilfe-Dienst, um zu erkennen, welche App gerade im Vordergrund ist. Dies ist der einzige Mechanismus, den Android einer App bietet, um zu wissen, was zu einem bestimmten Zeitpunkt läuft — er wird benötigt, damit das Overlay weiß, wann es erscheinen und wann es sich verstecken soll.

<p align="center">
  <img src="docs/accessibility-service-gate.png" width="270" alt="Bedienungshilfe-Dienst erforderlich">
</p>

Beim ersten Start erscheint dieser Bildschirm, wenn der Dienst noch nicht aktiviert ist. Tippe auf **Bedienungshilfe-Einstellungen öffnen**, suche NavXS in der Liste und schalte es ein. Kehre zur App zurück und tippe auf **Erneut prüfen**, um fortzufahren.

**Was der Dienst tut — und was nicht**

| | |
|---|---|
| ✅ | Liest den **Paketnamen** der Vordergrund-App, um zu entscheiden, ob das Overlay angezeigt wird. |
| ❌ | Liest **nicht** den Fensterinhalt, Text oder UI-Elemente anderer Apps (`canRetrieveWindowContent="false"`). |
| ❌ | Überträgt **keine** Daten. Die gesamte Verarbeitung bleibt auf deinem Gerät. |

Die Berechtigung kann jederzeit unter **Android-Einstellungen → Bedienungshilfen → NavXS** widerrufen werden.

---

## 2 Apps

<p>
  <img src="docs/apps_light.png" width="270" alt="Apps-Reiter – helles Design">
  &nbsp;&nbsp;&nbsp;
  <img src="docs/apps_dark.png" width="270" alt="Apps-Reiter – dunkles Design">
</p>

Die Liste zeigt alle auf deinem Gerät installierten Apps. Aktiviere eine App, damit das NavXS-Overlay erscheint, sobald sie im Vordergrund ist. Nutze die Suchleiste zum Filtern nach Namen oder aktiviere **System-Apps anzeigen**, um Systemapps einzublenden.

Aktivierte Apps erscheinen für schnellen Zugriff oben in der Liste. Das Deaktivieren einer App entfernt das Overlay beim nächsten Mal, wenn diese App in den Vordergrund kommt.

---

## 3 Einstellungen

Alle Einstellungen betreffen nur den aktuell ausgewählten Button. Die drei Buttons — Zurück, Home und Letzte Apps — werden unabhängig voneinander konfiguriert.

<p>
  <img src="docs/settings_light.png" width="270" alt="Einstellungen-Reiter – helles Design">
  &nbsp;&nbsp;&nbsp;
  <img src="docs/settings_dark.png" width="270" alt="Einstellungen-Reiter – dunkles Design">
</p>

### 3.1 Buttons positionieren

Öffnet einen Vollbild-Editor, in dem du Buttons an die genaue Position ziehst, die sie im Live-Overlay einnehmen sollen. Tippe einen Button an, um ihn zu wählen, ohne ihn zu verschieben. Das Zurücksetzen-Symbol (↺) stellt alle Standardpositionen wieder her. Die Präzisionssteuerung verschiebt den Button um eine einstellbare Schrittweite in Pixeln.

<p>
  <img src="docs/button_positioning_light.png" width="270" alt="Positionierungsmodus – helles Design">
  &nbsp;&nbsp;&nbsp;
  <img src="docs/button_positioning_dark.png" width="270" alt="Positionierungsmodus – dunkles Design">
</p>

Das Panel **Präzise Positionierung** bietet Schrittweiten von 1 px, 5 px und 10 px und zeigt die genauen Pixelkoordinaten des ausgewählten Buttons in Echtzeit an.

### 3.2 Button auswählen

Tippe oben auf dem Einstellungsbildschirm auf **Zurück**, **Home** oder **Letzte Apps**, um einen Button auszuwählen. Alle folgenden Einstellungen betreffen nur den ausgewählten Button.

### 3.3 Aktiv

Schaltet den ausgewählten Button im Overlay ein oder aus. Mindestens ein Button muss jederzeit aktiv bleiben.

### 3.4 Button

| Einstellung | Beschreibung |
|-------------|--------------|
| **Theme** | Wählt den Icon-Stil. Pro Button sind mehrere Design-Varianten verfügbar. |
| **Farbe** | Setzt die Icon-Farbe. |
| **Deckkraft** | Icon-Transparenz — 0 unsichtbar, 100 vollständig deckend. |
| **Größe** | Skaliert das Icon relativ zur Standardgröße. |

### 3.5 Button-Hintergrund

Ein optionaler Kreis hinter dem Icon zur Verbesserung der Sichtbarkeit auf jedem App-Hintergrund.

| Einstellung | Beschreibung |
|-------------|--------------|
| **Farbe** | Füllfarbe des Hintergrundkreises. |
| **Deckkraft** | Transparenz des Hintergrundkreises. |
| **Größe** | Durchmesser des Kreises relativ zum Icon. |
| **Weichheit** | 0 = harte Kante; 100 = vollständig ausgeblendeter Verlaufsrand. |

---

## 4 Vorschau-Fahne

Die kleine Fahne oben rechts im Einstellungsbildschirm öffnet ein Seitenpanel mit dem ausgewählten Button auf weißem, grauem und schwarzem Hintergrund. So lässt sich beurteilen, wie der Button auf einem beliebigen App-Hintergrund aussieht, ohne die Einstellungen zu verlassen.

<p>
  <img src="docs/preview_light.png" width="380" alt="Vorschau-Panel – helles Design">
</p>
<p>
  <img src="docs/preview_dark.png" width="380" alt="Vorschau-Panel – dunkles Design">
</p>

---

## 5 Lizenz

NavXS steht unter der [MIT-Lizenz](LICENSE.md).
