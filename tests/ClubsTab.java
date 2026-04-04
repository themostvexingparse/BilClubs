import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import org.json.JSONObject;

/**
 * Clubs tab — listClubs / joinClub / getMembers / createClub.
 *
 * listClubs → POST api/user action="listClubs" (auth required)
 * joinClub → POST api/user action="joinClub" (auth required, needs clubId)
 * getMembers → POST api/club action="getMembers" (auth required, needs clubId)
 * createClub → POST api/club action="create" (auth required, MANAGER/ADMIN
 * only)
 */
public class ClubsTab extends JPanel {

    private final SessionState session;
    private final TabContext ctx;

    public ClubsTab(SessionState session, TabContext ctx) {
        super(new GridLayout(1, 4, 8, 0));
        setBorder(new EmptyBorder(8, 8, 8, 8));
        this.session = session;
        this.ctx = ctx;
        build();
    }

    private void build() {

        /* ── listClubs ── */
        JPanel listPanel = TabHelpers.form("List Clubs");
        TabHelpers.addButton(listPanel, "List Clubs", Color.decode("#1565c0"),
                () -> ctx.send("api/user", session.authAction("listClubs")));
        add(listPanel);

        /* ── joinClub ── */
        JTextField clubIdField = TabHelpers.hint("clubId (integer)");
        JPanel joinPanel = TabHelpers.form("Join Club", "Club ID", clubIdField);
        TabHelpers.addButton(joinPanel, "Join Club", Color.decode("#f57f17"), () -> {
            String cid = TabHelpers.text(clubIdField);
            if (cid.isEmpty()) {
                ctx.showError("clubId cannot be empty.");
                return;
            }
            try {
                JSONObject body = session.authAction("joinClub");
                body.put("clubId", Integer.parseInt(cid));
                ctx.send("api/user", body);
            } catch (NumberFormatException e) {
                ctx.showError("clubId must be an integer.");
            }
        });
        add(joinPanel);

        /* ── getMembers ── */
        JTextField getMembersClubId = TabHelpers.hint("clubId (integer)");
        JPanel getMembersPanel = TabHelpers.form("Get Members", "Club ID", getMembersClubId);
        TabHelpers.addButton(getMembersPanel, "Get Members", Color.decode("#1565c0"), () -> {
            String cid = TabHelpers.text(getMembersClubId);
            if (cid.isEmpty()) {
                ctx.showError("clubId cannot be empty.");
                return;
            }
            try {
                JSONObject body = session.authAction("getMembers");
                body.put("clubId", Integer.parseInt(cid));
                ctx.send("api/club", body);
            } catch (NumberFormatException e) {
                ctx.showError("clubId must be an integer.");
            }
        });
        add(getMembersPanel);

        /* ── createClub ── */
        JTextField clubName = TabHelpers.hint("Bilkent Robotics Club");
        JTextField clubDesc = TabHelpers.hint("We build autonomous robots.");

        JPanel createPanel = TabHelpers.form("Create Club",
                "Club Name", clubName,
                "Club Description", clubDesc);
        TabHelpers.addButton(createPanel, "Create Club", Color.decode("#2e7d32"), () -> {
            JSONObject body = session.authAction("create");
            body.put("clubName", TabHelpers.text(clubName));
            body.put("clubDescription", TabHelpers.text(clubDesc));
            ctx.send("api/club", body);
        });
        add(createPanel);
    }
}
