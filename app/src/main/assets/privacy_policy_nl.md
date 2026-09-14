# Privacybeleid van NavXS

Laatste update: 14 september 2026

## 1. Algemene informatie

NavXS is een Android-app die een navigatie-overlay met vaste knoppen kan weergeven voor geselecteerde apps.

De ontwikkelaar van deze app verzamelt, slaat op of verwerkt persoonsgegevens alleen voor zover technisch noodzakelijk voor het functioneren van de app.

## 2. Lokale verwerking op het apparaat

NavXS verwerkt informatie doorgaans lokaal op het apparaat.

Dit omvat in het bijzonder:

- de pakketnaam van de app die momenteel op de voorgrond actief is
- de selectie van apps waarvoor de overlay moet worden ingeschakeld
- de door de gebruiker ingestelde overlay-instellingen, zoals posities, kleuren, formaten, transparantie en thema's
- de status of de toegankelijkheidsservice is ingeschakeld
- de URI van een afbeelding die de gebruiker optioneel als achtergrond van de positioneringseditor kiest, samen met de daarvoor gekozen dekking

## 3. Doel van de verwerking

Deze informatie wordt uitsluitend verwerkt om:

- te detecteren of de overlay moet worden getoond voor de momenteel geopende app
- de overlay in of uit te schakelen
- de geconfigureerde navigatieacties te bieden
- de door de gebruiker gekozen instellingen lokaal op te slaan en te herstellen

## 4. Toegankelijkheidsservice

NavXS gebruikt de Android-toegankelijkheidsservice-API (AccessibilityService). NavXS is geen toegankelijkheidstool voor mensen met een beperking; de service is nodig voor de kernfunctie van de app, de navigatie-overlay.

### 4.1 Toestemming

Voordat de toegankelijkheidsservice wordt aangevraagd, toont NavXS een melding die uitlegt hoe de service wordt gebruikt. De gebruiker kiest tussen twee knoppen: 'Weigeren' en 'Akkoord'. Pas nadat op 'Akkoord' is getikt, opent NavXS de Android-instellingen voor toegankelijkheid, waar de gebruiker de service zelf moet inschakelen. 'Weigeren' sluit de app zonder iets in te schakelen. Het verlaten van de melding op een andere manier geldt niet als toestemming. De service kan op elk moment worden uitgeschakeld in de Android-instellingen voor toegankelijkheid.

### 4.2 Waarvoor de service wordt gebruikt

De service wordt uitsluitend gebruikt om:

- de pakketnaam te herkennen van de app die momenteel op de voorgrond draait
- te bepalen of de overlay voor die app moet worden weergegeven
- de zwevende navigatieknoppen boven de geselecteerde apps weer te geven
- de navigatieacties Terug, Home en Recente apps uit te voeren wanneer de gebruiker op de betreffende knop tikt

### 4.3 Wat de service niet doet

De toegankelijkheidsservice heeft technisch toegang tot vensterinformatie van de app op de voorgrond. NavXS gebruikt deze toegang uitsluitend om de pakketnaam van die app te bepalen. NavXS leest, bewaart of verzendt geen getypte tekst, wachtwoorden, berichtinhoud of andere scherminhoud. NavXS voert geen acties uit namens de gebruiker, behalve de hierboven genoemde navigatieacties, en alleen wanneer de gebruiker op een knop tikt.

Deze service wordt niet gebruikt voor reclame-, tracking- of analysedoeleinden, en de gegevens worden niet verkocht of gedeeld.

## 5. Geen overdracht naar externe servers

Momenteel stuurt NavXS geen geselecteerde apps, pakketnamen van voorgrondapps of overlay-instellingen naar externe servers of andere externe diensten.

De verwerking vindt lokaal op het apparaat plaats.

## 6. Gegevensopslag

De instellingen en app-selecties die door NavXS worden opgeslagen, worden lokaal op het apparaat bewaard.

Deze gegevens blijven op het apparaat totdat ze door de gebruiker worden gewijzigd, worden verwijderd door de app-gegevens te wissen, of worden gewist bij het verwijderen van de app.

Als de gebruiker een schermafbeelding als achtergrond van de positioneringseditor kiest, slaat NavXS alleen de URI van die afbeelding op, samen met het door het systeem verleende leesrecht. Het afbeeldingsbestand zelf wordt niet gekopieerd, niet in een app-map opgeslagen, niet aan de toegankelijkheidsservice doorgegeven en niet verzonden. Bij het herstellen van de achtergrond wordt de opgeslagen URI verwijderd en het leesrecht weer vrijgegeven.

## 7. Machtigingen

NavXS vereist in het bijzonder de toegankelijkheidsmachtiging die expliciet door de gebruiker is ingeschakeld, zodat de app zijn overlay-functionaliteit kan bieden.

Daarnaast wordt alleen toegang gebruikt die technisch noodzakelijk is voor het gebruik van de app.

## 8. Derde partijen

Momenteel maakt NavXS geen gebruik van diensten van derden, analysehulpmiddelen of reclame voor de verwerking van de hierboven beschreven gegevens.

## 9. Wijzigingen in dit privacybeleid

Dit privacybeleid kan indien nodig worden bijgewerkt om wijzigingen in de app of wettelijke vereisten te weerspiegelen.

## 10. Contact

Als u vragen heeft over privacy, kunt u contact opnemen met de ontwikkelaar.

E-mail: navxs@robnice.de
