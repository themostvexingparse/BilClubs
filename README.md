# Bil'Clubs

A platform for Bilkent University students to discover, join and manage student clubs and events. Built as a client-server Java application with a JavaFX desktop UI and a lightweight HTTP backend.

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
| JavaFX SDK | 17+ (external) | Desktop GUI framework: **must be installed separately** |

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

### Option 1: Server only
Compiles and starts the HTTP server on port `5000`. Use this when the UI will be launched separately or when developing against the API directly.
```bat
run.bat
```
This script automatically runs `clean.bat` and `kill-server.bat` first, then compiles all `.java` files in `server/` and starts `BilClubsServer`.

### Option 2: Server + UI (recommended)
Starts the server in a background window, waits 3 seconds for it to initialize, then compiles and launches the JavaFX UI.
```bat
run-unified.bat
```

### Option 3: Run tests
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
| `clean-db.bat` | Wipes all files in `server/db/`: **deletes all data** |
| `kill-server.bat` | Force-terminates any running `BilClubsServer` process |
| `inspect-database.bat` | Opens the ObjectDB Explorer GUI against `server/db/bilclubs.odb` |
| `run-swing-tester.bat` | Launches a lightweight Swing-based manual test harness |
| `run-drop-db.bat` | Drops and recreates the database, then starts the server |

---

## Features

### General

- **Bilkent WebMail authentication**: Accounts are created by verifying credentials against the university WebMail service. No specific passwords are stored.
- **Role-based privilege system**: Privilege levels (`NORMAL_USER`, `ADMIN`) plus a `BANNED_USER` state, stored as bitflags per user and per club.
- **Club management**: Create clubs, customize name/description/icon/cover image, manage members and their roles.
- **Event management**: Create events with title, description, location, start/end times and optional attendance quota. Events can award GE250 points.
- **Media uploads**: Profile pictures, club icons, club covers and event posters can be uploaded as images (up to 16 MB per request).
- **GE250 point information**: Users can see the GE250 points awarded by the events before joining them.
- **Interest-based recommendations**: New users select interest keywords and enter biography which are used to personalize club and event recommendations.
- **Keyword based search**: Club and event descriptions are included in the keyword search and the exact matches are displayed as results.
- **Email notifications**: HTML emails are dispatched asynchronously for: account creation, joining/leaving a club, registering/leaving an event and event creation for members of a club.
- **Calendar view**: Registered events are displayed in a monthly calendar in the UI.
- **Admin panel**: Admins can manage and ban users, clubs and events from a dedicated management page.
- **Most clubs included**: `SyntheticDatabaseTest` populates the database with real club datas obtained using web scraping and synthetic event data for testing.

### UI-Specific (JavaFX)

- **Scene-based navigation**
- **Loading indicators**: An animated loading GIF is displayed during server requests to prevent UI freezing.
- **Dark / Light theme toggle**: Managed by `ThemeManager`; persists across scenes via a CSS stylesheet swap.
- **Custom reusable components**: `ClubPane`, `EventPane`, `SearchResultPane`, `NotificationCard`, `MemberCard`, `BanClubCard`, `BanEventCard`, `DetailedEventCard`.
- **Notification panel**: In-app alerts for club and event activity.
- **Club creation / event creation forms**: Multi field FXML forms with live validation feedback.
- **Interest keywords selection**: Scrollable tag based UI + embeddings based interest processing during and after onboarding.
- **Settings page**: Users can update their profile picture, interests, biography, major, name and notification preferences.

### Security-Specific


#### Authentication & Session Management

- **No password storage**: Authentication is delegated entirely to Bilkent WebMail. The server forwards credentials to `webmail.bilkent.edu.tr` for verification and never persists them. Bilkent WebMail credentials are verified on every login and signup; the server only stores a session token after successful verification.
- **Cryptographically random session tokens**: Generated via `java.security.SecureRandom` as a 32-character alphanumeric string. The token is stored in the format `<token>:<expiry_epoch_ms>` and expires after 24 hours (`ServerConfig.SESSION_TOKEN_TTL`).
- **Token invalidation on every login**: Each new login call to `generateToken()` replaces any existing token, making prior sessions immediately invalid. There is no way to hold multiple concurrent sessions.
- **Explicit logout invalidation**: The `/api/user` `logout` action calls `user.clearToken()` and persists the change, actively nulling the stored token.
- **Banned account login blocked**: The `login` handler checks `userByEmail.isBanned()` before performing WebMail verification, preventing banned accounts from obtaining a new session token at all.

#### Authorization & Privilege Enforcement

- **Two-tier privilege model**: General privileges (`BANNED_USER`, `NORMAL_USER`, `ADMIN` etc.) are stored as bitflags on the user. Club-level privileges (same flags) are stored separately per club in a `HashMap<Integer, Integer>`, allowing a user to be a club admin in one club and a regular member in another.
- **Authentication gate before all protected actions**: `handleUserAction` dispatches `signup` and `login` without authentication, and routes every other action through `authenticate()` first. A missing or invalid `userId`/`sessionToken` pair is rejected with `401`/`403` before any business logic runs.
- **Admin-only operations**: `listUsers`, `banUser`, `banEvent`, and `banClub` each explicitly check `user.hasGeneralPrivilege(Privileges.ADMIN)` and return `401` if the caller is not an admin.
- **Club-scoped permission for event operations**: `createEvent` and `modifyEvent` both check `user.hasClubPrivilege(club, Privileges.ADMIN)`, ensuring only the admin of the specific club can create or edit its events.
- **Club creation restricted to ADMIN**: `createClub` checks `hasGeneralPrivilege(ADMIN)`, preventing ordinary users from creating clubs.
- **Banned-user club actions blocked in DBManager**: `addUserToClub` checks `newMember.isBannedFromClub(club)` before adding, and `removeUserFromClub` checks the same flag to prevent a banned member from leaving and erasing their own ban record.

#### Input Validation

- **HTTP method enforcement**: The `handle()` dispatcher immediately returns `400` for any non-`POST` request.
- **Malformed JSON rejection**: JSON parse failures in `handle()` return `400` rather than propagating exceptions.
- **Request body size cap**: `StreamReader` enforces `ServerConfig.MAX_REQUEST_BYTES` (16 MB); oversized bodies throw an exception caught as `413` to prevent DDoS resource exhaustion.
- **Name format validation**: First and last names must be at least 3 characters and match the regex `^[a-zA-ZçğıöşüÇĞİÖŞÜ ]+$`, permitting Turkish characters but blocking digits, symbols, and control characters. This is enforced at both signup and `updateProfile`.
- **Field length minimums**: Club names: 3 chars; club descriptions: 20 chars; event names: 3 chars; event descriptions: 20 chars; event locations: 3 chars. All enforced before any DB write.
- **Temporal validation**: `createEvent` verifies `endEpoch > startEpoch`, preventing zero-duration or reversed events.
- **GE250 non-negative check**: Both `createEvent` and `modifyEvent` reject negative GE250 values with `400`.
- **Input sanitization**: `Sanitizer.sanitizeEscapedString()` escapes JSON control characters (`\`, `"`, `\n`, `\r`, `\t`) and neutralizes HTML angle brackets and SQL-style keywords (` AND `, ` OR `, ` WHERE `).

#### File Upload Security

- **Extension allowlist**: The upload handler only accepts files whose `fileType` field matches one of: `png`, `jpg`, `jpeg`, `pdf`, `gif`. Any other extension is silently skipped without error.
- **UUID-randomized storage names**: Uploaded files are saved as `UUID.randomUUID().toString() + "." + extension`, making stored filenames completely unpredictable and preventing original-filename path traversal or enumeration.
- **Media ownership tracking**: Every uploaded file is recorded in the `Media` entity with `media.setUserId(user.getId())`, linking it to the authenticated uploader.
- **Failed write cleanup**: If the `FileOutputStream` write fails, `newFile.delete()` is called immediately to avoid leaving partial files on disk.

#### Database & Query Safety

- **Parameterized queries throughout**: All database lookups in `DBManager` use JPA `TypedQuery` with `setParameter()` binding. User-supplied filter values are never concatenated into query strings to prevent SQL injection (or to be more accurate, JPQ injection) attacks.
- **Query key allowlisting**: Each `query*` method defines an explicit `keyMap` of permitted filter field names. Any key not in the map is silently discarded before the query is built, making it structurally impossible to inject arbitrary JPQL clauses via the `Filter` object.
- **Transactional writes**: Every DB mutation uses `em.getTransaction().begin()` / `em.getTransaction().commit()`. `removeUserFromClubDirect` calls `em.getTransaction().rollback()` explicitly if the target user is not found, preventing partial state.
- **Duplicate email prevention at the DB layer**: `DBManager.addUser()` re-checks email uniqueness before persisting, independently of the API-layer check in `signup()`.
- **Cascading delete correctness**: `deleteEvent`, `deleteClub`, and `deleteUser` each perform multi-step cleanup in the correct order: remove cross-entity references first (club membership, event registrations), then remove Discussion and Comment sub-entities, then remove the root entity. This avoids orphaned/dangling references in the object graph.
- **Event registration double-check**: `DBManager.addUserToEvent()` calls `user.canRegisterToEvent(event)` before writing, re-validating quota, open status and schedule conflict even if the API handler already checked.

#### Information Exposure Controls

- **Email withheld from public profiles**: `getForeignProfile` intentionally omits the `email` field returning only name, major, interests, biography and club affiliations.
- **Banned users excluded from admin listing**: `listUsers` skips any user whose privilege equals `BANNED_USER`, keeping banned account data out of the admin user list.
- **Credentials loaded from environment**: `APIHandler` reads SMTP and API credentials via `System.getenv()` into a `Credentials` object at startup; no secrets appear in source code or config files.
- **ObjectDB isolation**: The database is embedded and file local so it is not exposed on any network port.


---

## API Reference

See [API.md](API.md) for the full HTTP API documentation, including all endpoints, request/response shapes, authentication requirements, and error codes.

**Quick reference:**
- Base URL: `http://localhost:5000`
- All API calls use `POST` with `Content-Type: application/json`
- Protected endpoints require `userId` (integer) and `sessionToken` (string) in the request body
- All responses use a standard envelope: `{ "responseCode", "success", "data", "error" }`
