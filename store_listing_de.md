# NavXS – Google-Play-Store-Eintrag (Deutsch)

## App-Name (max. 30)

NavXS – Navigations-Overlay

## Kurzbeschreibung (max. 80)

Schwebende Buttons für Zurück, Startseite & Letzte Apps in Apps deiner Wahl.

## Vollständige Beschreibung (max. 4000)

NavXS blendet schwebende Navigationsbuttons – Zurück, Startbildschirm und Letzte Apps – über den Apps ein, die du auswählst. Praktisch für Apps, die die Systemnavigation ausblenden, für Vollbild-Apps oder wenn die normale Navigation schwer erreichbar ist.

**Funktionen**
• Drei feste Buttons: Zurück, Startbildschirm und Letzte Apps
• Auswahl der Apps, in denen das Overlay erscheinen soll
• Jeden Button frei per Drag & Drop oder mit Feinjustierung positionieren
• Größe, Farbe, Transparenz und Theme für jeden Button einzeln anpassen
• Optionaler Einbrennschutz für OLED-Displays
• Heller und dunkler Modus

**Nutzung der Bedienungshilfen-Schnittstelle (Accessibility Service API)**
NavXS benötigt den Android-Bedienungshilfe-Dienst für seine Kernfunktion:
• um zu erkennen, welche App gerade im Vordergrund ist, damit das Overlay nur in den ausgewählten Apps erscheint
• um die schwebenden Navigationsbuttons über diesen Apps anzuzeigen
• um die Aktionen Zurück, Startbildschirm und Letzte Apps auszuführen, wenn du auf einen Button tippst

NavXS verwendet ausschließlich den Paketnamen der App im Vordergrund. Eingegebene Texte, Passwörter, Nachrichteninhalte oder andere Bildschirminhalte werden nicht gelesen, und NavXS führt keine Aktionen in deinem Namen aus – außer der Navigationsaktion des Buttons, den du antippst.

Bevor der Dienst angefordert wird, erklärt NavXS dessen Nutzung und fragt mit den Optionen „Ablehnen“ und „Zustimmen“ nach deiner Einwilligung. Du aktivierst den Dienst selbst in den Android-Einstellungen und kannst ihn dort jederzeit wieder deaktivieren.

**Datenschutz**
• Die gesamte Verarbeitung erfolgt lokal auf deinem Gerät
• Es werden keine Daten erhoben, verkauft oder weitergegeben
• Keine Werbung, keine Analyse, kein Tracking
