# NavXS

A lightweight Android app that places a floating navigation overlay — **Back**, **Home**, and **Recents** buttons — on top of any app you choose. Ideal for devices where gesture navigation conflicts with a particular app, or when you simply want persistent, fully customisable soft keys exactly where you need them.

<p>
  <img src="docs/settings_light.png" width="270" alt="Settings – light mode">
  &nbsp;&nbsp;&nbsp;
  <img src="docs/settings_dark.png" width="270" alt="Settings – dark mode">
</p>

**How to get started**

1. Launch NavXS and grant the accessibility service permission.
2. In the **Apps** tab, enable the apps that should display the overlay.
3. In the **Settings** tab, customise each button's appearance and position.
4. Open any enabled app — the overlay appears automatically and hides when you leave.

---

## Contents

1. [Installation](#1-installation)
2. [Accessibility Service](#2-accessibility-service)
3. [Apps](#3-apps)
4. [Settings](#4-settings)
   - [4.1 Position buttons](#41-position-buttons)
   - [4.2 Select a button](#42-select-a-button)
   - [4.3 Active](#43-active)
   - [4.4 Button](#44-button)
   - [4.5 Button background](#45-button-background)
5. [Preview flag](#5-preview-flag)
6. [License](#6-license)

---

## 1 Installation

NavXS is available on Google Play:

<a href="https://play.google.com/store/apps/details?id=de.robnice.navxs">
  <img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" height="80" alt="Get it on Google Play">
</a>

Alternatively, signed APKs are available on the [Releases](https://github.com/robNice/NavXS/releases) page. To install an APK manually, allow installation from unknown sources for your browser or file manager when Android asks.

---

## 2 Accessibility Service

NavXS uses Android's Accessibility Service to detect which app is currently in the foreground. This is the only mechanism Android provides for an app to know what is running at any given moment — it is required for the overlay to know when to show and when to hide itself.

<p align="center">
  <img src="docs/accessibility-service-gate.png" width="270" alt="Accessibility service required">
</p>

On first launch, this screen appears if the service is not yet enabled. Tap **Open accessibility settings**, locate NavXS in the list, and toggle it on. Return to the app and tap **Check again** to continue.

**What the service does — and does not do**

| | |
|---|---|
| ✅ | Reads the **package name** of the foreground app to decide whether to show the overlay. |
| ❌ | Does **not** read text, content, or UI elements of other apps. Window access (`canRetrieveWindowContent="true"`) is used solely to determine the package name of the foreground app. |
| ❌ | Does **not** transmit any data. All processing stays on your device. |

The permission can be revoked at any time in **Android Settings → Accessibility → NavXS**.

---

## 3 Apps

<p>
  <img src="docs/apps_light.png" width="270" alt="Apps tab – light mode">
  &nbsp;&nbsp;&nbsp;
  <img src="docs/apps_dark.png" width="270" alt="Apps tab – dark mode">
</p>

The list shows all installed apps on your device. Enable an app to make the NavXS overlay appear whenever it is in the foreground. Use the search bar to filter by name, or toggle **Show system apps** to include system apps.

Enabled apps appear at the top of the list for quick access. Disabling an app removes the overlay the next time that app comes to the foreground.

---

## 4 Settings

All settings apply to the currently selected button only. The three buttons — Back, Home, and Recents — are configured independently of one another.

<p>
  <img src="docs/settings_light.png" width="270" alt="Settings tab – light mode">
  &nbsp;&nbsp;&nbsp;
  <img src="docs/settings_dark.png" width="270" alt="Settings tab – dark mode">
</p>

### 4.1 Position buttons

Opens a full-screen editor where you drag buttons to the exact position they should occupy in the live overlay. Tap a button to select it without moving it. The reset icon (↺) restores all default positions. The precision panel moves the selected button by an exact pixel step size. The image icon opens the positioning background dialog: pick any screenshot from your gallery as the editor backdrop to align the buttons with a real app screen, or reset it back to the dotted default. Once an image is set, a slider appears in the same dialog and fades the screenshot so the buttons stay easy to see while you drag them. The dialog stays open while you pick an image, so you can adjust the slider right away. Only the image URI is stored locally, never a copy of the file, and the screenshot is never shown in the live overlay.

<p>
  <img src="docs/button_positioning_light.png" width="270" alt="Position mode – light mode">
  &nbsp;&nbsp;&nbsp;
  <img src="docs/button_positioning_dark.png" width="270" alt="Position mode – dark mode">
</p>

The **Precision positioning** panel offers step sizes of 1 px, 5 px, and 10 px and displays the exact pixel coordinates of the selected button in real time.

### 4.2 Select a button

Tap **Back**, **Home**, or **Recents** at the top of the settings screen to pick a button. All settings below apply to the selected button only.

### 4.3 Active

Switches the selected button on or off in the overlay. At least one button must remain active at all times.

### 4.4 Button

| Setting | Description |
|---------|-------------|
| **Theme** | Chooses the icon style. Multiple design variants are available per button. |
| **Colour** | Sets the icon colour. |
| **Opacity** | Icon transparency — 0 invisible, 100 fully opaque. |
| **Size** | Scales the icon relative to its default size. |

### 4.5 Button background

An optional circle drawn behind the icon to improve visibility against any app background.

| Setting | Description |
|---------|-------------|
| **Colour** | Fill colour of the background circle. |
| **Opacity** | Transparency of the background circle. |
| **Size** | Diameter of the circle relative to the icon. |
| **Softness** | 0 = hard edge; 100 = fully faded gradient edge. |

---

## 5 Preview flag

The small flag at the top-right of the settings screen slides open a side panel showing the selected button rendered on white, grey, and black backgrounds. Use it to judge how the button will look on any app background without leaving the settings screen.

<p>
  <img src="docs/preview_light.png" width="380" alt="Preview panel – light mode">
</p>
<p>
  <img src="docs/preview_dark.png" width="380" alt="Preview panel – dark mode">
</p>

---

## 6 License

NavXS is released under the [MIT License](LICENSE.md).
