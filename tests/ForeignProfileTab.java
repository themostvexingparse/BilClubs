import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import org.json.JSONObject;

/**
 * Foreign Profile tab — getForeignProfile / getForeignProfileClubs /
 * getForeignProfileUpcomingEvents.
 * Endpoint: POST api/user (all actions require auth)
 */
public class ForeignProfileTab extends JPanel {

    private final SessionState session;
    private final TabContext ctx;

    public ForeignProfileTab(SessionState session, TabContext ctx) {
        super(new GridLayout(1, 3, 8, 0));
        setBorder(new EmptyBorder(8, 8, 8, 8));
        this.session = session;
        this.ctx = ctx;
        build();
    }

    private void build() {

        /* ── getForeignProfile ── */
        JTextField fpUserId = TabHelpers.hint("target userId (integer)");
        JPanel fpPanel = TabHelpers.form("Get Foreign Profile", "Target User ID", fpUserId);
        TabHelpers.addButton(fpPanel, "Get Foreign Profile", Color.decode("#1565c0"), () -> {
            JSONObject body = session.authAction("getForeignProfile");
            String uid = TabHelpers.text(fpUserId);
            if (!uid.isEmpty()) {
                try {
                    body.put("targetUserId", Integer.parseInt(uid));
                } catch (NumberFormatException ignored) {
                    ctx.showError("targetUserId must be an integer.");
                    return;
                }
            }
            ctx.send("api/user", body);
        });
        add(fpPanel);

        /* ── getForeignProfileClubs ── */
        JTextField fpcUserId = TabHelpers.hint("target userId (integer)");
        JPanel fpcPanel = TabHelpers.form("Get Foreign Profile Clubs", "Target User ID", fpcUserId);
        TabHelpers.addButton(fpcPanel, "Get Clubs", Color.decode("#1565c0"), () -> {
            JSONObject body = session.authAction("getForeignProfileClubs");
            String uid = TabHelpers.text(fpcUserId);
            if (!uid.isEmpty()) {
                try {
                    body.put("targetUserId", Integer.parseInt(uid));
                } catch (NumberFormatException ignored) {
                    ctx.showError("targetUserId must be an integer.");
                    return;
                }
            }
            ctx.send("api/user", body);
        });
        add(fpcPanel);
    }
}
