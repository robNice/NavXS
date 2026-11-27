NavXS Navigation Bar Buttons

NavXS adds customizable on-screen navigation buttons to selected Android apps.

Some apps hide the system navigation bar, use fullscreen layouts, or make standard navigation controls hard to reach. NavXS helps by placing a small, configurable navigation overlay on top of the apps you choose.

Unlike a global navigation overlay, NavXS appears only where you actually need it. You decide in which installed apps the buttons are shown – all other apps stay unchanged.

NavXS provides three floating buttons – <b>Back</b>, <b>Home</b> and <b>Recent Apps</b>. Position them where they are easiest to reach and adjust their appearance to your preference.

<b>Typical use cases</b>

• Apps that hide the Android navigation bar
• Apps that use fullscreen mode or immersive layouts
• Devices where navigation gestures are unreliable or inconvenient
• Large phones where reaching system navigation is uncomfortable
• Tablets or mounted devices used in fixed positions
• Kiosk-style setups where navigation should remain accessible
• Users who prefer visible navigation buttons instead of gestures

<b>Key features</b>

• Floating Back, Home and Recent Apps buttons for selected apps
• Choose exactly which installed apps show the overlay
• All other apps stay unchanged
• Free positioning by drag & drop or precise controls
• Customize size, colour, transparency and theme of each button
• Optional burn-in protection for OLED screens
• Light and dark mode
• Lightweight interface focused on practical control

<b>Accessibility service usage</b>

NavXS uses the Android Accessibility Service API. This is required for the core feature of the app, because Android does not allow normal apps to trigger system navigation actions or to display this kind of overlay without it.

The accessibility service is used only to:

• detect which app is currently in the foreground, so the overlay appears only in the apps you selected
• display the floating navigation buttons over those apps
• perform Back, Home or Recent Apps when you tap the corresponding button

The service technically has access to window information of the foreground app, but NavXS only uses the package name of that app. NavXS does not read typed text, passwords, message content or any other screen content, does not track user behavior, and never performs actions on your behalf other than the navigation action of the button you tapped.

Before the service is requested, NavXS explains how it is used and asks for your consent with two options: "Decline" and "Agree". Only after you tap "Agree" does NavXS open the Android accessibility settings, where you enable the service yourself. You can disable it there at any time.

<b>Installed apps permission</b>

NavXS needs access to the list of installed apps so you can choose in which apps the overlay should be shown. This is used only for app selection and configuration inside NavXS.

<b>Privacy</b>

• All processing happens locally on your device
• No account and no personal data required
• No data is collected, sold or shared
• No ads, no analytics, no tracking
