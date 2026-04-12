# BilClubs API Reference

This document covers all HTTP API endpoints exposed by the BilClubs server.

---

## Conventions

- **Base URL:** `http://<host>:5000`
- **Method:** All API endpoints accept only `POST`
- **Content-Type:** `application/json; charset=UTF-8`
- **Max body size:** `16 MB` (see `ServerConfig.MAX_REQUEST_BYTES`)
- **Action routing:** Every request must include an `"action"` field in the JSON body. The URL path identifies the resource (`/api/user`, `/api/club`, `/api/event`, `/api/upload`), and `action` identifies the operation within that resource.
- **Authentication:** Endpoints marked 🔒 require `userId` (int) and `sessionToken` (string) in the request body, obtained from the `login` action.

---

## Response envelope

Every response, success or failure, is wrapped in the same envelope:

```json
{
  "responseCode": 200,
  "success": true,
  "data": { ... },
  "error": {
    "message": "Human readable error description"
  }
}
```

| Field | Type | Description |
| :--- | :--- | :--- |
| `responseCode` | `int` | Standard HTTP status code |
| `success` | `boolean` | `true` when `responseCode` is in the 2xx range |
| `data` | `object` | Present on success. Shape varies per endpoint, see below |
| `error` | `object` | Present on failure. Contains a `message` string |

---

## Error reference

| Code | Meaning |
| :--- | :--- |
| `400` | Bad request, missing or invalid fields |
| `401` | Unauthenticated, credentials not provided or insufficient privilege |
| `403` | Forbidden, credentials provided but invalid, expired or account banned |
| `404` | Resource not found |
| `413` | Payload too large |
| `500` | Internal server error |
| `501` | Endpoint not implemented |

---

## `POST /api/user`

All user actions share the same URL. The `action` field selects the operation.

---

### action: `signup`

Creates a new user account. The provided credentials are verified against Bilkent WebMail before the account is created. On success, a welcome email is sent asynchronously and a session token is returned immediately.

**Note**: WebMail credentials are never stored in our server.

#### Request body

```json
{
  "action":    "signup",
  "email":     "student@ug.bilkent.edu.tr",
  "password":  "bilkent_webmail_password",
  "firstName": "Ozan",
  "lastName":  "Özbek",
  "major":     "Computer Engineering"
}
```

| Field | Type | Required | Constraints |
| :--- | :--- | :---: | :--- |
| `action` | `string` | ✅ | `"signup"` |
| `email` | `string` | ✅ | Must end in `bilkent.edu.tr` or `.bilkent.edu.tr` |
| `password` | `string` | ✅ | Bilkent WebMail password, never stored |
| `firstName` | `string` | ✅ | Min 3 chars. Letters only (Latin + Turkish: `a-z A-Z ç ğ ı ö ş ü` and space) |
| `lastName` | `string` | ✅ | Same constraints as `firstName` |
| `major` | `string` | - | Optional. Department or programme name |

#### Success response `200`

```json
{
  "responseCode": 200,
  "success": true,
  "data": {
    "email":        "student@ug.bilkent.edu.tr",
    "fullName":     "Ozan Özbek",
    "sessionToken": "A3FX9KQZ1BNW7YRC2PLT8VHD4EMJ5SU:1782000000000",
    "userId":       42
  }
}
```

| Field | Type | Description |
| :--- | :--- | :--- |
| `email` | `string` | The registered email address |
| `fullName` | `string` | First name + last name concatenated |
| `sessionToken` | `string` | Active session token. Format: `<32-char alphanumeric>:<expiry epoch ms>` |
| `userId` | `int` | Auto-generated numeric user ID |

#### Error responses

| Code | Condition |
| :--- | :--- |
| `400` | Missing `email`, `password`, `firstName`, or `lastName` |
| `400` | Email does not belong to Bilkent University |
| `400` | `firstName` or `lastName` is shorter than 3 characters |
| `400` | `firstName` or `lastName` contains non-letter characters |
| `400` | An account with this email already exists |
| `400` | Bilkent WebMail credential verification failed |
| `413` | Request body exceeds 16 MB |
| `500` | WebMail service error |
| `500` | Database error |

---

### action: `login`

Verifies credentials against Bilkent WebMail, generates a new session token, persists it, and returns it alongside the user ID. Every login invalidates the previous token, the token returned here is the only valid one until the next login.

#### Request body

```json
{
  "action":   "login",
  "email":    "student@ug.bilkent.edu.tr",
  "password": "bilkent_webmail_password"
}
```

| Field | Type | Required | Description |
| :--- | :--- | :---: | :--- |
| `action` | `string` | ✅ | `"login"` |
| `email` | `string` | ✅ | Bilkent WebMail address |
| `password` | `string` | ✅ | Bilkent WebMail password |

#### Success response `200`

```json
{
  "responseCode": 200,
  "success": true,
  "data": {
    "sessionToken": "A3FX9KQZ1BNW7YRC2PLT8VHD4EMJ5SU:1782000000000",
    "userId": 42
  }
}
```

| Field | Type | Description |
| :--- | :--- | :--- |
| `sessionToken` | `string` | Active session token. Format: `<32-char alphanumeric>:<expiry epoch ms>` |
| `userId` | `int` | Numeric ID of the authenticated user |

#### Error responses

| Code | Condition |
| :--- | :--- |
| `400` | Missing `email` or `password` |
| `401` | Bilkent WebMail credential verification failed |
| `403` | Account is banned |
| `404` | No account found for this email |
| `413` | Request body exceeds 16 MB |
| `500` | WebMail service error |

---

### action: `logout` 🔒

Invalidates the user's current session token server side. After this call, the token can no longer be used and the user is practically logged out.

#### Request body

```json
{
  "action":       "logout",
  "userId":       42,
  "sessionToken": "A3FX9KQZ1BNW7YRC2PLT8VHD4EMJ5SU:1782000000000"
}
```

#### Success response `200`

No `data` field.

---

### action: `getProfile` 🔒

Returns the full profile of the authenticated user, including private fields such as email and notification preferences.

#### Request body

```json
{
  "action":       "getProfile",
  "userId":       42,
  "sessionToken": "..."
}
```

#### Success response `200`

```json
{
  "responseCode": 200,
  "success": true,
  "data": {
    "userId":       42,
    "email":        "student@ug.bilkent.edu.tr",
    "firstName":    "Ozan",
    "lastName":     "Özbek",
    "major":        "Computer Engineering",
    "biography":    "Hi, I love robotics.",
    "profilePicture": "static/abc123.png",
    "interests":    ["robotics", "music"],
    "clubPrivileges": { "7": 8, "12": 1 },
    "privilege":    1,
    "wantToRecieveMails":                  true,
    "wantToRecieveClubAndEventAlerts":     true,
    "wantToRecieveGeneralNotifications":   true
  }
}
```

| Field | Type | Description |
| :--- | :--- | :--- |
| `userId` | `int` | User's numeric ID |
| `email` | `string` | Bilkent email address |
| `firstName` | `string` | First name |
| `lastName` | `string` | Last name |
| `major` | `string` | Department or programme |
| `biography` | `string` | User-written bio |
| `profilePicture` | `string` | Server-relative path to the profile image |
| `interests` | `string[]` | List of interest keywords |
| `clubPrivileges` | `object` | Map of `clubId -> privilegeFlag` for every club the user belongs to |
| `privilege` | `int` | General system privilege bitflag (see Privilege flags) |
| `wantToRecieveMails` | `boolean` | Whether transactional emails are enabled |
| `wantToRecieveClubAndEventAlerts` | `boolean` | Whether club/event alert emails and in-app notifications are enabled |
| `wantToRecieveGeneralNotifications` | `boolean` | Whether general in-app notifications are enabled |

---

### action: `getForeignProfile` 🔒

Returns the public facing profile of another user. Email is intentionally omitted.

#### Request body

```json
{
  "action":       "getForeignProfile",
  "userId":       42,
  "sessionToken": "...",
  "targetUserId": 99
}
```

| Field | Type | Required | Description |
| :--- | :--- | :---: | :--- |
| `targetUserId` | `int` | ✅ | ID of the user to look up |

#### Success response `200`

```json
{
  "data": {
    "userId":        99,
    "fullName":      "Ayşe Kaya",
    "major":         "Electrical Engineering",
    "biography":     "...",
    "interests":     ["jazz", "hiking"],
    "clubPrivileges": { "7": 1 }
  }
}
```

| Field | Type | Description |
| :--- | :--- | :--- |
| `userId` | `int` | Target user's ID |
| `fullName` | `string` | Full name |
| `major` | `string` | Department or programme |
| `biography` | `string` | User-written bio |
| `interests` | `string[]` | Interest keywords |
| `clubPrivileges` | `object` | Map of `clubId -> privilegeFlag` |

#### Error responses

| Code | Condition |
| :--- | :--- |
| `400` | `targetUserId` not provided |
| `404` | No user found for `targetUserId` |

---

### action: `getForeignProfileClubs` 🔒

Returns the list of clubs a given user belongs to.

#### Request body

```json
{
  "action":       "getForeignProfileClubs",
  "userId":       42,
  "sessionToken": "...",
  "targetUserId": 99
}
```

| Field | Type | Required | Description |
| :--- | :--- | :---: | :--- |
| `targetUserId` | `int` | ✅ | ID of the user whose clubs to fetch |

#### Success response `200`

```json
{
  "data": {
    "clubs": [
      {
        "id":           7,
        "name":         "Robotics Club",
        "iconFilename": "static/abc.png",
        "coverFilename":"static/xyz.jpg",
        "description":  "We build robots."
      }
    ]
  }
}
```

#### Error responses

| Code | Condition |
| :--- | :--- |
| `400` | `targetUserId` not provided |
| `404` | No user found for `targetUserId` |

---

### action: `updateProfile` 🔒

Partially updates the authenticated user's profile. Only fields present in the request body are modified; omitted fields are left unchanged.

#### Request body

```json
{
  "action":       "updateProfile",
  "userId":       42,
  "sessionToken": "...",
  "firstName":    "Ozan",
  "lastName":     "Özbek",
  "major":        "Computer Engineering",
  "biography":    "Updated bio.",
  "profilePicture": "uploaded-uuid.png",
  "interests":    ["jazz", "robotics"],
  "wantToRecieveMails":                true,
  "wantToRecieveClubAndEventAlerts":   false,
  "wantToRecieveGeneralNotifications": true
}
```

| Field | Type | Required | Constraints |
| :--- | :--- | :---: | :--- |
| `firstName` | `string` | - | Min 3 chars, letters only |
| `lastName` | `string` | - | Min 3 chars, letters only |
| `major` | `string` | - | Cannot be empty if provided |
| `biography` | `string` | - | No length constraint |
| `profilePicture` | `string` | - | Stored filename returned by `upload`. Cannot be empty if provided. The server prepends `"static/"` automatically |
| `interests` | `string[]` | - | Replaces the entire interests list |
| `wantToRecieveMails` | `boolean` | - | |
| `wantToRecieveClubAndEventAlerts` | `boolean` | - | |
| `wantToRecieveGeneralNotifications` | `boolean` | - | |

#### Success response `200`

No `data` field.

#### Error responses

| Code | Condition |
| :--- | :--- |
| `400` | `firstName`/`lastName` shorter than 3 characters or contains non-letter characters |
| `400` | `major` is an empty string |
| `400` | `profilePicture` is an empty string |

---

### action: `setInterests` 🔒

Replaces the authenticated user's interest keywords list entirely.

#### Request body

```json
{
  "action":       "setInterests",
  "userId":       42,
  "sessionToken": "...",
  "interests":    ["music", "machine learning", "chess"]
}
```

| Field | Type | Required | Description |
| :--- | :--- | :---: | :--- |
| `interests` | `string[]` | ✅ | New interests list. Replaces the previous list in full |

#### Success response `200`

No `data` field.

---

### action: `generateEmbeddings` 🔒

Triggers asynchronous generation of the user's interest embedding vector via the Gemini API. The vector is used for club and event recommendations. Returns immediately; the embedding is generated in the background.

#### Request body

```json
{
  "action":       "generateEmbeddings",
  "userId":       42,
  "sessionToken": "..."
}
```

#### Success response `200`

No `data` field.

---

### action: `getUpcomingEvents` 🔒

Returns a filtered list of upcoming events. Multiple filters can be combined.

#### Request body

```json
{
  "action":       "getUpcomingEvents",
  "userId":       42,
  "sessionToken": "...",
  "upToEpoch":    1800000000,
  "userSpecific": false,
  "getAll":       false,
  "clubIds":      [7, 12]
}
```

| Field | Type | Required | Description |
| :--- | :--- | :---: | :--- |
| `upToEpoch` | `long` | - | Unix epoch seconds. Only events starting before this time are returned. Defaults to `Long.MAX_VALUE` (no upper bound) |
| `userSpecific` | `boolean` | - | When `true`, only events the caller is registered for are returned. Default `false` |
| `getAll` | `boolean` | - | When `true`, bypasses the `clubIds` filter and returns all upcoming events. Default `false` |
| `clubIds` | `int[]` | - | When provided and `getAll` is `false`, only events belonging to one of these club IDs are returned |

#### Success response `200`

```json
{
  "data": {
    "events": [
      {
        "name":          "Robotics Workshop",
        "description":   "Hands-on session with ROS2.",
        "quota":         30,
        "registreeCount": 12,
        "location":      "EA building, room 202",
        "startDate":     "2026-05-10T14:00:00",
        "endDate":       "2026-05-10T17:00:00",
        "duration":      "3 hours",
        "GE250":         1,
        "posterImage":   "static/poster-uuid.jpg",
        "clubName":      "Robotics Club",
        "clubId":        7,
        "eventId":       55
      }
    ]
  }
}
```

| Field | Type | Description |
| :--- | :--- | :--- |
| `name` | `string` | Event title |
| `description` | `string` | Full description |
| `quota` | `int` \| `null` | Maximum attendees. `null` means unlimited |
| `registreeCount` | `int` | Current number of registered attendees |
| `location` | `string` | Venue |
| `startDate` | `string` | ISO-8601 datetime (UTC) |
| `endDate` | `string` | ISO-8601 datetime (UTC) |
| `duration` | `string` | Human-readable duration |
| `GE250` | `int` | GE250 activity points awarded for this event |
| `posterImage` | `string` | Server-relative path to the event poster |
| `clubName` | `string` | Name of the organizing club |
| `clubId` | `int` | ID of the organizing club |
| `eventId` | `int` | Unique event ID |

---

### action: `listClubs` 🔒

Returns a flat list of all clubs with the caller's membership privilege in each.

#### Request body

```json
{
  "action":       "listClubs",
  "userId":       42,
  "sessionToken": "..."
}
```

#### Success response `200`

```json
{
  "data": {
    "clubs": [
      {
        "id":              7,
        "clubName":        "Robotics Club",
        "memberCount":     34,
        "clubDescription": "We build robots and compete internationally.",
        "clubPrivilege":   8,
        "iconFilename":    "static/icon-uuid.png",
        "coverFilename":   "static/cover-uuid.jpg"
      }
    ]
  }
}
```

| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | `int` | Club ID |
| `clubName` | `string` | Club name |
| `memberCount` | `int` | Current number of members |
| `clubDescription` | `string` | Full description |
| `clubPrivilege` | `int` \| `null` | The caller's privilege flag for this club. `null` if the caller is not a member |
| `iconFilename` | `string` | Server-relative path to the club icon |
| `coverFilename` | `string` | Server-relative path to the club cover image |

---

### action: `listUsers` 🔒 (admin only)

Returns a list of all non-banned users. Requires `ADMIN` general privilege.

#### Request body

```json
{
  "action":       "listUsers",
  "userId":       42,
  "sessionToken": "..."
}
```

#### Success response `200`

```json
{
  "data": {
    "users": [
      {
        "id":             99,
        "name":           "Ayşe Kaya",
        "email":          "akaya@ug.bilkent.edu.tr",
        "major":          "Electrical Engineering",
        "profilePicture": "static/pfp-uuid.png",
        "privilege":      1
      }
    ]
  }
}
```

#### Error responses

| Code | Condition |
| :--- | :--- |
| `401` | Caller does not have `ADMIN` privilege |

---

### action: `joinClub` 🔒

Adds the authenticated user as a member of a club.

#### Request body

```json
{
  "action":       "joinClub",
  "userId":       42,
  "sessionToken": "...",
  "clubId":       7
}
```

| Field | Type | Required | Description |
| :--- | :--- | :---: | :--- |
| `clubId` | `int` | ✅ | ID of the club to join |

#### Success response `200`

No `data` field.

#### Error responses

| Code | Condition |
| :--- | :--- |
| `400` | `clubId` not provided |
| `400` | Club not found |
| `400` | User is already a member of this club |

---

### action: `leaveClub` 🔒

Removes the authenticated user from a club. Also removes the user from all events belonging to that club that they were registered for.

#### Request body

```json
{
  "action":       "leaveClub",
  "userId":       42,
  "sessionToken": "...",
  "clubId":       7
}
```

| Field | Type | Required | Description |
| :--- | :--- | :---: | :--- |
| `clubId` | `int` | ✅ | ID of the club to leave |

#### Success response `200`

No `data` field.

#### Error responses

| Code | Condition |
| :--- | :--- |
| `400` | `clubId` not provided |
| `400` | Club not found |
| `400` | User is not a member of this club |

---

### action: `banUser` 🔒 (admin only)

Permanently removes a user account. Cleans up all club memberships, event registrations and authored comments. Requires `ADMIN` general privilege.

#### Request body

```json
{
  "action":       "banUser",
  "userId":       42,
  "sessionToken": "...",
  "targetUserId": 99
}
```

| Field | Type | Required | Description |
| :--- | :--- | :---: | :--- |
| `targetUserId` | `int` | ✅ | ID of the user to ban |

#### Success response `200`

No `data` field.

#### Error responses

| Code | Condition |
| :--- | :--- |
| `400` | `targetUserId` not provided |
| `400` | Admin attempted to ban themselves |
| `401` | Caller does not have `ADMIN` privilege |
| `500` | Database error |

---

### action: `banEvent` 🔒 (admin only)

Permanently removes an event. Unregisters all attendees and deletes the associated discussion and comments. Requires `ADMIN` general privilege.

#### Request body

```json
{
  "action":       "banEvent",
  "userId":       42,
  "sessionToken": "...",
  "eventId":      55
}
```

| Field | Type | Required | Description |
| :--- | :--- | :---: | :--- |
| `eventId` | `int` | ✅ | ID of the event to remove |

#### Success response `200`

No `data` field.

#### Error responses

| Code | Condition |
| :--- | :--- |
| `400` | `eventId` not provided |
| `401` | Caller does not have `ADMIN` privilege |
| `500` | Database error |

---

### action: `banClub` 🔒 (admin only)

Permanently removes a club along with all of its events, memberships, and discussions. Requires `ADMIN` general privilege.

#### Request body

```json
{
  "action":       "banClub",
  "userId":       42,
  "sessionToken": "...",
  "clubId":       7
}
```

| Field | Type | Required | Description |
| :--- | :--- | :---: | :--- |
| `clubId` | `int` | ✅ | ID of the club to remove |

#### Success response `200`

No `data` field.

#### Error responses

| Code | Condition |
| :--- | :--- |
| `400` | `clubId` not provided |
| `401` | Caller does not have `ADMIN` privilege |
| `500` | Database error |

---

## `POST /api/club`

All club actions require authentication.

---

### action: `create` 🔒 (manager / admin)

Creates a new club. Requires  `ADMIN` general privilege. The creator is automatically added as the club's first admin member. An embedding is generated asynchronously after creation.

#### Request body

```json
{
  "action":           "create",
  "userId":           42,
  "sessionToken":     "...",
  "clubName":         "Robotics Club",
  "clubDescription":  "We build autonomous robots and compete in international championships.",
  "iconFilename":     "uploaded-icon-uuid.png",
  "coverFilename":    "uploaded-cover-uuid.jpg"
}
```

| Field | Type | Required | Constraints |
| :--- | :--- | :---: | :--- |
| `clubName` | `string` | ✅ | Min 3 characters |
| `clubDescription` | `string` | ✅ | Min 20 characters |
| `iconFilename` | `string` | - | Stored filename returned by `upload`. The server prepends `"static/"` automatically |
| `coverFilename` | `string` | - | Same as `iconFilename` |

#### Success response `200`

```json
{
  "data": {
    "clubId":   7,
    "clubName": "Robotics Club"
  }
}
```

#### Error responses

| Code | Condition |
| :--- | :--- |
| `400` | `clubName` or `clubDescription` missing |
| `400` | `clubName` shorter than 3 characters |
| `400` | `clubDescription` shorter than 20 characters |
| `401` | Caller does not have `MANAGER` or `ADMIN` privilege |
| `500` | Database error |

---

### action: `getMembers` 🔒

Returns the member list of a club. Email addresses are intentionally omitted from the response.

#### Request body

```json
{
  "action":       "getMembers",
  "userId":       42,
  "sessionToken": "...",
  "clubId":       7
}
```

| Field | Type | Required | Description |
| :--- | :--- | :---: | :--- |
| `clubId` | `int` | ✅ | ID of the club |

#### Success response `200`

```json
{
  "data": {
    "members": [
      {
        "id":             42,
        "name":           "Ozan Özbek",
        "privilege":      8,
        "profilePicture": "static/pfp-uuid.png",
        "major":          "Computer Engineering"
      }
    ]
  }
}
```

| Field | Type | Description |
| :--- | :--- | :--- |
| `id` | `int` | User ID |
| `name` | `string` | Full name |
| `privilege` | `int` \| `null` | Member's privilege flag within this club |
| `profilePicture` | `string` | Server-relative path to profile image |
| `major` | `string` | Department or programme |

#### Error responses

| Code | Condition |
| :--- | :--- |
| `400` | `clubId` not provided |
| `404` | Club not found |

---

### action: `search` 🔒

Searches clubs by keyword. Results are scored and ranked by a combination of name and description relevance. Only clubs with a positive score are returned.

#### Request body

```json
{
  "action":       "search",
  "userId":       42,
  "sessionToken": "...",
  "query":        "robotics automation"
}
```

| Field | Type | Required | Description |
| :--- | :--- | :---: | :--- |
| `query` | `string` | ✅ | Search string. Cannot be empty |

#### Success response `200`

```json
{
  "data": {
    "count": 2,
    "results": [
      {
        "rank":        1,
        "id":          7,
        "name":        "Robotics Club",
        "description": "We build autonomous robots...",
        "iconFilename":"static/icon-uuid.png"
      }
    ]
  }
}
```

#### Error responses

| Code | Condition |
| :--- | :--- |
| `400` | `query` is empty |

---

### action: `recommend` 🔒

Returns the top N clubs most similar to the caller's interest embedding. Requires the user to have previously generated embeddings via `generateEmbeddings`.

#### Request body

```json
{
  "action":       "recommend",
  "userId":       42,
  "sessionToken": "...",
  "number":       5
}
```

| Field | Type | Required | Description |
| :--- | :--- | :---: | :--- |
| `number` | `int` | - | Maximum number of results to return. Default `5` |

#### Success response `200`

```json
{
  "data": {
    "count": 3,
    "results": [
      {
        "rank":        1,
        "id":          7,
        "name":        "Robotics Club",
        "description": "We build autonomous robots...",
        "iconFilename":"static/icon-uuid.png",
        "similarity":  0.91
      }
    ]
  }
}
```

| Field | Type | Description |
| :--- | :--- | :--- |
| `similarity` | `double` | Cosine similarity score between the user's embedding and the club's embedding (0–1) |

#### Error responses

| Code | Condition |
| :--- | :--- |
| `400` | User has no generated embeddings |

---

## `POST /api/event`

All event actions require authentication.

---

### action: `create` 🔒 (club admin)

Creates a new event under a club. Only the `ADMIN`-privileged member of that specific club may call this. An embedding is generated asynchronously after creation, and notification emails are dispatched to all club members who have alerts enabled.

#### Request body

```json
{
  "action":          "create",
  "userId":          42,
  "sessionToken":    "...",
  "clubId":          7,
  "name":            "Robotics Workshop",
  "description":     "Hands-on session covering ROS2 navigation and path planning.",
  "location":        "EA building, room 202",
  "startEpoch":      1746878400,
  "endEpoch":        1746889200,
  "quota":           30,
  "GE250":           1,
  "posterFilename":  "uploaded-poster-uuid.jpg"
}
```

| Field | Type | Required | Constraints |
| :--- | :--- | :---: | :--- |
| `clubId` | `int` | ✅ | ID of the organizing club |
| `name` | `string` | ✅ | Min 3 characters |
| `description` | `string` | ✅ | Min 20 characters |
| `location` | `string` | ✅ | Min 3 characters |
| `startEpoch` | `long` | ✅ | Unix epoch **seconds** (UTC) |
| `endEpoch` | `long` | ✅ | Unix epoch **seconds** (UTC). Must be after `startEpoch` |
| `quota` | `int` | - | Maximum attendees. Omit or set to `null` for unlimited |
| `GE250` | `int` | - | Activity points awarded. Must be ≥ 0. Default `0` |
| `posterFilename` | `string` | - | Stored filename returned by `upload` |

#### Success response `200`

```json
{
  "data": {
    "eventId":   55,
    "eventName": "Robotics Workshop",
    "clubId":    7
  }
}
```

#### Error responses

| Code | Condition |
| :--- | :--- |
| `400` | Missing required fields |
| `400` | `name`, `description`, or `location` fails minimum length |
| `400` | `endEpoch` is not after `startEpoch` |
| `400` | `GE250` is negative |
| `403` | Caller is not an `ADMIN` of the specified club |
| `404` | Club not found |
| `500` | Database error |

---

### action: `register` 🔒

Registers the authenticated user for an event. Registration is blocked if the event is full, already started, or conflicts with another event the user is registered for.

#### Request body

```json
{
  "action":       "register",
  "userId":       42,
  "sessionToken": "...",
  "eventId":      55
}
```

| Field | Type | Required | Description |
| :--- | :--- | :---: | :--- |
| `eventId` | `int` | ✅ | ID of the event to register for |

#### Success response `200`

```json
{
  "data": {
    "eventId":       55,
    "eventName":     "Robotics Workshop",
    "registreeCount": 13
  }
}
```

#### Error responses

| Code | Condition |
| :--- | :--- |
| `400` | `eventId` not provided |
| `400` | User is already registered for this event |
| `400` | Event is full, has already started, or conflicts with another registered event |
| `404` | Event not found |
| `500` | Database error |

---

### action: `leave` 🔒

Unregisters the authenticated user from an event.

#### Request body

```json
{
  "action":       "leave",
  "userId":       42,
  "sessionToken": "...",
  "eventId":      55
}
```

| Field | Type | Required | Description |
| :--- | :--- | :---: | :--- |
| `eventId` | `int` | ✅ | ID of the event to leave |

#### Success response `200`

```json
{
  "data": {
    "eventId":       55,
    "eventName":     "Robotics Workshop",
    "registreeCount": 12
  }
}
```

#### Error responses

| Code | Condition |
| :--- | :--- |
| `400` | `eventId` not provided |
| `400` | User is not registered for this event |
| `404` | Event not found |
| `500` | Database error |

---

### action: `search` 🔒

Searches upcoming events by keyword. Only events that have not yet started are included. Results are scored and ranked by name and description relevance.

#### Request body

```json
{
  "action":       "search",
  "userId":       42,
  "sessionToken": "...",
  "query":        "robotics workshop"
}
```

| Field | Type | Required | Description |
| :--- | :--- | :---: | :--- |
| `query` | `string` | ✅ | Search string. Cannot be empty |

#### Success response `200`

```json
{
  "data": {
    "count": 1,
    "results": [
      {
        "rank":          1,
        "id":            55,
        "name":          "Robotics Workshop",
        "description":   "Hands-on session...",
        "location":      "EA building, room 202",
        "startDate":     "2026-05-10T14:00:00",
        "endDate":       "2026-05-10T17:00:00",
        "duration":      "3 hours",
        "clubId":        7,
        "clubName":      "Robotics Club",
        "GE250":         1,
        "quota":         30,
        "registreeCount":13,
        "posterImage":   "static/poster-uuid.jpg"
      }
    ]
  }
}
```

#### Error responses

| Code | Condition |
| :--- | :--- |
| `400` | `query` is empty |

---

### action: `recommend` 🔒

Returns the top N upcoming events most similar to the caller's interest embedding. Requires the user to have previously generated embeddings via `generateEmbeddings`.

#### Request body

```json
{
  "action":       "recommend",
  "userId":       42,
  "sessionToken": "...",
  "number":       5
}
```

| Field | Type | Required | Description |
| :--- | :--- | :---: | :--- |
| `number` | `int` | - | Maximum number of results to return. Default `5` |

#### Success response `200`

Same shape as `event/search`, with an additional `similarity` field on each result item:

| Field | Type | Description |
| :--- | :--- | :--- |
| `similarity` | `double` | Cosine similarity score between the user's embedding and the event's embedding (0–1) |

#### Error responses

| Code | Condition |
| :--- | :--- |
| `400` | User has no generated embeddings |

---

## `POST /api/upload` 🔒

### action: `upload`

Uploads one or more files on behalf of the authenticated user. Each file must be base64-encoded in the request body. Files are saved to `./static/` under a UUID-randomized filename. Files with an unrecognised `fileType` are silently skipped and do not fail the request.

#### Request body

```json
{
  "action":       "upload",
  "userId":       42,
  "sessionToken": "...",
  "files": [
    {
      "fileName": "photo.png",
      "fileData": "<base64-encoded content>",
      "fileType": "png"
    },
    {
      "fileName": "document.pdf",
      "fileData": "<base64-encoded content>",
      "fileType": "pdf"
    }
  ]
}
```

| Field | Type | Required | Description |
| :--- | :--- | :---: | :--- |
| `files` | `array` | ✅ | Array of file objects |

##### File object

| Field | Type | Required | Description |
| :--- | :--- | :---: | :--- |
| `fileName` | `string` | ✅ | Original filename, used as the key in the response maps |
| `fileData` | `string` | ✅ | Base64-encoded file content |
| `fileType` | `string` | ✅ | Extension without the dot. Allowed values: `png` `jpg` `jpeg` `pdf` `gif`. Any other value causes this file to be silently skipped |

#### Success response `200`

```json
{
  "responseCode": 200,
  "success": true,
  "data": {
    "fileMap": {
      "photo.png":    "550e8400-e29b-41d4-a716-446655440000.png",
      "document.pdf": "6ba7b810-9dad-11d1-80b4-00c04fd430c8.pdf"
    },
    "fileStatus": {
      "photo.png":    true,
      "document.pdf": false
    }
  }
}
```

| Field | Type | Description |
| :--- | :--- | :--- |
| `fileMap` | `object` | Maps each successfully saved original filename to its server-assigned UUID filename under `/static/`. Only contains entries for files that were written successfully |
| `fileStatus` | `object` | Maps every submitted original filename to `true` (saved) or `false` (failed or skipped) |

#### Error responses

| Code | Condition |
| :--- | :--- |
| `400` | `files` field is missing or not an array |
| `401` | `userId` or `sessionToken` not provided |
| `403` | Invalid or expired session token |
| `413` | Request body exceeds 16 MB |

---

## Privilege flags

Privileges are stored as integer bitflags. General privileges apply system-wide; club privileges apply per-club.

| Constant | Value | Description |
| :--- | :--- | :--- |
| `BANNED_USER` | `0` (`0b0000`) | Account is banned. Cannot log in or perform any actions |
| `NORMAL_USER` | `1` (`0b0001`) | Standard member |
| `ADMIN` | `8` (`0b1000`) | Full system access including banning users, clubs, and events |

Flags are bitwise-combined. For example, a user who is both an admin and a normal user has privilege `9` (`0b1001`).

---

## Code example

```java
// Log in
JSONObject loginRequest = new JSONObject();
loginRequest.put("action",   "login");
loginRequest.put("email",    "student@ug.bilkent.edu.tr");
loginRequest.put("password", "bilkent_password");
Response loginResponse = RequestManager.sendPostRequest("api/user", loginRequest);

String token  = loginResponse.getPayload().getString("sessionToken");
int    userId = loginResponse.getPayload().getInt("userId");

// Search for clubs
JSONObject searchRequest = new JSONObject();
searchRequest.put("action",       "search");
searchRequest.put("userId",       userId);
searchRequest.put("sessionToken", token);
searchRequest.put("query",        "robotics");
Response searchResponse = RequestManager.sendPostRequest("api/club", searchRequest);

// Upload a profile picture
JSONObject uploadRequest = new JSONObject();
uploadRequest.put("action",       "upload");
uploadRequest.put("userId",       userId);
uploadRequest.put("sessionToken", token);
Response uploadResponse = RequestManager.uploadFile(uploadRequest, new File("photo.png"));

String storedName = uploadResponse.getPayload()
    .getJSONObject("fileMap")
    .getString("photo.png");

// Update profile with the uploaded picture
JSONObject updateRequest = new JSONObject();
updateRequest.put("action",         "updateProfile");
updateRequest.put("userId",         userId);
updateRequest.put("sessionToken",   token);
updateRequest.put("profilePicture", storedName);
RequestManager.sendPostRequest("api/user", updateRequest);
```
