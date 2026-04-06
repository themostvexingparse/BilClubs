import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;

public class APIHandler {
    private static final HTMLTemplate welcomeTemplate = new HTMLTemplate("templates/welcome.html");
    private static final HTMLTemplate eventCreatedTemplate = new HTMLTemplate("templates/eventCreated.html");
    private static final HTMLTemplate eventRegisteredTemplate = new HTMLTemplate("templates/eventRegistered.html");
    private static final HTMLTemplate eventLeftTemplate = new HTMLTemplate("templates/eventLeft.html");
    private static final HTMLTemplate clubJoinedTemplate = new HTMLTemplate("templates/clubJoined.html");
    private static final HTMLTemplate clubLeftTemplate = new HTMLTemplate("templates/clubLeft.html");

    private static final ExecutorService concurrentExecutor = Executors.newCachedThreadPool();
    private static final Credentials credentials = new Credentials(System.getenv());
    public static DBManager manager = new DBManager();
    private static MailSession session = new MailSession(credentials);
    private static final Base64.Decoder base64Decoder = Base64.getDecoder();
    private static final List<String> allowedExtensions = List.of("png", "jpg", "jpeg", "pdf", "gif");

    public static void initializeDB() {
        manager.initialize("db");
    }

    private static JSONObject buildResponse(int code, JSONObject data, String errorMessage) {
        JSONObject response = new JSONObject();
        response.put("responseCode", code);
        response.put("success", code >= 200 && code < 300);

        if (data != null) {
            response.put("data", data);
        }

        if (errorMessage != null) {
            JSONObject errorObj = new JSONObject();
            errorObj.put("message", errorMessage);
            response.put("error", errorObj);
        }

        return response;
    }

    private static class AuthResult {
        User user;
        JSONObject errorResponse;
    }

    private static AuthResult authenticate(JSONObject requestBody) {
        AuthResult authResult = new AuthResult();
        if (!requestBody.has("userId") || !requestBody.has("sessionToken")) {
            authResult.errorResponse = buildResponse(401, null,
                    "Missing credentials: userId or sessionToken not provided.");
            return authResult;
        }

        Integer userId = requestBody.optInt("userId", 0);
        String sessionToken = requestBody.optString("sessionToken", null);

        Filter userFilter = new Filter();
        userFilter.addFilter("id", userId);
        User user = manager.queryUser(userFilter);

        if (user == null) {
            authResult.errorResponse = buildResponse(403, null, "User does not exist.");
            return authResult;
        }

        if (!user.validateToken(sessionToken)) {
            authResult.errorResponse = buildResponse(403, null, "Invalid credentials.");
            return authResult;
        }

        authResult.user = user;
        return authResult;
    }

    private static JSONObject handleUserAction(String action, JSONObject requestBody) {
        switch (action) {
            case "signup":
                return signup(requestBody);
            case "login":
                return login(requestBody);
            case "logout":
            case "joinClub":
            case "getProfile":
            case "getForeignProfile":
            case "getForeignProfileClubs":
            case "getUpcomingEvents":
            case "updateProfile":
            case "setInterests":
            case "generateEmbeddings":
            case "listClubs":
            case "upload": {
                AuthResult authResult = authenticate(requestBody);
                if (authResult.errorResponse != null)
                    return authResult.errorResponse;
                return handleAuthedUserAction(action, authResult.user, requestBody);
            }
            default:
                return buildResponse(400, null, "Unsupported user action.");
        }
    }

    private static JSONObject handleUploadAction(String action, JSONObject requestBody) {
        AuthResult authResult = authenticate(requestBody);
        if (authResult.errorResponse != null)
            return authResult.errorResponse;
        switch (action) {
            case "upload":
                return uploadFiles(authResult.user, requestBody);
            default:
                return buildResponse(400, null, "Unsupported upload action.");
        }
    }

    private static JSONObject handleAuthedUserAction(String action, User user, JSONObject requestBody) {
        switch (action) {
            case "logout":
                return logout(user, requestBody);
            case "joinClub":
                return joinClub(user, requestBody);
            case "getProfile":
                return getProfile(user, requestBody);
            case "getForeignProfile":
                return getForeignProfile(requestBody);
            case "getForeignProfileClubs":
                return getForeignProfileClubs(requestBody);
            case "getUpcomingEvents":
                return getUpcomingEvents(user, requestBody);
            case "updateProfile":
                return updateProfile(user, requestBody);
            case "setInterests":
                return setInterests(user, requestBody);
            case "generateEmbeddings":
                return generateEmbeddings(user, requestBody);
            case "listClubs":
                return listClubs(requestBody);
            default:
                return buildResponse(400, null, "Unsupported user action.");
        }
    }

    private static JSONObject handleClubAction(String action, JSONObject requestBody) {
        AuthResult authResult = authenticate(requestBody);
        if (authResult.errorResponse != null)
            return authResult.errorResponse;

        switch (action) {
            case "create":
                return createClub(authResult.user, requestBody);
            case "getMembers":
                return getMembersOfClub(requestBody);
            case "search":
                return searchClubs(requestBody);
            default:
                return buildResponse(400, null, "Unsupported club action.");
        }
    }

    private static JSONObject signup(JSONObject requestBody) {
        String email = requestBody.optString("email", null);
        String password = requestBody.optString("password", null);
        String firstName = requestBody.optString("firstName", null);
        String lastName = requestBody.optString("lastName", null);
        String major = requestBody.optString("major", null);

        if (email == null || password == null || firstName == null || lastName == null) {
            return buildResponse(400, null, "Missing required fields: email, password, firstName, or lastName");
        }

        Filter emailFilter = new Filter();
        emailFilter.addFilter("email", email);
        if (manager.queryUser(emailFilter) != null) {
            return buildResponse(400, null, "Account already exists with the same email.");
        }

        boolean verified;
        try {
            verified = LoginVerifier.verify(email, password);
        } catch (IOException e) {
            return buildResponse(500, null, "Credential verification service error.");
        }
        if (!verified) {
            return buildResponse(400, null, "Incorrect email or password. Verification failed.");
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        if (major != null)
            newUser.setMajor(major);

        if (!manager.addUserUnsafe(newUser)) {
            return buildResponse(500, null, "Unknown database error.");
        }

        HTMLTemplate welcomeMessage = welcomeTemplate.formatted("name", newUser.getFullName());
        MailMessage message = new MailMessage();
        message.setSubject("Welcome to Bil'Clubs");
        message.fromTemplate(welcomeMessage);
        message.addRecipient(newUser.getEmail());
        MailTask mailTask = session.getTask(message);
        if (mailTask != null)
            concurrentExecutor.submit(mailTask);

        JSONObject data = new JSONObject();
        data.put("email", email);
        data.put("fullName", firstName + " " + lastName);
        return buildResponse(200, data, null);
    }

    private static JSONObject login(JSONObject requestBody) {
        String email = requestBody.optString("email", null);
        String password = requestBody.optString("password", null);

        if (email == null || password == null) {
            return buildResponse(400, null, "Missing required fields: email or password");
        }

        Filter emailFilter = new Filter();
        emailFilter.addFilter("email", email);
        User userByEmail = manager.queryUser(emailFilter);

        if (userByEmail == null) {
            return buildResponse(404, null, "Account does not exist.");
        }

        if (userByEmail.isBanned()) {
            return buildResponse(403, null, "Account is banned.");
        }

        boolean verified;
        try {
            verified = LoginVerifier.verify(email, password);
        } catch (IOException e) {
            return buildResponse(500, null, "Credential verification service error.");
        }
        if (!verified) {
            return buildResponse(401, null, "Incorrect email or password. Verification failed.");
        }

        userByEmail.generateToken();
        manager.updateUser(userByEmail);
        JSONObject data = new JSONObject();
        data.put("sessionToken", userByEmail.getToken());
        data.put("userId", userByEmail.getId());
        return buildResponse(200, data, null);
    }

    private static JSONObject logout(User user, JSONObject requestBody) {
        user.clearToken();
        manager.updateUser(user);
        return buildResponse(200, null, null);
    }

    private static JSONObject joinClub(User user, JSONObject requestBody) {
        Integer clubId = requestBody.optIntegerObject("clubId", null);
        JSONObject data = new JSONObject();
        if (clubId == null) {
            return buildResponse(400, null, "clubId cannot be empty.");
        } else {
            Filter clubFilter = new Filter();
            clubFilter.addFilter("id", clubId);
            Club club = manager.queryClub(clubFilter);
            if (club == null) {
                return buildResponse(400, null, "No club found with the given clubId.");
            }
            club.addMember(user);
            user.joinClub(club);
            manager.updateUser(user);
            manager.updateClub(club);
            if (user.wantToRecieveMails()) {
                HashMap<String, String> formatMap = new HashMap<>();
                formatMap.put("name", user.getFullName());
                formatMap.put("club_name", club.getClubName());
                formatMap.put("join_date", LocalDateTime.now(ZoneOffset.UTC).toString());
                HTMLTemplate clubJoinedMessage = clubJoinedTemplate.formatted(formatMap);
                MailMessage joinMessage = new MailMessage();
                joinMessage.setSubject("You joined " + club.getClubName() + "!");
                joinMessage.fromTemplate(clubJoinedMessage);
                joinMessage.addRecipient(user.getEmail());
                MailTask joinMailTask = session.getTask(joinMessage);
                if (joinMailTask != null)
                    concurrentExecutor.submit(joinMailTask);
            }
            return buildResponse(200, data, null);
        }
    }

    private static JSONObject getProfile(User user, JSONObject requestBody) {
        JSONObject data = new JSONObject();
        data.put("userId", user.getId());
        data.put("email", user.getEmail());
        data.put("firstName", user.getFirstName());
        data.put("lastName", user.getLastName());
        data.put("major", user.getMajor());
        data.put("profilePicture", user.getProfilePicture());
        data.put("interests", new JSONArray(user.getInterests()));
        data.put("clubPrivileges", new JSONObject(user.getClubPrivileges()));
        return buildResponse(200, data, null);
    }

    private static JSONObject getForeignProfile(JSONObject requestBody) {
        Integer userId = requestBody.optInt("targetUserId", -1);
        if (userId == -1) {
            return buildResponse(400, null, "targetUserId cannot be empty.");
        }
        Filter userIdFilter = new Filter();
        userIdFilter.addFilter("id", userId);
        User targetUser = manager.queryUser(userIdFilter);
        if (targetUser == null) {
            return buildResponse(404, null, "No user found with the given userId.");
        }
        JSONObject data = new JSONObject();
        data.put("userId", targetUser.getId());
        data.put("fullName", targetUser.getFullName());
        data.put("major", targetUser.getMajor());
        data.put("interests", new JSONArray(targetUser.getInterests()));
        data.put("clubPrivileges", new JSONObject(targetUser.getClubPrivileges()));
        return buildResponse(200, data, null);
    }

    private static JSONObject getForeignProfileClubs(JSONObject requestBody) {
        Integer userId = requestBody.optInt("targetUserId", -1);
        if (userId == -1) {
            return buildResponse(400, null, "targetUserId cannot be empty.");
        }
        Filter userIdFilter = new Filter();
        userIdFilter.addFilter("id", userId);
        User targetUser = manager.queryUser(userIdFilter);
        if (targetUser == null) {
            return buildResponse(404, null, "No user found with the given userId.");
        }
        ArrayList<Integer> clubIds = targetUser.getClubIds();
        JSONArray clubDatas = new JSONArray();
        for (Integer clubId : clubIds) {
            Filter clubFilter = new Filter();
            clubFilter.addFilter("id", clubId);
            Club club = manager.queryClub(clubFilter);
            JSONObject clubData = new JSONObject();
            clubData.put("id", club.getId());
            clubData.put("name", club.getClubName());
            clubData.put("icon", club.getIconFilename());
            clubData.put("description", club.getClubDescription());
            clubDatas.put(clubData);
        }
        JSONObject data = new JSONObject();
        data.put("clubs", clubDatas);
        return buildResponse(200, data, null);
    }

    private static JSONObject getUpcomingEvents(User user, JSONObject requestBody) {
        Long epochNow = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        Long epoch = requestBody.optLong("upToEpoch", Long.MAX_VALUE);
        Boolean userSpecific = requestBody.optBoolean("userSpecific", false);
        JSONArray clubIds = requestBody.optJSONArray("clubIds", new JSONArray());
        ArrayList<Integer> clubIdsList = new ArrayList<>();
        for (int i = 0; i < clubIds.length(); i++) {
            clubIdsList.add(clubIds.getInt(i));
        }
        Filter eventFilter = new Filter();
        eventFilter.addFilter("startDate", epochNow);
        eventFilter.addFilter("endDate", epoch);
        JSONArray eventDatas = new JSONArray();
        List<Event> events = manager.queryEvents(eventFilter);
        for (Event event : events) {
            if (userSpecific && !user.isRegisteredToEvent(event))
                continue;
            Filter clubFilter = new Filter();
            clubFilter.addFilter("id", event.getClubId());
            Club eventClub = manager.queryClub(clubFilter);
            if (!clubIdsList.isEmpty() && !clubIdsList.contains(eventClub.getId()))
                continue;
            JSONObject eventObject = new JSONObject();
            eventObject.put("name", event.getEventName());
            eventObject.put("description", event.getDescription());
            eventObject.put("quota", event.getQuota());
            eventObject.put("registreeCount", event.getRegistreeCount());
            eventObject.put("location", event.getLocation());
            eventObject.put("startDate", event.getStart().toString());
            eventObject.put("endDate", event.getEnd().toString());
            eventObject.put("GE250", event.getGE250());
            eventObject.put("posterImage", event.getPoster());
            eventObject.put("clubName", eventClub != null ? eventClub.getClubName() : "");
            eventObject.put("clubId", event.getClubId());
            eventObject.put("eventId", event.getId());
            eventDatas.put(eventObject);
        }

        JSONObject data = new JSONObject();
        data.put("events", eventDatas);
        return

        buildResponse(200, data, null);
    }

    private static JSONObject updateProfile(User user, JSONObject requestBody) {
        if (requestBody.has("firstName")) {
            String firstName = requestBody.optString("firstName", "").trim();
            if (firstName.isEmpty())
                return buildResponse(400, null, "firstName cannot be empty.");
            user.setFirstName(firstName);
        }
        if (requestBody.has("lastName")) {
            String lastName = requestBody.optString("lastName", "").trim();
            if (lastName.isEmpty())
                return buildResponse(400, null, "lastName cannot be empty.");
            user.setLastName(lastName);
        }
        if (requestBody.has("major")) {
            String major = requestBody.optString("major", "").trim();
            if (major.isEmpty())
                return buildResponse(400, null, "major cannot be empty.");
            user.setMajor(major);
        }
        if (requestBody.has("interests")) {
            JSONArray interests = requestBody.optJSONArray("interests", new JSONArray());
            ArrayList<String> interestsList = new ArrayList<>();
            for (Object interest : interests) {
                interestsList.add(interest.toString().trim());
            }
            user.setInterests(interestsList);
        }

        if (requestBody.has("profilePicture")) {
            String profilePicture = requestBody.optString("profilePicture", "").trim();
            if (profilePicture.isEmpty())
                return buildResponse(400, null, "profilePicture cannot be empty.");
            user.setProfilePicture("static/" + profilePicture);
        }

        manager.updateUser(user);
        return buildResponse(200, null, null);
    }

    private static JSONObject setInterests(User user, JSONObject requestBody) {
        JSONArray interests = requestBody.optJSONArray("interests", new JSONArray());
        ArrayList<String> interestsList = new ArrayList<>();
        for (Object interest : interests) {
            interestsList.add(interest.toString().trim());
        }
        user.setInterests(interestsList);
        manager.updateUser(user);
        return buildResponse(200, null, null);
    }

    private static JSONObject generateEmbeddings(User user, JSONObject requestBody) {
        EmbeddingsTask embeddingsTask = new EmbeddingsTask(user);
        concurrentExecutor.submit(embeddingsTask);
        return buildResponse(200, null, null);
    }

    private static JSONObject listClubs(JSONObject requestBody) {
        JSONArray clubArray = new JSONArray();
        List<Club> clubs = manager.queryClubs(new Filter());
        for (Club club : clubs) {
            JSONObject clubJson = new JSONObject();
            clubJson.put("id", club.getId());
            clubJson.put("clubName", club.getClubName());
            clubJson.put("clubDescription", club.getClubDescription());
            clubArray.put(clubJson);
        }

        JSONObject data = new JSONObject();
        data.put("clubs", clubArray);
        return buildResponse(200, data, null);
    }

    private static JSONObject createClub(User user, JSONObject requestBody) {
        if (!user.hasGeneralPrivilege(Privileges.MANAGER) && !user.hasGeneralPrivilege(Privileges.ADMIN)) {
            return buildResponse(401, null, "Not authorized to create a new club.");
        }

        String clubName = requestBody.optString("clubName", null);
        String clubDescription = requestBody.optString("clubDescription", null);
        if (clubName == null || clubDescription == null) {
            return buildResponse(400, null, "Malformed JSON request body.");
        }

        Club newClub = new Club(clubName, clubDescription);
        if (!manager.addClub(newClub)) {
            return buildResponse(500, null, "Club could not be created due to a database error.");
        }

        newClub.addMember(user);
        user.joinClub(newClub);
        user.setClubPrivilege(newClub, Privileges.ADMIN);
        newClub.setMemberPrivilege(user, Privileges.ADMIN);

        boolean updatedClub = manager.updateClub(newClub);
        boolean updatedUser = manager.updateUser(user);
        if (!updatedClub || !updatedUser) {
            return buildResponse(500, null, "Club was created but membership information could not be persisted.");
        }

        JSONObject data = new JSONObject();
        data.put("clubId", newClub.getId());
        data.put("clubName", newClub.getClubName());
        return buildResponse(200, data, null);
    }

    public static JSONObject getMembersOfClub(JSONObject requestBody) {
        Integer clubId = requestBody.optInt("clubId", -1);
        if (clubId == -1) {
            return buildResponse(400, null, "clubId cannot be empty.");
        }
        Filter filter = new Filter();
        filter.addFilter("id", clubId);
        Club club = manager.queryClub(filter);
        if (club == null) {
            return buildResponse(404, null, "Club not found.");
        }
        JSONArray members = new JSONArray();
        for (User member : club.getMembers()) {
            JSONObject memberJson = new JSONObject();
            // memberJson.put("email", member.getEmail()); // do not add email because we
            // wouldn't want stalking
            memberJson.put("id", member.getId());
            memberJson.put("name", member.getFirstName() + " " + member.getLastName());
            memberJson.put("privilege", member.getClubPrivileges().get(club.getId()));
            memberJson.put("profilePicture", member.getProfilePicture());
            members.put(memberJson);
        }
        JSONObject data = new JSONObject();
        data.put("members", members);
        return buildResponse(200, data, null);
    }

    private static JSONObject searchClubs(JSONObject requestBody) {
        String query = requestBody.optString("query", "").trim();
        if (query.isEmpty()) {
            return buildResponse(400, null, "query cannot be empty.");
        }

        List<Club> clubs = manager.queryClubs(new Filter());
        List<Map.Entry<Club, Double>> scored = new ArrayList<>();

        for (Club club : clubs) {
            double s = SearchScorer.score(query, club.getClubName(), club.getClubDescription());
            if (s > 0.0) {
                scored.add(Map.entry(club, s));
            }
        }

        scored.sort(Collections.reverseOrder(Map.Entry.comparingByValue()));

        JSONArray results = new JSONArray();
        for (int i = 0; i < scored.size(); i++) {
            Club club = scored.get(i).getKey();
            JSONObject item = new JSONObject();
            item.put("rank", i + 1);
            item.put("id", club.getId());
            item.put("name", club.getClubName());
            item.put("description", club.getClubDescription());
            item.put("icon", club.getIconFilename());
            results.put(item);
        }

        JSONObject data = new JSONObject();
        data.put("results", results);
        data.put("count", results.length());
        return buildResponse(200, data, null);
    }

    private static JSONObject uploadFiles(User user, JSONObject requestBody) {
        JSONObject data = new JSONObject();
        JSONObject fileMap = new JSONObject();
        JSONObject fileStatus = new JSONObject();

        JSONArray files = requestBody.optJSONArray("files");
        if (files == null) {
            return buildResponse(400, null, "Missing or invalid 'files' array in request body.");
        }

        for (int index = 0; index < files.length(); index++) {
            JSONObject file;
            String fileName;
            try {
                file = files.getJSONObject(index);
                fileName = file.getString("fileName");
            } catch (Exception e) {
                continue;
            }
            try {
                String fileDataBase64 = file.getString("fileData");
                byte[] fileData = base64Decoder.decode(fileDataBase64);
                String fileExtension = file.getString("fileType").trim().toLowerCase();
                if (!allowedExtensions.contains(fileExtension))
                    continue;
                String newFileName = UUID.randomUUID().toString() + "." + fileExtension;
                File newFile = new File("./static/" + newFileName);
                try (FileOutputStream outputStream = new FileOutputStream(newFile)) {
                    outputStream.write(fileData);
                    outputStream.close();
                    Media media = new Media();
                    media.setUserId(user.getId());
                    media.setRealFileName(fileName);
                    media.setStoredFileName(newFileName);
                    manager.addFile(media);
                    fileMap.put(fileName, newFileName);
                    fileStatus.put(fileName, true);
                } catch (Exception e) {
                    newFile.delete();
                    fileStatus.put(fileName, false);
                }
            } catch (Exception e) {
                fileStatus.put(fileName, false);
                continue;
            }
        }

        data.put("fileMap", fileMap);
        data.put("fileStatus", fileStatus);

        return buildResponse(200, data, null);
    }

    private static JSONObject handleEventAction(String action, JSONObject requestBody) {
        AuthResult authResult = authenticate(requestBody);
        if (authResult.errorResponse != null)
            return authResult.errorResponse;

        switch (action) {
            case "create":
                return createEvent(authResult.user, requestBody);
            case "modify":
                return modifyEvent(authResult.user, requestBody);
            case "register":
                return registerEvent(authResult.user, requestBody);
            case "leave":
                return leaveEvent(authResult.user, requestBody);
            case "search":
                return searchEvents(requestBody);
            default:
                return buildResponse(400, null, "Unsupported event action.");
        }
    }

    private static JSONObject createEvent(User user, JSONObject requestBody) {
        Integer clubId = requestBody.optIntegerObject("clubId", null);
        if (clubId == null) {
            return buildResponse(400, null, "clubId is required.");
        }

        Filter clubFilter = new Filter();
        clubFilter.addFilter("id", clubId);
        Club club = manager.queryClub(clubFilter);
        if (club == null) {
            return buildResponse(404, null, "Club not found.");
        }

        if (!user.hasClubPrivilege(club, Privileges.ADMIN)) {
            return buildResponse(403, null, "Only club admins can create events.");
        }

        String eventName = requestBody.optString("name", null);
        String description = requestBody.optString("description", null);
        String location = requestBody.optString("location", null);
        Integer GE250 = requestBody.optIntegerObject("GE250", null);
        Long startEpoch = requestBody.has("startEpoch") ? requestBody.getLong("startEpoch") : null;
        Long endEpoch = requestBody.has("endEpoch") ? requestBody.getLong("endEpoch") : null;

        if (eventName == null || description == null || location == null || startEpoch == null || endEpoch == null) {
            return buildResponse(400, null,
                    "Missing required fields: name, description, location, startEpoch, endEpoch.");
        }

        if (endEpoch <= startEpoch) {
            return buildResponse(400, null, "endEpoch must be after startEpoch.");
        }

        LocalDateTime start = LocalDateTime.ofInstant(Instant.ofEpochSecond(startEpoch), ZoneOffset.UTC);
        LocalDateTime end = LocalDateTime.ofInstant(Instant.ofEpochSecond(endEpoch), ZoneOffset.UTC);

        // quota is optional and null means unlimited
        Integer quota = requestBody.has("quota") ? requestBody.getInt("quota") : null;

        Event newEvent = new Event(eventName.trim(), club, description.trim(), location.trim(), start, end, quota);

        // poster image: client uploads via /api/upload first, then provides the stored
        // filename
        String posterFilename = requestBody.optString("posterFilename", null);
        if (posterFilename != null && !posterFilename.trim().isEmpty()) {
            newEvent.setPoster(posterFilename.trim());
        }

        if (GE250 != null) {
            newEvent.setGE250(GE250);
        }

        if (!manager.addEvent(newEvent)) {
            return buildResponse(500, null, "Event could not be created due to a database error.");
        }

        club.addEvent(newEvent);
        if (!manager.updateClub(club)) {
            return buildResponse(500, null, "Event was created but could not be linked to the club.");
        }

        // loop through all members of the club and send emails
        for (User member : club.getMembers()) {
            if (!member.wantToRecieveMails() || !member.wantToRecieveClubAndEventAlerts()) {
                continue;
            }
            HashMap<String, String> formatMap = new HashMap<>();
            formatMap.put("name", member.getFullName());
            formatMap.put("club_name", club.getClubName());
            formatMap.put("event_name", newEvent.getEventName());
            formatMap.put("event_date", newEvent.getStart().atZone(ZoneOffset.UTC).toString());
            formatMap.put("event_duration", DurationFormatter.format(newEvent.getStart(), newEvent.getEnd()));
            formatMap.put("event_location", newEvent.getLocation());
            formatMap.put("event_capacity", newEvent.getQuota().toString());
            formatMap.put("event_description", newEvent.getDescription());
            HTMLTemplate eventCreatedMessage = eventCreatedTemplate.formatted(formatMap);
            MailMessage message = new MailMessage();
            message.setSubject(club.getClubName() + " has posted a new event!");
            message.fromTemplate(eventCreatedMessage);
            message.addRecipient(member.getEmail());
            MailTask mailTask = session.getTask(message);
            if (mailTask != null)
                concurrentExecutor.submit(mailTask);
        }

        JSONObject data = new JSONObject();
        data.put("eventId", newEvent.getId());
        data.put("eventName", newEvent.getEventName());
        data.put("clubId", club.getId());
        return buildResponse(200, data, null);
    }

    private static JSONObject modifyEvent(User user, JSONObject requestBody) {
        Integer eventId = requestBody.optIntegerObject("eventId", null);
        if (eventId == null) {
            return buildResponse(400, null, "eventId is required.");
        }

        Filter eventFilter = new Filter();
        eventFilter.addFilter("id", eventId);
        Event event = manager.queryEvent(eventFilter);
        if (event == null) {
            return buildResponse(404, null, "Event not found.");
        }

        Filter clubFilterForModify = new Filter();
        clubFilterForModify.addFilter("id", event.getClubId());
        Club club = manager.queryClub(clubFilterForModify);
        if (club == null) {
            return buildResponse(404, null, "The club associated with this event no longer exists.");
        }
        if (!user.hasClubPrivilege(club, Privileges.ADMIN)) {
            return buildResponse(403, null, "Only club admins can modify events.");
        }

        // partial updates, only modify fields that are present in the request
        if (requestBody.has("name")) {
            String name = requestBody.optString("name", "").trim();
            if (name.isEmpty())
                return buildResponse(400, null, "name cannot be empty.");
            event.setName(name);
        }
        if (requestBody.has("description")) {
            String description = requestBody.optString("description", "").trim();
            if (description.isEmpty())
                return buildResponse(400, null, "description cannot be empty.");
            event.setDescription(description);
        }
        if (requestBody.has("location")) {
            String location = requestBody.optString("location", "").trim();
            if (location.isEmpty())
                return buildResponse(400, null, "location cannot be empty.");
            event.setLocation(location);
        }
        if (requestBody.has("startEpoch") && requestBody.has("endEpoch")) {
            long startEpoch = requestBody.getLong("startEpoch");
            long endEpoch = requestBody.getLong("endEpoch");
            if (endEpoch <= startEpoch) {
                return buildResponse(400, null, "endEpoch must be after startEpoch.");
            }
            LocalDateTime start = LocalDateTime.ofInstant(Instant.ofEpochSecond(startEpoch), ZoneOffset.UTC);
            LocalDateTime end = LocalDateTime.ofInstant(Instant.ofEpochSecond(endEpoch), ZoneOffset.UTC);
            event.setStartAndEnd(start, end);
        }
        if (requestBody.has("quota")) {
            int quota = requestBody.getInt("quota");
            if (!event.setQuota(quota)) {
                return buildResponse(400, null,
                        "Quota cannot be less than the current number of registered users.");
            }
        }
        if (requestBody.has("GE250")) {
            int GE250 = requestBody.getInt("GE250");
            event.setGE250(GE250);
        }
        if (requestBody.has("posterFilename")) {
            String posterFilename = requestBody.optString("posterFilename", "").trim();
            if (!posterFilename.isEmpty()) {
                event.setPoster(posterFilename);
            }
        }

        if (!manager.updateEvent(event)) {
            return buildResponse(500, null, "Event could not be updated due to a database error.");
        }

        JSONObject data = new JSONObject();
        data.put("eventId", event.getId());
        data.put("eventName", event.getEventName());
        return buildResponse(200, data, null);
    }

    private static JSONObject registerEvent(User user, JSONObject requestBody) {
        Integer eventId = requestBody.optIntegerObject("eventId", null);
        if (eventId == null) {
            return buildResponse(400, null, "eventId is required.");
        }

        Filter eventFilter = new Filter();
        eventFilter.addFilter("id", eventId);
        Event event = manager.queryEvent(eventFilter);
        if (event == null) {
            return buildResponse(404, null, "Event not found.");
        }

        if (user.isRegisteredToEvent(event)) {
            return buildResponse(400, null, "You are already registered to this event.");
        }

        if (!user.canRegisterToEvent(event)) {
            return buildResponse(400, null,
                    "Cannot register: the event is full, closed, or conflicts with another registered event.");
        }

        if (!manager.addUserToEvent(user, event)) {
            return buildResponse(500, null, "Registration failed due to a database error.");
        }

        if (user.wantToRecieveClubAndEventAlerts()) {
            Filter clubFilterForEvent = new Filter();
            clubFilterForEvent.addFilter("id", event.getClubId());
            Club eventClub = manager.queryClub(clubFilterForEvent);

            HashMap<String, String> formatMap = new HashMap<>();
            formatMap.put("name", user.getFullName());
            formatMap.put("event_name", event.getEventName());
            formatMap.put("club_name", eventClub != null ? eventClub.getClubName() : "");
            formatMap.put("event_date", event.getStart().atZone(ZoneOffset.UTC).toString());
            formatMap.put("event_time", DurationFormatter.format(event.getStart(), event.getEnd()));
            formatMap.put("event_location", event.getLocation());

            HTMLTemplate eventRegisteredMessage = eventRegisteredTemplate.formatted(formatMap);
            MailMessage registerMessage = new MailMessage();
            registerMessage.setSubject("You're registered for " + event.getEventName() + "!");
            registerMessage.fromTemplate(eventRegisteredMessage);
            registerMessage.addRecipient(user.getEmail());
            MailTask registerMailTask = session.getTask(registerMessage);
            if (registerMailTask != null)
                concurrentExecutor.submit(registerMailTask);
        }

        JSONObject data = new JSONObject();
        data.put("eventId", event.getId());
        data.put("eventName", event.getEventName());
        data.put("registreeCount", event.getRegistreeCount());
        return buildResponse(200, data, null);
    }

    private static JSONObject leaveEvent(User user, JSONObject requestBody) {
        Integer eventId = requestBody.optIntegerObject("eventId", null);
        if (eventId == null) {
            return buildResponse(400, null, "eventId is required.");
        }

        Filter eventFilter = new Filter();
        eventFilter.addFilter("id", eventId);
        Event event = manager.queryEvent(eventFilter);
        if (event == null) {
            return buildResponse(404, null, "Event not found.");
        }

        if (!user.isRegisteredToEvent(event)) {
            return buildResponse(400, null, "You are not registered to this event.");
        }

        if (!manager.removeUserFromEvent(user, event)) {
            return buildResponse(500, null, "Could not leave the event due to a database error.");
        }

        if (user.wantToRecieveClubAndEventAlerts()) {
            Filter clubFilterForLeave = new Filter();
            clubFilterForLeave.addFilter("id", event.getClubId());
            Club eventClubForLeave = manager.queryClub(clubFilterForLeave);

            HashMap<String, String> formatMap = new HashMap<>();
            formatMap.put("name", user.getFullName());
            formatMap.put("event_name", event.getEventName());
            formatMap.put("club_name", eventClubForLeave != null ? eventClubForLeave.getClubName() : "");
            formatMap.put("event_date", event.getStart().atZone(ZoneOffset.UTC).toString());
            formatMap.put("event_time", DurationFormatter.format(event.getStart(), event.getEnd()));
            formatMap.put("event_location", event.getLocation());
            formatMap.put("cancellation_date", LocalDateTime.now(ZoneOffset.UTC).toString());

            HTMLTemplate eventLeftMessage = eventLeftTemplate.formatted(formatMap);
            MailMessage leaveEventMessage = new MailMessage();
            leaveEventMessage.setSubject("Registration cancelled: " + event.getEventName());
            leaveEventMessage.fromTemplate(eventLeftMessage);
            leaveEventMessage.addRecipient(user.getEmail());
            MailTask leaveEventMailTask = session.getTask(leaveEventMessage);
            if (leaveEventMailTask != null)
                concurrentExecutor.submit(leaveEventMailTask);
        }

        JSONObject data = new JSONObject();
        data.put("eventId", event.getId());
        data.put("eventName", event.getEventName());
        data.put("registreeCount", event.getRegistreeCount());
        return buildResponse(200, data, null);
    }

    private static JSONObject searchEvents(JSONObject requestBody) {
        String query = requestBody.optString("query", "").trim();
        if (query.isEmpty()) {
            return buildResponse(400, null, "query cannot be empty.");
        }

        Long epochNow = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        Filter eventFilter = new Filter();
        eventFilter.addFilter("startDate", epochNow);
        List<Event> events = manager.queryEvents(eventFilter);
        List<Map.Entry<Event, Double>> scored = new ArrayList<>();

        for (Event event : events) {
            double s = SearchScorer.score(query, event.getEventName(), event.getDescription());
            if (s > 0.0) {
                scored.add(Map.entry(event, s));
            }
        }

        scored.sort(Collections.reverseOrder(Map.Entry.comparingByValue()));

        JSONArray results = new JSONArray();
        for (int i = 0; i < scored.size(); i++) {
            Event event = scored.get(i).getKey();
            Filter cf = new Filter();
            cf.addFilter("id", event.getClubId());
            Club owningClub = manager.queryClub(cf);
            JSONObject item = new JSONObject();
            item.put("rank", i + 1);
            item.put("id", event.getId());
            item.put("name", event.getEventName());
            item.put("description", event.getDescription());
            item.put("location", event.getLocation());
            item.put("startDate", event.getStart().toString());
            item.put("clubId", event.getClubId());
            item.put("clubName", owningClub != null ? owningClub.getClubName() : "");
            results.put(item);
        }

        JSONObject data = new JSONObject();
        data.put("results", results);
        data.put("count", results.length());
        return buildResponse(200, data, null);
    }

    public static JSONObject handle(HttpExchange httpExchange) {
        String remoteAddress = httpExchange.getRemoteAddress().toString() + "/";
        String path = httpExchange.getRequestURI().getRawPath();

        if (!httpExchange.getRequestMethod().equals("POST")) {
            return buildResponse(400, null, "Method not allowed. Only POST is supported.");
        }

        JSONObject requestBody;
        try {
            requestBody = new JSONObject(StreamReader.readStream(httpExchange.getRequestBody()));
        } catch (IOException e) {
            return buildResponse(413, null,
                    "Request body is too large. Maximum allowed size is " + ServerConfig.MAX_REQUEST_BYTES + " bytes.");
        } catch (Exception e) {
            return buildResponse(400, null, "Malformed JSON request body.");
        }

        if (ServerConfig.PRINT_DEBUG)
            System.out.printf("%s : %s\n", remoteAddress, path);

        if (!path.startsWith("/api/")) {
            return buildResponse(501, null, "Not implemented.");
        }

        String[] pathParts = path.substring(1).split("/");
        if (pathParts.length < 2) {
            return buildResponse(400, null, "Invalid API endpoint");
        }

        String endpoint = pathParts[1];

        try {
            String action = requestBody.optString("action", "").trim();
            if (action.isEmpty()) {
                return buildResponse(400, null, "Missing required field: action");
            }

            if (endpoint.equals("user")) {
                return handleUserAction(action, requestBody);
            }

            if (endpoint.equals("upload")) {
                return handleUploadAction(action, requestBody);
            }

            if (endpoint.equals("club")) {
                return handleClubAction(action, requestBody);
            }

            if (endpoint.equals("event")) {
                return handleEventAction(action, requestBody);
            }

            return buildResponse(404, null,
                    "Endpoint not found. Supported endpoints are /api/user, /api/club, and /api/event.");

        } catch (Exception e) {
            if (ServerConfig.PRINT_STACK_TRACES)
                e.printStackTrace();
            return buildResponse(500, null, "Internal server error.");
        }
    }
}