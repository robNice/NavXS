# Manuelle Testliste NavXS

## Accessibility-Gate

- Service nicht aktiv: Nur der Accessibility-Gate-Screen ist sichtbar.
- Button `Bedienungshilfe-Einstellungen öffnen` öffnet die Systemeinstellungen.
- Button `Erneut prüfen` aktualisiert den Status.

## Tabs und App-Auswahl

- Service aktiv: Genau zwei Reiter `Apps` und `Einstellungen` sind sichtbar.
- Suchfeld filtert nach App-Name und Paketname.
- Aktivierte Apps stehen im oberen Bereich.
- Deaktivierte Apps stehen im unteren Bereich.
- `System-Apps anzeigen` blendet System-Apps ein und aus.
- App-Auswahl bleibt nach App-Neustart erhalten.

## Settings und Vorschau

- Wechsel zwischen `Zurück`, `Home` und `Letzte Apps` aktualisiert die Controls.
- Pinker Rahmen markiert immer nur den aktuell ausgewählten Button.
- `Aktiv` blendet nur den ausgewählten Button ein oder aus.
- Farbe, Deckkraft und Größe wirken nur auf den ausgewählten Button.
- Theme-Liste zeigt nur passende Themes für den ausgewählten Button.
- `Edit Mode` erlaubt Drag nur für den ausgewählten Button.
- Präzisionssteuerung bewegt nur den ausgewählten Button.
- Schrittweiten 1 px, 5 px und 10 px funktionieren.
- `Position zurücksetzen` setzt nur den ausgewählten Button zurück.

## Overlay auf Gerät

- Ausgewählte Ziel-App öffnen: Overlay erscheint.
- Nicht ausgewählte App öffnen: Overlay verschwindet.
- NavXS selbst öffnen: Overlay bleibt verborgen.
- `BACK`, `HOME` und `RECENTS` lösen die jeweiligen globalen Aktionen aus.
- Wenn der Accessibility-Service beendet wird, verschwindet das Overlay.
