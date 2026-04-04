import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

import org.json.JSONObject;

/**
 * BilClubs API Test Suite — Swing UI (frame shell)
 *
 * Compile: javac -cp "server/lib/*;." *.java
 * Run:     java  -cp "server/lib/*;." BilClubsTestSuite
 *
 * Implements TabContext so individual tab panels can send requests,
 * log output, and trigger session-label updates without coupling to
 * this class directly.
 */
public class BilClubsTestSuite extends JFrame implements TabContext {

    // ── Shared session (populated on login, cleared on logout) ──────────────
    private final SessionState session = new SessionState();

    // ── Persistent widgets ──────────────────────────────────────────────────
    private final JTextField serverAddressField = new JTextField("http://127.0.0.1:5000", 28);
    private final JLabel     sessionLabel       = new JLabel("Not logged in");
    private final JTextArea  responseArea       = new JTextArea(14, 70);

    // ════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BilClubsTestSuite().setVisible(true));
    }

    public BilClubsTestSuite() {
        super("BilClubs API Test Suite");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(6, 6));
        add(buildTopBar(),      BorderLayout.NORTH);
        add(buildTabs(),        BorderLayout.CENTER);
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
            refreshSession();
        });
        p.add(clearBtn);

        RequestManager.setDefaultAddress(serverAddressField.getText().trim());
        return p;
    }

    // ════════════════════════════════════════════════════════════════════════
    // TABS
    // ════════════════════════════════════════════════════════════════════════
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Auth",            new AuthTab(session, this));
        tabs.addTab("Profile",         new ProfileTab(session, this));
        tabs.addTab("Foreign Profile", new ForeignProfileTab(session, this));
        tabs.addTab("Clubs",           new ClubsTab(session, this));
        tabs.addTab("Events",          new EventsTab(session, this));
        tabs.addTab("Upload",          new UploadTab(session, this));
        tabs.addTab("Misc",            new MiscTab(session, this));
        return tabs;
    }

    // ────────────────────────────────────────────────────────────────────────
    // RESPONSE PANE
    // ────────────────────────────────────────────────────────────────────────
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
    // TabContext implementation
    // ════════════════════════════════════════════════════════════════════════

    @Override
    public Response send(String endpoint, JSONObject body) {
        log("→ POST  " + endpoint);
        log(body.toString(2));
        Response resp = RequestManager.sendPostRequest(endpoint, body);
        logResponse(endpoint, resp);
        return resp;
    }

    @Override
    public void logResponse(String endpoint, Response resp) {
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

    @Override
    public void log(String text) {
        SwingUtilities.invokeLater(() -> {
            responseArea.append(text + "\n");
            responseArea.setCaretPosition(responseArea.getDocument().getLength());
        });
    }

    @Override
    public void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void refreshSession() {
        SwingUtilities.invokeLater(() -> {
            if (session.token == null) {
                sessionLabel.setText("Not logged in");
                sessionLabel.setForeground(Color.GRAY);
            } else {
                int    len      = session.token.length();
                String shortTok = len > 10 ? "…" + session.token.substring(len - 10) : session.token;
                sessionLabel.setText("userId=" + session.userId + "  token=" + shortTok);
                sessionLabel.setForeground(new Color(0, 140, 60));
            }
        });
    }
}