NavXS Navigations-Buttons

NavXS fügt ausgewählten Android-Apps anpassbare Navigationsbuttons auf dem Bildschirm hinzu.

Manche Apps blenden die Navigationsleiste des Systems aus, nutzen Vollbild-Layouts oder machen die normale Navigation schwer erreichbar. NavXS hilft, indem es ein kleines, konfigurierbares Navigations-Overlay über den Apps einblendet, die du auswählst.

Anders als ein globales Navigations-Overlay erscheint NavXS nur dort, wo du es wirklich brauchst. Du legst fest, in welchen installierten Apps die Buttons angezeigt werden – alle anderen Apps bleiben unverändert.

NavXS bietet drei schwebende Buttons – <b>Zurück</b>, <b>Startbildschirm</b> und <b>Letzte Apps</b>. Platziere sie dort, wo sie am besten erreichbar sind, und passe ihr Aussehen nach deinen Wünschen an.

<b>Typische Einsatzbereiche</b>

• Apps, die die Android-Navigationsleiste ausblenden
• Apps im Vollbildmodus oder mit immersiven Layouts
• Geräte, auf denen Navigationsgesten unzuverlässig oder unpraktisch sind
• Große Smartphones, auf denen die Systemnavigation schwer zu erreichen ist
• Tablets oder fest montierte Geräte
• Kiosk-Anwendungen, bei denen die Navigation erreichbar bleiben soll
• Nutzer, die sichtbare Navigationsbuttons statt Gesten bevorzugen

<b>Wichtigste Funktionen</b>

• Schwebende Buttons für Zurück, Startbildschirm und Letzte Apps in ausgewählten Apps
• Genaue Auswahl, welche installierten Apps das Overlay anzeigen
• Alle anderen Apps bleiben unverändert
• Freie Positionierung per Drag & Drop oder mit Feinjustierung
• Größe, Farbe, Transparenz und Theme für jeden Button einzeln anpassbar
• Optionaler Einbrennschutz für OLED-Displays
• Heller und dunkler Modus
• Schlanke Oberfläche, auf das Wesentliche konzentriert

<b>Nutzung des Bedienungshilfe-Dienstes</b>

NavXS verwendet die Android-Schnittstelle für Bedienungshilfen (Accessibility Service API). Sie ist für die Kernfunktion der App erforderlich, da Android normalen Apps ohne sie weder das Auslösen von System-Navigationsaktionen noch diese Art von Overlay erlaubt.

Der Bedienungshilfe-Dienst wird ausschließlich genutzt, um:

• zu erkennen, welche App gerade im Vordergrund ist, damit das Overlay nur in den ausgewählten Apps erscheint
• die schwebenden Navigationsbuttons über diesen Apps anzuzeigen
• Zurück, Startbildschirm oder Letzte Apps auszuführen, wenn du auf den entsprechenden Button tippst

Der Dienst hat technisch Zugriff auf Fensterinformationen der App im Vordergrund, NavXS verwendet davon jedoch ausschließlich den Paketnamen dieser App. NavXS liest keine eingegebenen Texte, Passwörter, Nachrichteninhalte oder sonstigen Bildschirminhalte, verfolgt kein Nutzerverhalten und führt keine Aktionen in deinem Namen aus – außer der Navigationsaktion des Buttons, den du antippst.

Bevor der Dienst angefordert wird, erklärt NavXS dessen Nutzung und fragt mit zwei Optionen nach deiner Einwilligung: „Ablehnen“ und „Zustimmen“. Erst nach Tippen auf „Zustimmen“ öffnet NavXS die Android-Einstellungen für Bedienungshilfen, in denen du den Dienst selbst aktivierst. Dort kannst du ihn jederzeit wieder deaktivieren.

<b>Zugriff auf installierte Apps</b>

NavXS benötigt Zugriff auf die Liste der installierten Apps, damit du auswählen kannst, in welchen Apps das Overlay angezeigt werden soll. Diese Liste wird ausschließlich für die App-Auswahl und die Konfiguration innerhalb von NavXS verwendet.

<b>Datenschutz</b>

• Die gesamte Verarbeitung erfolgt lokal auf deinem Gerät
• Kein Konto und keine personenbezogenen Daten erforderlich
• Es werden keine Daten erhoben, verkauft oder weitergegeben
• Keine Werbung, keine Analyse, kein Tracking
