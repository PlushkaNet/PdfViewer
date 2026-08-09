## Fork notice

This is a fork of [GrapheneOS PdfViewer](https://github.com/GrapheneOS/PdfViewer) with
reading-history improvements:

- A home screen with a scrollable list of recently opened files.
- The last read page of each file is remembered; tapping a file in the list
  reopens it on that page.
- Individual entries can be removed from the list.

Only metadata (file URI, display name and last page) is stored in the app's
private storage. PDF content is never copied or cached, and the app still
requires no permissions.

## Original README

Simple Android PDF viewer based on pdf.js and content providers. The app
doesn't require any permissions. The PDF stream is fed into the sandboxed
WebView without giving it access to the network, files, content providers or
any other data.

Content-Security-Policy is used to enforce that the JavaScript and styling
properties within the WebView are entirely static content from the APK assets
along with blocking custom fonts since pdf.js handles rendering those itself.

It reuses the hardened Chromium rendering stack while only exposing a tiny
subset of the attack surface compared to actual web content. The PDF rendering
code itself is memory safe with dynamic code evaluation disabled, and even if
an attacker did gain code execution by exploiting the underlying web rendering
engine, they're within the Chromium renderer sandbox with less access than it
would have within the browser.

## Tests

### Android instrumentation tests

Requires a connected device or running emulator.

```sh
./gradlew connectedAndroidTest
```

To run a single test:

```sh
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=app.grapheneos.pdfviewer.test.PdfViewerLaunchTest
```

### JavaScript unit tests

Requires Node.js 24+. Make sure modules are installed:

```sh
npm install
```

Run the tests:

```sh
npm test
```