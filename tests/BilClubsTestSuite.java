import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * BilClubs API Test Suite — Swing UI
 *
 * Compile: javac -cp "server/lib/*:." BilClubsTestSuite.java
 * Run: java -cp "server/lib/*:." BilClubsTestSuite
 */
public class BilClubsTestSuite extends JFrame {

    // ── Shared session (populated on login, cleared on logout) ──────────────
    private final SessionState session = new SessionState();

    // ── Persistent widgets ──────────────────────────────────────────────────
    private final JTextField serverAddressField = new JTextField("http://127.0.0.1:5000", 28);
    private final JLabel sessionLabel = new JLabel("Not logged in");
    private final JTextArea responseArea = new JTextArea(14, 70);

    // ════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BilClubsTestSuite().setVisible(true));
    }

    public BilClubsTestSuite() {
        super("BilClubs API Test Suite");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(6, 6));
        add(buildTopBar(), BorderLayout.NORTH);
        add(buildTabs(), BorderLayout.CENTER);
        add(buildResponsePane(), BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
    }

    // ════════════════════════════════════════════════════════════════════════
    // TOP BAR
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildTopBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        p.setBorder(new TitledBorder("Connection & Session"));

        JButton applyBtn = new JButton("Apply");
        applyBtn.setToolTipText("Update RequestManager base address");
        applyBtn.addActionListener(e -> {
            RequestManager.setDefaultAddress(serverAddressField.getText().trim());
            log("» Base address set to: " + serverAddressField.getText().trim());
        });

        p.add(new JLabel("Server:"));
        p.add(serverAddressField);
        p.add(applyBtn);
        p.add(Box.createHorizontalStrut(16));

        sessionLabel.setFont(sessionLabel.getFont().deriveFont(Font.BOLD));
        sessionLabel.setForeground(Color.GRAY);
        p.add(new JLabel("Session:"));
        p.add(sessionLabel);

        JButton clearBtn = new JButton("Clear Session");
        clearBtn.addActionListener(e -> {
            session.clear();
            refreshSessionLabel();
        });
        p.add(clearBtn);

        // Set initial address
        RequestManager.setDefaultAddress(serverAddressField.getText().trim());
        return p;
    }

    // ════════════════════════════════════════════════════════════════════════
    // TABS
    // ════════════════════════════════════════════════════════════════════════
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Auth", buildAuthTab());
        tabs.addTab("Profile", buildProfileTab());
        tabs.addTab("Clubs", buildClubsTab());
        tabs.addTab("Upload", buildUploadTab());
        tabs.addTab("Misc", buildMiscTab());
        return tabs;
    }

    // ── AUTH (signup / login / logout) ─────────────────────────────────────
    private JPanel buildAuthTab() {
        JPanel outer = new JPanel(new GridLayout(1, 3, 8, 0));
        outer.setBorder(new EmptyBorder(8, 8, 8, 8));

        /* ── Signup ── */
        JTextField signupEmail = hint("student@ug.bilkent.edu.tr");
        JPasswordField signupPass = new JPasswordField(18);
        JTextField signupFirst = hint("Ada");
        JTextField signupLast = hint("Lovelace");
        JTextField signupMajor = hint("Computer Engineering  (optional)");

        JPanel signupPanel = form("Signup",
                "Email", signupEmail,
                "Password", signupPass,
                "First Name", signupFirst,
                "Last Name", signupLast,
                "Major", signupMajor);
        addButton(signupPanel, "Sign Up", Color.decode("#2e7d32"), () -> {
            JSONObject body = action("signup");
            body.put("email", text(signupEmail));
            body.put("password", new String(signupPass.getPassword()));
            body.put("firstName", text(signupFirst));
            body.put("lastName", text(signupLast));
            String major = text(signupMajor);
            if (!major.isEmpty())
                body.put("major", major);
            send("api/user", body);
        });
        outer.add(signupPanel);

        /* ── Login ── */
        JTextField loginEmail = hint("student@ug.bilkent.edu.tr");
        JPasswordField loginPass = new JPasswordField(18);

        JPanel loginPanel = form("Login",
                "Email", loginEmail,
                "Password", loginPass);
        addButton(loginPanel, "Log In", Color.decode("#1565c0"), () -> {
            JSONObject body = action("login");
            body.put("email", text(loginEmail));
            body.put("password", new String(loginPass.getPassword()));
            Response resp = send("api/user", body);
            if (resp != null && !resp.isNullResponse() && resp.isSuccess()) {
                session.userId = resp.getPayload().optInt("userId", 0);
                session.token = resp.getPayload().optString("sessionToken", "");
                refreshSessionLabel();
            }
        });
        outer.add(loginPanel);

        /* ── Logout ── */
        JTextField logoutUserId = hint("(uses session — override here)");
        JTextField logoutToken = hint("(uses session — override here)");

        JPanel logoutPanel = form("Logout",
                "userId override", logoutUserId,
                "sessionToken override", logoutToken);
        addButton(logoutPanel, "Log Out", Color.decode("#b71c1c"), () -> {
            JSONObject body = authAction("logout");
            overrideAuth(body, logoutUserId, logoutToken);
            Response resp = send("api/user", body);
            if (resp != null && resp.isSuccess()) {
                session.clear();
                refreshSessionLabel();
            }
        });
        outer.add(logoutPanel);

        return outer;
    }

    // ── PROFILE (getProfile / updateProfile / setInterests) ────────────────
    private JPanel buildProfileTab() {
        JPanel outer = new JPanel(new GridLayout(1, 3, 8, 0));
        outer.setBorder(new EmptyBorder(8, 8, 8, 8));

        /* ── getProfile ── */
        JPanel getPanel = form("Get Profile");
        addButton(getPanel, "Get Profile", Color.decode("#1565c0"),
                () -> send("api/user", authAction("getProfile")));
        outer.add(getPanel);

        /* ── updateProfile ── */
        JTextField upFirst = hint("(leave blank to skip)");
        JTextField upLast = hint("(leave blank to skip)");
        JTextField upMajor = hint("(leave blank to skip)");
        JTextField upInterests = hint("chess, robotics, AI  (leave blank to skip)");

        JPanel updatePanel = form("Update Profile",
                "First Name", upFirst,
                "Last Name", upLast,
                "Major", upMajor,
                "Interests (csv)", upInterests);
        addButton(updatePanel, "Update Profile", Color.decode("#1565c0"), () -> {
            JSONObject body = authAction("updateProfile");
            putNonEmpty(body, "firstName", upFirst);
            putNonEmpty(body, "lastName", upLast);
            putNonEmpty(body, "major", upMajor);
            String iv = text(upInterests);
            if (!iv.isEmpty()) {
                JSONArray arr = new JSONArray();
                for (String s : iv.split(","))
                    arr.put(s.trim());
                body.put("interests", arr);
            }
            send("api/user", body);
        });
        outer.add(updatePanel);

        /* ── setInterests ── */
        JTextField siField = hint("chess, music, robotics");
        JPanel siPanel = form("Set Interests", "Interests (csv)", siField);
        addButton(siPanel, "Set Interests", Color.decode("#1565c0"), () -> {
            JSONObject body = authAction("setInterests");
            JSONArray arr = new JSONArray();
            for (String s : text(siField).split(","))
                arr.put(s.trim());
            body.put("interests", arr);
            send("api/user", body);
        });
        outer.add(siPanel);

        return outer;
    }

    // ── CLUBS (listClubs / createClub) ─────────────────────────────────────
    private JPanel buildClubsTab() {
        JPanel outer = new JPanel(new GridLayout(1, 2, 8, 0));
        outer.setBorder(new EmptyBorder(8, 8, 8, 8));

        /* ── listClubs ── */
        JPanel listPanel = form("List My Clubs");
        addButton(listPanel, "List Clubs", Color.decode("#1565c0"),
                () -> send("api/user", authAction("listClubs")));
        outer.add(listPanel);

        /* ── createClub (POST to api/club with action="create") ── */
        JTextField clubName = hint("Bilkent Robotics Club");
        JTextField clubDesc = hint("We build autonomous robots.");

        JPanel createPanel = form("Create Club",
                "Club Name", clubName,
                "Club Description", clubDesc);
        addButton(createPanel, "Create Club", Color.decode("#2e7d32"), () -> {
            JSONObject body = authAction("create");
            body.put("clubName", text(clubName));
            body.put("clubDescription", text(clubDesc));
            send("api/club", body);
        });
        outer.add(createPanel);

        return outer;
    }

    // ── UPLOAD (JFileChooser → RequestManager.uploadFiles) ─────────────────
    private JPanel buildUploadTab() {
        JPanel outer = new JPanel(new BorderLayout(6, 6));
        outer.setBorder(new TitledBorder("Upload Files  (png, jpg, jpeg, pdf, gif)"));

        DefaultListModel<java.io.File> model = new DefaultListModel<>();
        JList<java.io.File> fileList = new JList<>(model);
        fileList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> l, Object v, int i, boolean sel, boolean foc) {
                super.getListCellRendererComponent(l, v, i, sel, foc);
                java.io.File f = (java.io.File) v;
                setText(f.getName() + "  (" + f.length() / 1024 + " KB)");
                return this;
            }
        });
        JScrollPane scroll = new JScrollPane(fileList);
        scroll.setPreferredSize(new Dimension(500, 160));

        JButton addBtn = new JButton("Add Files…");
        JButton removeBtn = new JButton("Remove Selected");
        JButton uploadBtn = new JButton("Upload");
        uploadBtn.setBackground(Color.decode("#1565c0"));
        uploadBtn.setForeground(Color.WHITE);
        uploadBtn.setFont(uploadBtn.getFont().deriveFont(Font.BOLD));
        uploadBtn.setOpaque(true);
        uploadBtn.setBorderPainted(false);

        addBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setMultiSelectionEnabled(true);
            chooser.setFileFilter(new FileNameExtensionFilter(
                    "Allowed types (png, jpg, jpeg, pdf, gif)",
                    "png", "jpg", "jpeg", "pdf", "gif"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
                for (java.io.File f : chooser.getSelectedFiles())
                    model.addElement(f);
        });

        removeBtn.addActionListener(e -> fileList.getSelectedValuesList().forEach(model::removeElement));

        uploadBtn.addActionListener(e -> {
            if (model.isEmpty()) {
                showError("No files selected.");
                return;
            }
            ArrayList<java.io.File> files = new ArrayList<>();
            for (int i = 0; i < model.size(); i++)
                files.add(model.get(i));

            // RequestManager.uploadFiles() handles base64 encoding and posts to
            // "api/upload"
            JSONObject body = authAction("upload");
            log("» Uploading " + files.size() + " file(s) via RequestManager.uploadFiles() → api/upload");
            Response resp = RequestManager.uploadFiles(body, files);
            logResponse("api/upload", resp);
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        btnRow.add(addBtn);
        btnRow.add(removeBtn);
        btnRow.add(Box.createHorizontalStrut(12));
        btnRow.add(uploadBtn);

        JLabel hintLabel = new JLabel(
                "  RequestManager encodes each file as base64 and submits them in one JSON request.");
        hintLabel.setForeground(Color.GRAY);

        outer.add(hintLabel, BorderLayout.NORTH);
        outer.add(scroll, BorderLayout.CENTER);
        outer.add(btnRow, BorderLayout.SOUTH);
        return outer;
    }

    // ── MISC (generateEmbeddings + raw JSON sender) ─────────────────────────
    private JPanel buildMiscTab() {
        JPanel outer = new JPanel(new GridLayout(1, 2, 8, 0));
        outer.setBorder(new EmptyBorder(8, 8, 8, 8));

        /* ── generateEmbeddings ── */
        JPanel embPanel = form("Generate Embeddings");
        addButton(embPanel, "Generate Embeddings", Color.decode("#6a1b9a"),
                () -> send("api/user", authAction("generateEmbeddings")));
        outer.add(embPanel);

        /* ── Raw JSON sender ── */
        JPanel rawPanel = new JPanel(new BorderLayout(4, 4));
        rawPanel.setBorder(new TitledBorder("Raw JSON (manual)"));

        JComboBox<String> endpointBox = new JComboBox<>(
                new String[] { "api/user", "api/club", "api/upload" });
        JTextArea rawArea = new JTextArea(8, 30);
        rawArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        rawArea.setText("{\n  \"action\": \"\"\n}");

        JButton sendRaw = new JButton("Send");
        sendRaw.addActionListener(e -> {
            try {
                send((String) endpointBox.getSelectedItem(), new JSONObject(rawArea.getText()));
            } catch (Exception ex) {
                showError("Invalid JSON: " + ex.getMessage());
            }
        });

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        topRow.add(new JLabel("Endpoint:"));
        topRow.add(endpointBox);

        rawPanel.add(topRow, BorderLayout.NORTH);
        rawPanel.add(new JScrollPane(rawArea), BorderLayout.CENTER);
        rawPanel.add(sendRaw, BorderLayout.SOUTH);
        outer.add(rawPanel);

        return outer;
    }

    // ── RESPONSE PANE ────────────────────────────────────────────────────────
    private JPanel buildResponsePane() {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBorder(new TitledBorder("Response Log"));

        responseArea.setEditable(false);
        responseArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        responseArea.setBackground(new Color(24, 24, 24));
        responseArea.setForeground(new Color(180, 230, 180));
        responseArea.setCaretColor(Color.WHITE);

        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> responseArea.setText(""));

        p.add(new JScrollPane(responseArea), BorderLayout.CENTER);
        p.add(clearBtn, BorderLayout.EAST);
        return p;
    }

    // ════════════════════════════════════════════════════════════════════════
    // NETWORK HELPERS
    // ════════════════════════════════════════════════════════════════════════

    private Response send(String endpoint, JSONObject body) {
        log("→ POST  " + endpoint);
        log(body.toString(2));
        Response resp = RequestManager.sendPostRequest(endpoint, body);
        logResponse(endpoint, resp);
        return resp;
    }

    private void logResponse(String endpoint, Response resp) {
        if (resp == null || resp.isNullResponse()) {
            log("← NULL RESPONSE — check server / network");
        } else {
            log("← " + resp.getCode() + "  " + (resp.isSuccess() ? "✓ SUCCESS" : "✗ FAILED"));
            if (!resp.isSuccess())
                log("   Error : " + resp.getErrorMessage());
            JSONObject payload = resp.getPayload();
            if (payload != null && payload.length() > 0)
                log("   Payload: " + payload.toString(2));
        }
        log("─".repeat(64));
    }

    // ════════════════════════════════════════════════════════════════════════
    // BODY BUILDERS
    // ════════════════════════════════════════════════════════════════════════

    private static JSONObject action(String actionName) {
        JSONObject b = new JSONObject();
        b.put("action", actionName);
        return b;
    }

    private JSONObject authAction(String actionName) {
        JSONObject b = action(actionName);
        if (session.userId != 0)
            b.put("userId", session.userId);
        if (session.token != null)
            b.put("sessionToken", session.token);
        return b;
    }

    private void overrideAuth(JSONObject body, JTextField uidField, JTextField tokField) {
        String uid = text(uidField);
        String tok = text(tokField);
        if (!uid.isEmpty()) {
            try {
                body.put("userId", Integer.parseInt(uid));
            } catch (Exception ignored) {
            }
        }
        if (!tok.isEmpty())
            body.put("sessionToken", tok);
    }

    private void putNonEmpty(JSONObject body, String key, JTextField field) {
        String v = text(field);
        if (!v.isEmpty())
            body.put(key, v);
    }

    // ════════════════════════════════════════════════════════════════════════
    // UI HELPERS
    // ════════════════════════════════════════════════════════════════════════

    private void refreshSessionLabel() {
        SwingUtilities.invokeLater(() -> {
            if (session.token == null) {
                sessionLabel.setText("Not logged in");
                sessionLabel.setForeground(Color.GRAY);
            } else {
                int len = session.token.length();
                String shortTok = len > 10 ? "…" + session.token.substring(len - 10) : session.token;
                sessionLabel.setText("userId=" + session.userId + "  token=" + shortTok);
                sessionLabel.setForeground(new Color(0, 140, 60));
            }
        });
    }

    private void log(String text) {
        SwingUtilities.invokeLater(() -> {
            responseArea.append(text + "\n");
            responseArea.setCaretPosition(responseArea.getDocument().getLength());
        });
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Builds a titled GridBagLayout panel from interleaved (String label, Component
     * field) pairs.
     */
    private JPanel form(String title, Object... pairs) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(new TitledBorder(title));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 5, 3, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        for (int i = 0; i < pairs.length; i += 2) {
            c.gridy = i / 2;
            c.gridx = 0;
            c.weightx = 0;
            p.add(new JLabel(pairs[i] + ":"), c);
            c.gridx = 1;
            c.weightx = 1.0;
            p.add((Component) pairs[i + 1], c);
        }
        return p;
    }

    private void addButton(JPanel panel, String label, Color bg, Runnable action) {
        JButton btn = new JButton(label);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD));
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.addActionListener(e -> action.run());

        GridBagConstraints c = new GridBagConstraints();
        c.gridy = GridBagConstraints.RELATIVE;
        c.gridx = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 5, 5, 5);
        panel.add(btn, c);
    }

    /** Hint-text field: shows placeholder in gray, clears on first focus. */
    private static JTextField hint(String placeholder) {
        JTextField f = new JTextField(18);
        f.setText(placeholder);
        f.setForeground(Color.GRAY);
        f.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (f.getForeground() == Color.GRAY) {
                    f.setText("");
                    f.setForeground(UIManager.getColor("TextField.foreground"));
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (f.getText().isEmpty()) {
                    f.setText(placeholder);
                    f.setForeground(Color.GRAY);
                }
            }
        });
        return f;
    }

    /**
     * Returns the field value, or empty string if it still shows the placeholder
     * hint.
     */
    private static String text(JTextField f) {
        return f.getForeground() == Color.GRAY ? "" : f.getText().trim();
    }

    // ════════════════════════════════════════════════════════════════════════
    // SESSION STATE
    // ════════════════════════════════════════════════════════════════════════
    private static class SessionState {
        int userId = 0;
        String token = null;

        void clear() {
            userId = 0;
            token = null;
        }
    }
}