# App-Icon-Vorschläge

## Wortmarke: unverändert
Die Wortmarke wird **nicht** nachgebaut oder neu gesetzt. Alle Vorschläge verwenden die
Original-Vektorpfade aus `mockups/NavXS-schriftzug.svg` – identische Glyphen, identisches
Kerning, ☰ auf seiner originalen vertikalen Position (mittig zur XS-Höhe).

- `navxs_wordmark_long_navxs.svg` – Langform **NavXS☰**
- `navxs_wordmark_short_nxs.svg` – Kurzform **NXS☰**
- `wm_check.png` – Kontrollrendering beider Formen

Die Kurzform entsteht ausschließlich durch Weglassen der Glyphen `a` und `v`; die Gruppe
`XS☰` rückt um 205 Einheiten nach links, sodass der Abstand N→X exakt dem Original-Abstand
N→a entspricht. Keine Glyphe wird skaliert, verzerrt oder verschoben.

Farben wie im Bestand: **N** `#FFFFFF`, **XS** und **☰** `#43C4EE`, Grund `#0E1116` / `#1B2027`.

## Warum ein neues Icon?
Das aktuelle `drawable-nodpi/ic_launcher_full_art.png` hat drei Probleme:
1. **Zu viel Detail** – der gestapelte Karten-/Panel-Look zerfällt bei 48 dp zu grauem Matsch.
2. **Wortmarke zu klein** – sie füllt nur ca. 40 % der Fläche, obwohl sie das einzige Erkennungsmerkmal ist.
3. **Keine Adaptive-Icon-Trennung** – Foreground/Background sind faktisch ein Bild, der Launcher kann nicht maskieren.

## Varianten (1024×1024)

| Datei | Idee | Stärke | Schwäche |
|---|---|---|---|
| `variant_a_wordmark.svg` | Kurzform NXS☰ groß und allein auf dunklem Verlauf | maximale Lesbarkeit, reinste Markenwirkung | erklärt die Funktion nicht |
| `variant_b_navbar.svg` | **Gewählt.** NXS☰ vertikal zentriert, dahinter BACK / HOME / RECENTS als grobe Handskizze – ohne Rahmen, ohne Füllung, vergrößert und von den Icon-Rändern angeschnitten | zeigt die drei festen Buttons, ohne der Wortmarke die Fläche zu nehmen | Formen sind bewusst nur angedeutet |
| `variant_c_overlay_pill.svg` | NXS☰ in einer schwebenden Overlay-Pill | transportiert „Overlay über anderen Apps" | Rahmen kostet Fläche, Wortmarke kleiner |

**Umgesetzt ist Variante B.** Die Wortmarke steht vertikal zentriert (900 px breit); die drei
Symbole liegen als Skizzenlinien dahinter: BACK-Dreieck oben links, RECENTS-Quadrat oben rechts,
HOME-Kreis unten – jeweils über den Rand hinaus, mit offenen Ecken, Überschwingern und einem
schwächeren zweiten Strich. Opazität 0.28–0.30 (Hauptzug) bzw. 0.12 (Beistrich).

## Vorschau
- `montage.png` – alle drei bei 512 px
- `montage48.png` – alle drei bei 48 px (Launcher-Realität)
- `preview_*.png`, `preview48_*.png` – einzeln

## Flächennutzung
Die 1024×1024 der SVGs sind die **sichtbare** Icon-Fläche, nicht das 108-dp-Adaptive-Canvas.
Der Inhalt reicht bewusst bis nahe an den Rand (Wortmarke ~86 % der Breite), damit er in den
Zielgrößen (48 dp im Launcher, 512 px im Play Store) noch lesbar ist – siehe `montage48.png`.

Konsequenz für das Adaptive Icon: dieses Bild darf **nicht** full-bleed als Foreground
verwendet werden, sonst schneidet die Launcher-Maske die ☰-Balken ab. Stattdessen wird es in
das mittlere Safe-Feld eines 1536×1536-Foregrounds gesetzt. Die Adaptive-Safe-Zone ist
systembedingt konservativ – wer dort dieselbe optische Größe will, muss Beschnitt in Kauf
nehmen; deshalb ist ein Legacy-Icon (`mipmap-*/ic_launcher.png`) ohne Maske hier klar im Vorteil.

## Umgesetzte Assets (Variante B)
| Datei | Inhalt |
|---|---|
| `mipmap-{m,h,xh,xxh,xxxh}dpi/ic_launcher.png` | Legacy, Squircle-Maske, 48–192 px |
| `mipmap-{...}/ic_launcher_round.png` | Legacy, Kreismaske |
| `mipmap-anydpi-v26/ic_launcher{,_round}.xml` | Adaptive Icon (background / foreground / monochrome) |
| `drawable-nodpi/ic_launcher_foreground_art.png` | 1536², Wortmarke 820 px (Safe-Zone), Skizze full-bleed |
| `drawable-nodpi/ic_launcher_background_art.png` | 1536², Verlauf `#232A33 → #0E1116`, full-bleed |
| `drawable-nodpi/ic_launcher_monochrome_art.png` | 1536², Wortmarke + Symbole weiß, ohne Glow (Themed Icons) |
| `drawable-nodpi/ic_launcher_full_art.png` | 1024², ungemaskt |
| `play_store_512.png` | Play-Store-App-Symbol, 512², PNG ohne Alpha |
| `play_feature_1024x500.png` | Play-Store-Feature-Grafik, 1024×500, PNG ohne Alpha (Quelle: `play_feature_1024x500.svg`) |
| `drawable-nodpi/splash_icon_art.png` | 1152², Splash-Icon: dunkler Kreis d=1024, Wortmarke 660 px in der Splash-Safe-Zone (768) |

`AndroidManifest.xml`: `android:icon` zeigt jetzt auf `@mipmap/ic_launcher`
(vorher fälschlich auf `ic_launcher_round`), `android:roundIcon` unverändert.

## Adaptive Icon: Skizze vs. Safe-Zone
Wortmarke und Skizze werden im Foreground **getrennt** skaliert. Die Wortmarke muss in die
Safe-Zone (820 px von 1536), die Skizze soll dagegen angeschnitten sein und deshalb über den
Canvas-Rand reichen (Faktor 1,5). Beides zusammen ginge sonst nicht.

Damit die Skizze durch diese Vergrößerung nicht die Wortmarke erschlägt, sind ihre
Strichstärken im Adaptive-Foreground auf 62 % reduziert – sie bleibt Hintergrundtextur.
Im Legacy-Icon (1024², keine Maske) gilt das Original-Verhältnis.

## Splash-Icon
`splash_icon_art.png` (Theme `Theme.NavXS.Starting`) ist auf dasselbe Motiv nachgezogen:
1152² nach Android-12-Spezifikation, dunkler Kreis mit d = 1024, Skizze am Kreisrand
angeschnitten, Wortmarke 660 px – innerhalb der 768er-Safe-Zone. Strichstärken der Skizze
hier auf 73 % (Verhältnis zur kleineren Wortmarke).

Der Splash-Hintergrund steht unverändert auf `@android:color/white` (auch im Night-Theme).

## Play Console
| Asset | Datei |
|---|---|
| App-Symbol (512×512) | `play_store_512.png` |
| Feature-Grafik (1024×500) | `play_feature_1024x500.png` |

Beide ohne Alphakanal und ohne eigene Eckenrundung – die macht der Store selbst.
Die Feature-Grafik nutzt die **Langform NavXS☰** (600 px breit, mittig), weil sie im
Querformat trägt; die Skizzensymbole sind links, rechts und unten angeschnitten. Wichtige
Inhalte liegen in der Mitte, da der Store die Grafik je nach Platzierung seitlich beschneidet.
