import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

import org.json.JSONObject;

/**
 * Events tab — createEvent / modifyEvent / registerEvent / leaveEvent.
 *
 * createEvent  → POST api/event action="create"   (auth required, club ADMIN only)
 * modifyEvent  → POST api/event action="modify"    (auth required, club ADMIN only)
 * registerEvent→ POST api/event action="register"  (auth required)
 * leaveEvent   → POST api/event action="leave"     (auth required)
 */
public class EventsTab extends JPanel {

    private final SessionState session;
    private final TabContext ctx;

    public EventsTab(SessionState session, TabContext ctx) {
        super(new GridLayout(1, 4, 8, 0));
        setBorder(new EmptyBorder(8, 8, 8, 8));
        this.session = session;
        this.ctx = ctx;
        build();
    }

    private void build() {
        add(buildCreatePanel());
        add(buildModifyPanel());
        add(buildRegisterPanel());
        add(buildLeavePanel());
    }

    // ════════════════════════════════════════════════════════════════════════
    // CREATE EVENT (ADMIN only)
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildCreatePanel() {
        JTextField clubIdField     = TabHelpers.hint("clubId (integer)");
        JTextField nameField       = TabHelpers.hint("Spring Workshop");
        JTextField descField       = TabHelpers.hint("A hands-on workshop...");
        JTextField locationField   = TabHelpers.hint("Bilkent B-101");
        JTextField startEpochField = TabHelpers.hint("startEpoch (seconds)");
        JTextField endEpochField   = TabHelpers.hint("endEpoch (seconds)");
        JTextField quotaField      = TabHelpers.hint("quota (optional)");

        // Poster file picker
        JLabel posterLabel = new JLabel("No file selected");
        posterLabel.setForeground(Color.GRAY);
        final File[] selectedPoster = {null};

        JButton choosePosterBtn = new JButton("Choose Poster…");
        choosePosterBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileFilter(new FileNameExtensionFilter(
                    "Image files (png, jpg, jpeg, gif)", "png", "jpg", "jpeg", "gif"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedPoster[0] = chooser.getSelectedFile();
                posterLabel.setText(selectedPoster[0].getName());
                posterLabel.setForeground(new Color(0, 100, 180));
            }
        });

        JPanel posterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        posterRow.add(choosePosterBtn);
        posterRow.add(posterLabel);

        JPanel panel = TabHelpers.form("Create Event (ADMIN)",
                "Club ID",     clubIdField,
                "Event Name",  nameField,
                "Description", descField,
                "Location",    locationField,
                "Start Epoch", startEpochField,
                "End Epoch",   endEpochField,
                "Quota",       quotaField,
                "Poster",      posterRow);

        TabHelpers.addButton(panel, "Create Event", Color.decode("#2e7d32"), () -> {
            String cid = TabHelpers.text(clubIdField);
            if (cid.isEmpty()) { ctx.showError("Club ID cannot be empty."); return; }
            String name = TabHelpers.text(nameField);
            if (name.isEmpty()) { ctx.showError("Event name cannot be empty."); return; }
            String desc = TabHelpers.text(descField);
            String loc  = TabHelpers.text(locationField);
            String se   = TabHelpers.text(startEpochField);
            String ee   = TabHelpers.text(endEpochField);
            if (se.isEmpty() || ee.isEmpty()) { ctx.showError("Start and End epoch are required."); return; }

            try {
                int clubId = Integer.parseInt(cid);
                long startEpoch = Long.parseLong(se);
                long endEpoch   = Long.parseLong(ee);

                // Step 1: Upload poster if selected
                String posterFilename = null;
                if (selectedPoster[0] != null) {
                    ctx.log("» Uploading poster image first via api/upload...");
                    JSONObject uploadBody = session.authAction("upload");
                    Response uploadResp = RequestManager.uploadFile(uploadBody, selectedPoster[0]);
                    ctx.logResponse("api/upload", uploadResp);

                    if (uploadResp != null && uploadResp.isSuccess()) {
                        JSONObject payload = uploadResp.getPayload();
                        if (payload != null && payload.has("fileMap")) {
                            JSONObject fileMap = payload.getJSONObject("fileMap");
                            // The fileMap maps original filename → stored filename
                            String originalName = selectedPoster[0].getName();
                            if (fileMap.has(originalName)) {
                                posterFilename = fileMap.getString(originalName);
                                ctx.log("» Poster uploaded as: " + posterFilename);
                            }
                        }
                    } else {
                        ctx.log("» Poster upload failed — creating event without poster.");
                    }
                }

                // Step 2: Create the event
                JSONObject body = session.authAction("create");
                body.put("clubId", clubId);
                body.put("name", name);
                body.put("description", desc);
                body.put("location", loc);
                body.put("startEpoch", startEpoch);
                body.put("endEpoch", endEpoch);

                String quota = TabHelpers.text(quotaField);
                if (!quota.isEmpty()) {
                    body.put("quota", Integer.parseInt(quota));
                }
                if (posterFilename != null) {
                    body.put("posterFilename", posterFilename);
                }

                ctx.send("api/event", body);
            } catch (NumberFormatException ex) {
                ctx.showError("Club ID, epochs, and quota must be valid numbers.");
            }
        });

        return panel;
    }

    // ════════════════════════════════════════════════════════════════════════
    // MODIFY EVENT (ADMIN only)
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildModifyPanel() {
        JTextField eventIdField    = TabHelpers.hint("eventId (integer)");
        JTextField nameField       = TabHelpers.hint("(optional) new name");
        JTextField descField       = TabHelpers.hint("(optional) new desc");
        JTextField locationField   = TabHelpers.hint("(optional) new location");
        JTextField startEpochField = TabHelpers.hint("(optional) startEpoch");
        JTextField endEpochField   = TabHelpers.hint("(optional) endEpoch");
        JTextField quotaField      = TabHelpers.hint("(optional) quota");

        // Poster replacement picker
        JLabel posterLabel = new JLabel("No file selected");
        posterLabel.setForeground(Color.GRAY);
        final File[] selectedPoster = {null};

        JButton choosePosterBtn = new JButton("New Poster…");
        choosePosterBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(false);
            chooser.setFileFilter(new FileNameExtensionFilter(
                    "Image files (png, jpg, jpeg, gif)", "png", "jpg", "jpeg", "gif"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedPoster[0] = chooser.getSelectedFile();
                posterLabel.setText(selectedPoster[0].getName());
                posterLabel.setForeground(new Color(0, 100, 180));
            }
        });

        JPanel posterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        posterRow.add(choosePosterBtn);
        posterRow.add(posterLabel);

        JPanel panel = TabHelpers.form("Modify Event (ADMIN)",
                "Event ID",    eventIdField,
                "Name",        nameField,
                "Description", descField,
                "Location",    locationField,
                "Start Epoch", startEpochField,
                "End Epoch",   endEpochField,
                "Quota",       quotaField,
                "Poster",      posterRow);

        TabHelpers.addButton(panel, "Modify Event", Color.decode("#e65100"), () -> {
            String eid = TabHelpers.text(eventIdField);
            if (eid.isEmpty()) { ctx.showError("Event ID cannot be empty."); return; }

            try {
                int eventId = Integer.parseInt(eid);

                // Step 1: Upload new poster if selected
                String posterFilename = null;
                if (selectedPoster[0] != null) {
                    ctx.log("» Uploading new poster image via api/upload...");
                    JSONObject uploadBody = session.authAction("upload");
                    Response uploadResp = RequestManager.uploadFile(uploadBody, selectedPoster[0]);
                    ctx.logResponse("api/upload", uploadResp);

                    if (uploadResp != null && uploadResp.isSuccess()) {
                        JSONObject payload = uploadResp.getPayload();
                        if (payload != null && payload.has("fileMap")) {
                            JSONObject fileMap = payload.getJSONObject("fileMap");
                            String originalName = selectedPoster[0].getName();
                            if (fileMap.has(originalName)) {
                                posterFilename = fileMap.getString(originalName);
                                ctx.log("» Poster uploaded as: " + posterFilename);
                            }
                        }
                    } else {
                        ctx.log("» Poster upload failed — modifying event without poster change.");
                    }
                }

                // Step 2: Build the modify request with only non-empty fields
                JSONObject body = session.authAction("modify");
                body.put("eventId", eventId);

                String name = TabHelpers.text(nameField);
                if (!name.isEmpty()) body.put("name", name);

                String desc = TabHelpers.text(descField);
                if (!desc.isEmpty()) body.put("description", desc);

                String loc = TabHelpers.text(locationField);
                if (!loc.isEmpty()) body.put("location", loc);

                String se = TabHelpers.text(startEpochField);
                String ee = TabHelpers.text(endEpochField);
                if (!se.isEmpty() && !ee.isEmpty()) {
                    body.put("startEpoch", Long.parseLong(se));
                    body.put("endEpoch", Long.parseLong(ee));
                } else if (!se.isEmpty() || !ee.isEmpty()) {
                    ctx.showError("Both startEpoch and endEpoch must be provided together.");
                    return;
                }

                String quota = TabHelpers.text(quotaField);
                if (!quota.isEmpty()) body.put("quota", Integer.parseInt(quota));

                if (posterFilename != null) body.put("posterFilename", posterFilename);

                ctx.send("api/event", body);
            } catch (NumberFormatException ex) {
                ctx.showError("Event ID, epochs, and quota must be valid numbers.");
            }
        });

        return panel;
    }

    // ════════════════════════════════════════════════════════════════════════
    // REGISTER FOR EVENT
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildRegisterPanel() {
        JTextField eventIdField = TabHelpers.hint("eventId (integer)");
        JPanel panel = TabHelpers.form("Register for Event",
                "Event ID", eventIdField);

        TabHelpers.addButton(panel, "Register", Color.decode("#1565c0"), () -> {
            String eid = TabHelpers.text(eventIdField);
            if (eid.isEmpty()) { ctx.showError("Event ID cannot be empty."); return; }
            try {
                JSONObject body = session.authAction("register");
                body.put("eventId", Integer.parseInt(eid));
                ctx.send("api/event", body);
            } catch (NumberFormatException e) {
                ctx.showError("Event ID must be an integer.");
            }
        });

        return panel;
    }

    // ════════════════════════════════════════════════════════════════════════
    // LEAVE EVENT
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildLeavePanel() {
        JTextField eventIdField = TabHelpers.hint("eventId (integer)");
        JPanel panel = TabHelpers.form("Leave Event",
                "Event ID", eventIdField);

        TabHelpers.addButton(panel, "Leave Event", Color.decode("#c62828"), () -> {
            String eid = TabHelpers.text(eventIdField);
            if (eid.isEmpty()) { ctx.showError("Event ID cannot be empty."); return; }
            try {
                JSONObject body = session.authAction("leave");
                body.put("eventId", Integer.parseInt(eid));
                ctx.send("api/event", body);
            } catch (NumberFormatException e) {
                ctx.showError("Event ID must be an integer.");
            }
        });

        return panel;
    }
}
