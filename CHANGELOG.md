# Changelog

All notable changes to NavXS are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2026-08-30

### Added

- **Positioning background.** The full-screen positioning editor has a fourth icon in the
  top bar that opens a "Positioning background" dialog. Pick any screenshot from your
  gallery as the editor backdrop and place BACK, HOME, and RECENTS directly on the
  interface of the app you actually use, instead of guessing on an empty dotted grid.
- **Background opacity slider.** Once an image is set, a slider in the same dialog fades
  the screenshot (10–100 %) so the buttons stay easy to see while you drag them. The value
  is applied live and stored across restarts.
- **Reset background.** Removes the screenshot and restores the dotted default. Your
  button positions are never touched by it.
- Help section 2.1.1 "Positioning background" explains what the feature is for.

### Changed

- The selected background survives leaving the editor as well as app and device restarts.
  Only the image URI and the chosen opacity are stored in DataStore, together with the
  persistable read permission the system grants for that one picture.
- The dialog stays open while you pick an image, so the opacity slider can be adjusted
  right away.
- Privacy policy updated in all 13 languages — in-app and in the repository — to cover the
  stored image URI and opacity.

### Fixed

- Completed the missing translations for the accessibility disclosure screen, the support
  help section, and the app rescan states in 11 languages (es, fr, it, ja, ko, nl, pl, pt,
  ru, tr, uk). `lintDebug` now passes without `MissingTranslation` errors.
- Repaired two outdated instrumentation tests that no longer matched the current UI.

### Security & privacy

- No new permission and no new dependency. The Android photo picker grants access to the
  single picked image only, so neither `READ_MEDIA_IMAGES` nor storage access is requested.
- The screenshot file itself is never copied, never written to an app directory, never
  passed to the accessibility service, and never transmitted.
- The persistable read permission is released when the background is reset, and when an
  image is replaced it is released only after the new one has been taken over successfully.
- An image that becomes unreadable falls back to the default backdrop, drops the stale URI,
  and reports a localized error instead of crashing.
- The background is a setup aid only — it is never rendered in the live overlay.

[1.1.0]: https://github.com/robNice/NavXS/releases/tag/v1.1.0
