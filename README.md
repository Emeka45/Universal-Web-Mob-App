# Universal Web Mob

Universal Web Mob is an Android desktop-style web workspace designed to make desktop-oriented websites and web applications practical on a phone.

## Engine architecture

Universal Web Mob uses **Mozilla GeckoView** as its embedded web engine. The application does **not** embed Chromium and does not use Android WebView.

GeckoView is a standalone Gecko engine designed for browser applications. The app uses Gecko's native desktop user-agent mode and desktop viewport mode rather than pretending to be a desktop browser by changing a WebView string.

## Current foundation

- Mozilla GeckoView rendering engine
- Desktop user-agent mode
- Desktop viewport mode
- JavaScript-enabled web applications
- Persistent browser session/runtime
- Back / forward / reload navigation
- URL and web search bar
- Find in page
- Share current page
- External download handoff
- Clear browsing data
- Fullscreen web content
- Supported-browser authentication fallback
- Dark U-branded desktop-style interface
- Android 8.0+ baseline

## Authentication compatibility

Some identity providers and security systems can reject embedded application browsers. Universal Web Mob therefore provides an **Open in supported browser** action.

That path is intentionally a compatibility fallback. The application does not attempt to defeat Cloudflare, CAPTCHA, OAuth restrictions, anti-bot checks, DRM, or other website security controls.

The external browser may maintain its own cookies and session state; Universal Web Mob does not claim that cookies from another browser will automatically transfer back into GeckoView.

## Product direction

Future releases can build on GeckoSession and GeckoView for:

- persistent multi-tab workspace
- tab cards and tab groups
- private tabs
- session restore
- desktop-style pointer/touchpad controls
- richer file upload/download workflows
- bookmarks and history
- site profiles
- permissions controls
- page printing and PDF workflows
- performance controls

All browser features must remain inside normal Android and website security boundaries.

## Build

Open in Android Studio and run:

    ./gradlew assembleDebug

Package: `com.coeric.universalwebmob`

GitHub Actions verifies builds on pushes to `main`.


## Size optimization
The build uses ABI-split APKs for `arm64-v8a` and `armeabi-v7a`, plus R8/resource shrinking for release builds and an optimized Android App Bundle for store delivery.
