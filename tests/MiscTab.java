import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

import org.json.JSONObject;

/**
 * Misc tab — generateEmbeddings + raw JSON sender.
 * Endpoint: varies (chosen via combo box)
 */
public class MiscTab extends JPanel {

    private final SessionState session;
    private final TabContext ctx;

    public MiscTab(SessionState session, TabContext ctx) {
        super(new GridLayout(1, 2, 8, 0));
        setBorder(new EmptyBorder(8, 8, 8, 8));
        this.session = session;
        this.ctx = ctx;
        build();
    }

    private void build() {

        /* ── generateEmbeddings ── */
        JPanel embPanel = TabHelpers.form("Generate Embeddings");
        TabHelpers.addButton(embPanel, "Generate Embeddings", Color.decode("#6a1b9a"),
                () -> ctx.send("api/user", session.authAction("generateEmbeddings")));
        add(embPanel);

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
                ctx.send((String) endpointBox.getSelectedItem(),
                        new JSONObject(rawArea.getText()));
            } catch (Exception ex) {
                ctx.showError("Invalid JSON: " + ex.getMessage());
            }
        });

        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        topRow.add(new JLabel("Endpoint:"));
        topRow.add(endpointBox);

        rawPanel.add(topRow, BorderLayout.NORTH);
        rawPanel.add(new JScrollPane(rawArea), BorderLayout.CENTER);
        rawPanel.add(sendRaw, BorderLayout.SOUTH);
        add(rawPanel);
    }
}
