# Privacy Policy for NavXS

Last updated: September 14, 2026

## 1. General Information

NavXS is an Android app that can display its own navigation overlay with fixed buttons for selected apps.

The developer of this app only collects, stores, or processes personal data to the extent technically required for the app to function.

## 2. Local Processing on the Device

NavXS generally processes information locally on the device.

This includes in particular:

- the package name of the app currently in the foreground
- the selection of apps for which the overlay should be enabled
- the overlay settings defined by the user, such as positions, colors, sizes, transparency, and themes
- the status of whether the accessibility service is enabled
- the URI of an image that the user optionally selects as the background of the positioning editor, along with the opacity chosen for it

## 3. Purpose of Processing

This information is processed solely in order to:

- detect whether the overlay should be shown for the currently open app
- show or hide the overlay
- provide the configured navigation actions
- save and restore the settings selected by the user locally

## 4. Accessibility Service

NavXS uses the Android Accessibility Service API (AccessibilityService). NavXS is not an accessibility tool for people with disabilities; the service is required for the core feature of the app, the navigation overlay.

### 4.1 Consent

Before the accessibility service is requested, NavXS shows a disclosure explaining how the service is used. The user can choose between two buttons, "Decline" and "Agree". Only after tapping "Agree" does NavXS open the Android accessibility settings, where the user must enable the service manually. Tapping "Decline" closes the app without enabling anything. Leaving the disclosure in any other way is not treated as consent. The service can be disabled at any time in the Android accessibility settings.

### 4.2 What the service is used for

The service is used exclusively to:

- detect the package name of the app currently running in the foreground
- decide whether the overlay should be displayed for that app
- display the floating navigation buttons over the selected apps
- perform the navigation actions Back, Home and Recent Apps when the user taps the corresponding button

### 4.3 What the service does not do

The accessibility service technically has access to window information of the app in the foreground. NavXS uses this access only to determine the package name of that app. NavXS does not read, store, or transmit typed text, passwords, message content, or any other screen content. NavXS does not perform actions on behalf of the user other than the navigation actions mentioned above, and only when the user taps a button.

This service is not used for advertising, tracking, or analytics purposes, and the data is neither sold nor shared.

## 5. No Transfer to External Servers

At the current time, NavXS does not transmit selected apps, foreground app package names, or overlay settings to external servers or other external services.

Processing takes place locally on the device.

## 6. Data Storage

The settings and app selections stored by NavXS are saved locally on the device.

This data remains on the device until it is changed by the user, removed by clearing the app data, or deleted by uninstalling the app.

If the user selects a screenshot as the background of the positioning editor, NavXS stores only the URI of that image together with the read permission granted by the system. The image file itself is not copied, not saved in an app directory, not passed to the accessibility service, and not transmitted. Resetting the background deletes the stored URI and releases the read permission again.

## 7. Permissions

NavXS in particular requires the accessibility permission explicitly enabled by the user so that the app can provide its overlay functionality.

In addition, only such access is used as is technically necessary for operating the app.

## 8. Third Parties

At the current time, NavXS does not use third-party services, analytics tools, or advertising for processing the data described above.

## 9. Changes to This Privacy Policy

This privacy policy may be updated if necessary to reflect changes to the app or legal requirements.

## 10. Contact

If you have any questions about privacy, you can contact the developer.

Email: navxs@robnice.de
