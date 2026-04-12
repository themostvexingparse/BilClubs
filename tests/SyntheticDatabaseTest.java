import java.io.FileReader;
import java.io.BufferedReader;
import java.io.Console;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

public class SyntheticDatabaseTest {

    private static int userId;
    private static String sessionToken;

    private static JSONObject authBody(String action) {
        JSONObject body = new JSONObject();
        body.put("action", action);
        body.put("userId", userId);
        body.put("sessionToken", sessionToken);
        return body;
    }

    private static void log(String msg) {
        System.out.println(msg);
    }

    private static void fail(String msg) {
        System.err.println("[FATAL] " + msg);
        System.exit(1);
    }

    public static void main(String[] args) throws Exception {
        RequestManager.setDefaultAddress("http://127.0.0.1:5000");
        String email, password;
        if (args.length >= 2) {
            email = args[0];
            password = args[1];
        } else {
            Console console = System.console();
            if (console == null) {
                fail("No console available and no CLI args provided. Usage: java SyntheticDatabaseTest <email> <password>");
                return;
            }
            email = console.readLine("Admin email: ").trim();
            char[] pw = console.readPassword("Password: ");
            password = new String(pw).trim();
        }

        log("═".repeat(64));
        log("  BilClubs Synthetic Database Populator");
        log("═".repeat(64));

        log("\n[1/5] Logging in as " + email + "...");
        {
            JSONObject loginBody = new JSONObject();
            loginBody.put("action", "login");
            loginBody.put("email", email);
            loginBody.put("password", password);
            Response resp = RequestManager.sendPostRequest("api/user", loginBody);
            if (resp == null || resp.isNullResponse() || !resp.isSuccess()) {
                fail("Login failed: " + (resp != null ? resp.getErrorMessage() : "null response"));
            }
            userId = resp.getPayload().getInt("userId");
            sessionToken = resp.getPayload().getString("sessionToken");
            log("  ✓ Logged in as userId=" + userId);
        }

        log("\n[2/5] Fetching existing clubs...");
        Set<String> existingClubNames = new HashSet<>();
        Map<String, Integer> clubNameToId = new java.util.HashMap<>();
        {
            JSONObject listBody = authBody("listClubs");
            Response resp = RequestManager.sendPostRequest("api/user", listBody);
            if (resp != null && resp.isSuccess() && resp.getPayload().has("clubs")) {
                JSONArray clubs = resp.getPayload().getJSONArray("clubs");
                for (int i = 0; i < clubs.length(); i++) {
                    JSONObject c = clubs.getJSONObject(i);
                    String name = c.getString("clubName");
                    existingClubNames.add(name);
                    clubNameToId.put(name, c.getInt("id"));
                }
            }
            log("  Found " + existingClubNames.size() + " existing club(s).");
        }

        log("\n[3/5] Reading clubs_english.json and creating clubs...");
        JSONObject clubsJson;
        {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new FileReader("clubs_english.json", java.nio.charset.StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null)
                    sb.append(line);
            }
            clubsJson = new JSONObject(sb.toString());
        }

        int created = 0, skippedEmpty = 0, skippedExists = 0;
        ArrayList<String> createdClubNames = new ArrayList<>();

        for (String clubName : clubsJson.keySet()) {
            String description = clubsJson.optString(clubName, "").trim();
            if (description.isEmpty()) {
                skippedEmpty++;
                continue;
            }
            if (existingClubNames.contains(clubName)) {
                skippedExists++;
                continue;
            }

            JSONObject body = authBody("create");
            body.put("clubName", clubName);
            body.put("clubDescription", description);
            Response resp = RequestManager.sendPostRequest("api/club", body);
            if (resp != null && resp.isSuccess()) {
                int clubId = resp.getPayload().getInt("clubId");
                clubNameToId.put(clubName, clubId);
                existingClubNames.add(clubName);
                createdClubNames.add(clubName);
                created++;
                log("  ✓ Created club: " + clubName + " (id=" + clubId + ")");
                log("  (Sleeping 0.5s to avoid embedding rate limit)");
                Thread.sleep(500);
            } else {
                log("  ✗ Failed to create club: " + clubName +
                        (resp != null ? " — " + resp.getErrorMessage() : ""));
            }
        }
        log(String.format("  Summary: %d created, %d skipped (no description), %d skipped (already exists)",
                created, skippedEmpty, skippedExists));

        log("\n[4/5] Creating synthetic events...");

        String[][] syntheticEvents = {
                // { clubNameSubstring, eventName, description, location, daysFromNow,
                // durationHours, quota }
                { "ACM", "ACM Hackathon 2026",
                        "A 24-hour hackathon open to all skill levels. Build, learn, and compete for prizes.",
                        "Bilkent EE Building - B Block", "14", "24", "120" },
                { "IEEE", "IEEE Workshop: Intro to PCB Design",
                        "Learn how to design printed circuit boards from scratch using KiCad.", "EA-409", "7", "3",
                        "40" },
                { "Yapay Zekâ", "AI Paper Reading Group #5",
                        "Discussing the latest advances in transformer architectures and multi-modal models.",
                        "A Building - Room 130", "3", "2", "30" },
                { "Fotoğrafçılık", "Golden Hour Campus Photowalk",
                        "Capture the magic of Bilkent campus during golden hour. All skill levels welcome.",
                        "Main Campus Entrance", "10", "3", "50" },
                { "Astronomi", "Spring Night Sky Observation",
                        "Observe Jupiter, the Pleiades cluster, and the Orion Nebula through our telescopes.",
                        "Bilkent Dormitories Hill", "5", "4", "60" },
                { "Tiyatro", "Improv Night: Lights On!",
                        "An evening of improvisational theater games and short scenes. No experience needed.",
                        "FEASS Auditorium", "8", "2", "80" },
                { "Münazara", "Inter-University Debate Tournament",
                        "Compete in British Parliamentary format against teams from METU, Hacettepe, and Ankara University.",
                        "Bilkent Hotel - Conference Hall", "21", "8", "64" },
                { "Veri Bilimi", "Datathon: Predict Ankara Traffic",
                        "Use real city data to build models predicting traffic congestion. Cash prizes for top 3 teams.",
                        "Cyberpark - Co-Working Space", "18", "12", "100" },
                { "Blockchain", "Web3 Builder Night",
                        "A hands-on workshop building and deploying a smart contract on Ethereum testnets.",
                        "EE-05 Lab", "12", "4", "35" },
                { "Dans", "Latin Dance Workshop: Salsa & Bachata",
                        "A beginner-friendly workshop taught by professional instructors from Ankara Dance Academy.",
                        "Bilkent Gymnasium - Hall B", "6", "3", "45" },
                { "Satranç", "Bilkent Blitz Chess Tournament",
                        "5+0 blitz format, Swiss system, 7 rounds. Open to all Bilkent students.",
                        "Student Center - Room 201", "9", "5", "48" },
                { "Müzikal", "Spring Musical Auditions 2026",
                        "Open auditions for the spring musical production. Prepare a 2-minute monologue and a song.",
                        "FEASS Theater Stage", "4", "6", "0" },
                { "Elektrikli Araç", "EV Tech Talk: Battery Management Systems",
                        "Guest lecture by a Tesla engineer on modern BMS architecture and thermal management.",
                        "FA Building - Main Hall", "15", "2", "100" },
        };

        int eventsCreated = 0, eventsSkipped = 0;
        for (String[] ev : syntheticEvents) {
            String clubSubstring = ev[0];
            String eventName = ev[1];
            String eventDesc = ev[2];
            String eventLocation = ev[3];
            int daysFromNow = Integer.parseInt(ev[4]);
            int durationHours = Integer.parseInt(ev[5]);
            int quota = Integer.parseInt(ev[6]);

            Integer clubId = null;
            String matchedClubName = null;
            for (Map.Entry<String, Integer> entry : clubNameToId.entrySet()) {
                if (entry.getKey().contains(clubSubstring)) {
                    clubId = entry.getValue();
                    matchedClubName = entry.getKey();
                    break;
                }
            }

            if (clubId == null) {
                log("  ⚠ No club matching '" + clubSubstring + "' found. Skipping event: " + eventName);
                eventsSkipped++;
                continue;
            }

            long nowEpoch = System.currentTimeMillis() / 1000;
            long startEpoch = nowEpoch + (long) daysFromNow * 86400 + 36000;
            long endEpoch = startEpoch + (long) durationHours * 3600;

            JSONObject body = authBody("create");
            body.put("clubId", clubId);
            body.put("name", eventName);
            body.put("description", eventDesc);
            body.put("location", eventLocation);
            body.put("startEpoch", startEpoch);
            body.put("endEpoch", endEpoch);
            if (quota > 0) {
                body.put("quota", quota);
            }

            Response resp = RequestManager.sendPostRequest("api/event", body);
            if (resp != null && resp.isSuccess()) {
                int eventId = resp.getPayload().getInt("eventId");
                eventsCreated++;
                log("  ✓ Created event: " + eventName + " (id=" + eventId + ") under " + matchedClubName);
                log("  (Sleeping .5s to avoid embedding rate limit)");
                Thread.sleep(500);
            } else {
                log("  ✗ Failed to create event: " + eventName +
                        (resp != null ? " — " + resp.getErrorMessage() : ""));
                eventsSkipped++;
            }
        }
        log(String.format("  Summary: %d events created, %d skipped", eventsCreated, eventsSkipped));

        log("\n[5/5] Logging out...");
        {
            JSONObject body = authBody("logout");
            RequestManager.sendPostRequest("api/user", body);
        }

        log("\n" + "═".repeat(64));
        log("  Population complete!");
        log("  Clubs created:  " + created);
        log("  Events created: " + eventsCreated);
        log("═".repeat(64));
    }
}
