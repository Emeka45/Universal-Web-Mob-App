# Universal Web Mob

Universal Web Mob is an Android desktop-style web workspace designed to make desktop-oriented websites and web applications practical on a phone.

## Current foundation
- Desktop browser identity by default
- Wide viewport and desktop rendering
- JavaScript and DOM storage
- Cookies and third-party cookies
- URL/search bar
- Back/forward/reload controls
- In-page find
- Share page
- Download handoff
- Clear site data
- Dark desktop-style interface
- Low-dependency native Android architecture

## Product direction
The app is being built as more than a "desktop site" switch. Planned releases include a real tab manager, session restore, private tabs, touchpad/mouse interaction, desktop-like file workflows, fullscreen workspace, site profiles, bookmarks/history, downloads, permissions controls, connection-friendly authentication flows and performance tools.

The app must respect Android and website security boundaries and must not bypass authentication, DRM, access controls or platform restrictions.

## Build
Open in Android Studio and run:

    ./gradlew assembleDebug

Package: `com.coeric.universalwebmob`


Build status is verified by the repository's GitHub Actions workflow on pushes to `main`.
