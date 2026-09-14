# Datenschutzbestimmung für NavXS

Stand: 14. September 2026

## 1. Allgemeines

NavXS ist eine Android-App, die für ausgewählte Apps ein eigenes Navigations-Overlay mit festen Schaltflächen anzeigen kann.

Der Entwickler dieser App erhebt, speichert oder verarbeitet personenbezogene Daten nur insoweit, wie dies technisch für die Funktion der App erforderlich ist.

## 2. Lokale Verarbeitung auf dem Gerät

NavXS verarbeitet Informationen grundsätzlich lokal auf dem Gerät.

Dazu gehören insbesondere:

- der Paketname der aktuell im Vordergrund befindlichen App
- die Auswahl der Apps, für die das Overlay aktiviert werden soll
- die vom Nutzer festgelegten Overlay-Einstellungen, zum Beispiel Positionen, Farben, Größen, Transparenz und Themes
- der Status, ob der Bedienungshilfe-Dienst aktiviert ist
- die URI eines Bildes, das der Nutzer optional als Hintergrund des Positionierungs-Editors auswählt, samt der dafür gewählten Deckkraft

## 3. Zweck der Verarbeitung

Die Verarbeitung dieser Informationen erfolgt ausschließlich, um:

- zu erkennen, ob das Overlay für die aktuell geöffnete App angezeigt werden soll
- das Overlay ein- oder auszublenden
- die konfigurierten Navigationsaktionen bereitzustellen
- die vom Nutzer gewählten Einstellungen lokal zu speichern und wiederherzustellen

## 4. Bedienungshilfe-Dienst

NavXS verwendet die Android-Schnittstelle für Bedienungshilfen (AccessibilityService API). NavXS ist keine Bedienungshilfe für Menschen mit Behinderungen; der Dienst wird für die Kernfunktion der App, das Navigations-Overlay, benötigt.

### 4.1 Einwilligung

Bevor der Bedienungshilfe-Dienst angefordert wird, zeigt NavXS einen Hinweis, der die Nutzung des Dienstes erklärt. Der Nutzer wählt zwischen zwei Schaltflächen: „Ablehnen“ und „Zustimmen“. Erst nach Tippen auf „Zustimmen“ öffnet NavXS die Android-Einstellungen für Bedienungshilfen, in denen der Nutzer den Dienst selbst aktivieren muss. „Ablehnen“ schließt die App, ohne etwas zu aktivieren. Ein Verlassen des Hinweises auf andere Weise gilt nicht als Einwilligung. Der Dienst kann jederzeit in den Android-Einstellungen für Bedienungshilfen deaktiviert werden.

### 4.2 Wofür der Dienst genutzt wird

Der Dienst wird ausschließlich dafür genutzt,

- den Paketnamen der aktuell im Vordergrund laufenden App zu erkennen
- zu entscheiden, ob das Overlay für diese App eingeblendet werden soll
- die schwebenden Navigationsschaltflächen über den ausgewählten Apps anzuzeigen
- die Navigationsaktionen Zurück, Startbildschirm und Letzte Apps auszuführen, wenn der Nutzer auf die entsprechende Schaltfläche tippt

### 4.3 Was der Dienst nicht tut

Der Bedienungshilfe-Dienst hat technisch Zugriff auf Fensterinformationen der App im Vordergrund. NavXS nutzt diesen Zugriff ausschließlich, um den Paketnamen dieser App zu ermitteln. NavXS liest, speichert oder überträgt keine eingegebenen Texte, Passwörter, Nachrichteninhalte oder sonstigen Bildschirminhalte. NavXS führt keine Aktionen im Namen des Nutzers aus, außer den oben genannten Navigationsaktionen, und das nur, wenn der Nutzer auf eine Schaltfläche tippt.

Es erfolgt keine Nutzung dieses Dienstes zu Werbe-, Tracking- oder Analysezwecken, und die Daten werden weder verkauft noch weitergegeben.

## 5. Keine Übermittlung an externe Server

NavXS übermittelt nach aktuellem Stand keine ausgewählten Apps, keine Paketnamen von Vordergrund-Apps und keine Overlay-Einstellungen an externe Server oder sonstige externe Dienste.

Die Verarbeitung erfolgt lokal auf dem Gerät.

## 6. Datenspeicherung

Die von NavXS gespeicherten Einstellungen und App-Auswahlen werden lokal auf dem Gerät gespeichert.

Diese Daten verbleiben auf dem Gerät, bis sie durch den Nutzer geändert, durch das Löschen der App-Daten entfernt oder durch Deinstallation der App gelöscht werden.

Wählt der Nutzer einen Screenshot als Hintergrund des Positionierungs-Editors, speichert NavXS ausschließlich die URI dieses Bildes zusammen mit dem vom System erteilten Leserecht. Die Bilddatei selbst wird nicht kopiert, nicht in einem App-Verzeichnis abgelegt, nicht an den Bedienungshilfe-Dienst übergeben und nicht übertragen. Beim Zurücksetzen des Hintergrunds werden die gespeicherte URI gelöscht und das Leserecht wieder freigegeben.

## 7. Berechtigungen

NavXS benötigt insbesondere die vom Nutzer ausdrücklich aktivierte Bedienungshilfe-Berechtigung, damit die App ihre Overlay-Funktion bereitstellen kann.

Darüber hinaus werden nur solche Zugriffe verwendet, die technisch für den Betrieb der App erforderlich sind.

## 8. Drittanbieter

NavXS verwendet nach aktuellem Stand keine Drittanbieter-Dienste, keine Analyse-Tools und keine Werbung zur Verarbeitung der oben beschriebenen Daten.

## 9. Änderungen dieser Datenschutzbestimmung

Diese Datenschutzbestimmung kann bei Bedarf aktualisiert werden, um Änderungen der App oder rechtliche Anforderungen abzubilden.

## 10. Kontakt

Bei Fragen zum Datenschutz können Sie den Entwickler kontaktieren.

E-Mail: navxs@robnice.de
