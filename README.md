# Bil'Clubs

A platform for Bilkent University students to discover, join, and manage student clubs and events. Built as a client-server Java application with a JavaFX desktop UI and a lightweight HTTP backend.

---

## Table of Contents

- [Project Structure](#project-structure)
- [Architecture Overview](#architecture-overview)
- [Dependencies](#dependencies)
- [Prerequisites](#prerequisites)
- [Environment Setup](#environment-setup)
- [Building and Running](#building-and-running)
- [Utility Scripts](#utility-scripts)
- [Features](#features)
- [API Reference](#api-reference)

---

## Project Structure

```
BilClubs/
├── server/                    # Java HTTP backend
│   ├── *.java                 # Server source files
│   ├── embeddings/            # Gemini API embedding support
│   ├── templates/             # HTML email templates
│   ├── lib/                   # Backend JAR dependencies
│   └── db/                    # ObjectDB database files (auto-created)
├── ui/                        # JavaFX desktop client
│   ├── src/bilclubs/
│   │   ├── App.java           # Application entry point
│   │   ├── controllers/       # FXML scene controllers
│   │   ├── components/        # Reusable UI components
│   │   └── utils/             # HTTP client, config, helpers
│   ├── resources/
│   │   ├── fxml/              # Scene layout files
│   │   ├── styles/            # CSS stylesheets
│   │   └── assets/            # Images and icons
│   ├── lib/                   # Client JAR dependencies
│   └── build-scripts/         # Automated build scripts
├── tests/                     # Integration test suite
│   ├── BilClubsTestSuite.java
│   ├── SyntheticDatabaseTest.java
│   └── *.java                 # Per-feature test tabs
├── run.bat                    # Start server only
├── run-unified.bat            # Start server + build and launch UI
├── run-tests.bat              # Run integration test suite
├── clean.bat                  # Remove compiled class files and logs
├── clean-db.bat               # Wipe the database
├── kill-server.bat            # Force-stop a running server
├── inspect-database.bat       # Open ObjectDB Explorer GUI
├── populate-synthetic-database.bat  # Seed the DB with synthetic data
├── set-api-key.bat            # Save Gemini API key to environment
├── set-password.bat           # Save SMTP credentials to environment
└── API.md                     # Full HTTP API documentation
```

---

## Architecture Overview

| Layer | Technology | Notes |
| :--- | :--- | :--- |
| Backend | Java (JDK 11+), `com.sun.net.httpserver` | Listens on port `5000` |
| Database | ObjectDB 2.9.5 (embedded JPA) | Stored in `server/db/` |
| Frontend | JavaFX (desktop GUI) | Built via auto-detecting script |
| AI / Search | Google Gemini API | Text embeddings |
| Email | JavaMail 1.6.2 via SMTP | Transactional notifications |
| Auth | Bilkent WebMail credential verification | No passwords stored |

The server exposes a JSON-over-HTTP API (`POST /api/*`) and also serves static files. The JavaFX client communicates with the server exclusively through this API.

---

## Dependencies

All required JARs are bundled in `server/lib/` and `ui/lib/`. No build tool (Maven/Gradle) is needed.

| Library | Version | Purpose |
| :--- | :--- | :--- |
| ObjectDB | 2.9.5 | Embedded JPA object database |
| JSON (org.json) | 20251224 | JSON serialization/parsing |
| JavaMail | 1.6.2 | SMTP email dispatch |
| javax.activation | 1.2.0 | JavaMail runtime dependency |
| javax.persistence-api | 2.2 | JPA annotations |
| javax.transaction-api | 1.3 | JPA transaction support |
| JavaFX SDK | 17+ (external) | Desktop GUI framework — **must be installed separately** |

---

## Prerequisites

1. **JDK 11 or later**
Download from Adoptium or Oracle. Verify with:
   ```
   java -version
   javac -version
   ```

2. **JavaFX SDK 17 or later** 
The build script auto-detects the SDK by searching common locations. If detection fails, set the environment variable:
   ```bat
   setx JAVAFX_HOME "C:\path\to\javafx-sdk-XX"
   ```
   Detection order: `JAVAFX_HOME` env var → entries on `PATH` → common filesystem paths (`Desktop`, `Downloads`, `Program Files`, etc.) → Scoop/Chocolatey trees → top-level folders on `C:\` and `D:\`.

3. **Google Gemini API key**
Required for AI-powered semantic search. Obtain one from [Google AI Studio](https://aistudio.google.com/).

4. **SMTP account**
Required for email notifications (welcome emails, event/club alerts). A Gmail account with an App Password works.

---

## Environment Setup

Run these scripts once before starting the server. Each script saves its values as persistent Windows environment variables (via `setx`). **Restart your terminal after running them.**

### Set Gemini API Key
```bat
set-api-key.bat
```
Prompts for your key and saves it as `GEMINI_API_KEY`. Required for semantic search and recommendation features.

### Set SMTP Credentials
```bat
set-password.bat
```
Prompts for an email address and SMTP password, saved as `SMTP_EMAIL` and `SMTP_PASSWORD`. Required for sending transactional emails (welcome messages, event registration confirmations, etc.).

> **Note:** If either environment variable is missing, the corresponding feature degrades gracefully, the server still starts, but AI based recommendations are not shown and email notifications are silently skipped.

---

## Building and Running

### Option 1 — Server only
Compiles and starts the HTTP server on port `5000`. Use this when the UI will be launched separately or when developing against the API directly.
```bat
run.bat
```
This script automatically runs `clean.bat` and `kill-server.bat` first, then compiles all `.java` files in `server/` and starts `BilClubsServer`.

### Option 2 — Server + UI (recommended)
Starts the server in a background window, waits 3 seconds for it to initialize, then compiles and launches the JavaFX UI.
```bat
run-unified.bat
```

### Option 3 — Run tests
Compiles the server, starts it in the background, compiles the test suite, runs all tests, then shuts everything down and cleans up.
```bat
run-tests.bat
```

### Populate with synthetic data
Seeds the database with a realistic set of clubs, events, and users for development or demonstration purposes. Requires the server to be stopped before running (the script starts it internally).
```bat
populate-synthetic-database.bat
```
You will be prompted for an admin account's email and password.

---

## Utility Scripts

| Script | Purpose |
| :--- | :--- |
| `clean.bat` | Deletes all compiled `.class` files and server log files |
| `clean-db.bat` | Wipes all files in `server/db/` — **deletes all data** |
| `kill-server.bat` | Force-terminates any running `BilClubsServer` process |
| `inspect-database.bat` | Opens the ObjectDB Explorer GUI against `server/db/bilclubs.odb` |
| `run-swing-tester.bat` | Launches a lightweight Swing-based manual test harness |
| `run-drop-db.bat` | Drops and recreates the database, then starts the server |

---

## Features

### General

- **Bilkent WebMail authentication** — Accounts are created by verifying credentials against the university WebMail service. No Bil'Clubs-specific passwords are stored.
- **Role-based privilege system** — Four privilege levels (`NORMAL_USER`, `MODERATOR`, `MANAGER`, `ADMIN`) plus a `BANNED_USER` state, stored as bitflags per user and per club.
- **Club management** — Create clubs, customize name/description/icon/cover image, manage members and their roles.
- **Event management** — Create events with title, description, location, start/end times, and optional attendance quota. Events can award GE250 points.
- **Discussion boards** — Threaded comment sections on both clubs and events.
- **Media uploads** — Profile pictures, club icons, club covers, and event posters can be uploaded as images (up to 16 MB per request).
- **GE250 point tracking** — Users accumulate GE250 points by attending designated events; totals are visible on profiles.
- **Interest-based onboarding** — New users select interest keywords which are used to personalize club and event recommendations.
- **AI-powered semantic search** — Club and event descriptions are embedded via the Gemini API (`text-embedding-004`, 768 dimensions) using cosine similarity for semantic ranking alongside keyword search.
- **Email notifications** — Transactional HTML emails are dispatched asynchronously for: account creation, joining/leaving a club, registering/leaving an event, and event creation.
- **Follow system** — Users can follow other users to see their activity.
- **Calendar view** — Registered events are displayed in a monthly calendar in the UI.
- **Admin panel** — Admins can manage (ban/unban) users, clubs, and events from a dedicated management page.
- **Synthetic database seeder** — `SyntheticDatabaseTest` populates the database with realistic English-language club and event data for development.

### UI-Specific (JavaFX)

- **Scene-based navigation** with back-button support via a scene history stack.
- **Loading indicators** — An animated loading GIF is displayed during server requests to prevent UI freezing.
- **Dark / Light theme toggle** — Managed by `ThemeManager`; persists across scenes via a CSS stylesheet swap.
- **Custom reusable components** — `ClubPane`, `EventPane`, `SearchResultPane`, `NotificationCard`, `MemberCard`, `BanClubCard`, `BanEventCard`, `DetailedEventCard`.
- **Notification panel** — In-app alerts for club and event activity, dismissable per notification type.
- **Profile pages** — Distinct views for your own profile, other members' profiles, club managers, and admins.
- **Club creation / event creation forms** — Multi-field FXML forms with live validation feedback.
- **Interest keywords selection** — Scrollable tag-based UI for selecting interests during and after onboarding.
- **Settings page** — Users can update their profile picture, biography, major, name, email, and notification preferences.

### Security-Specific

- **No password storage** — Authentication is delegated entirely to Bilkent WebMail; the server never stores or sees a user's university password after the initial verification request.
- **Session tokens** — Each login generates a 32-character cryptographically random token (via `java.security.SecureRandom`) with a 24-hour TTL. Tokens are stored as `<token>:<expiry_epoch_ms>` and are invalidated on every new login.
- **Request size cap** — All API requests are rejected at 16 MB (`ServerConfig.MAX_REQUEST_BYTES`) to prevent memory exhaustion.
- **Input sanitization** — The `Sanitizer` class escapes control characters and strips potential injection sequences (e.g. `<`, `>`, ` AND `, ` OR `, ` WHERE `) from user-supplied strings before they are persisted.
- **Privilege enforcement** — Every protected endpoint checks the caller's `userId`/`sessionToken` pair and validates the required privilege level before executing.
- **ObjectDB isolation** — The database is embedded and file-local; it is not exposed on any network port.

> **Known limitations / TODOs noted in source:**
> - A rate limiter is not yet implemented (`// TODO: we need a rate limiter` in `BilClubsServer.java`).
> - The HTML sanitizer for XSS prevention is scaffolded but not yet actively applied (`// TODO: Implement HTML sanitizer` in `Sanitizer.java`).
> - WebMail login verification relies on an HTTP redirect response code; a stricter cookie-based check is noted as a FIXME in `LoginVerifier.java`.

---

## API Reference

See [API.md](API.md) for the full HTTP API documentation, including all endpoints, request/response shapes, authentication requirements, and error codes.

**Quick reference:**
- Base URL: `http://localhost:5000`
- All API calls use `POST` with `Content-Type: application/json`
- Protected endpoints require `userId` (integer) and `sessionToken` (string) in the request body
- All responses use a standard envelope: `{ "responseCode", "success", "data", "error" }`
