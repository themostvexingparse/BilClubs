import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Profile tab — getProfile / updateProfile / setInterests.
 * Endpoint: POST api/user  (all actions require auth)
 */
public class ProfileTab extends JPanel {

    private final SessionState session;
    private final TabContext ctx;

    public ProfileTab(SessionState session, TabContext ctx) {
        super(new GridLayout(1, 3, 8, 0));
        setBorder(new EmptyBorder(8, 8, 8, 8));
        this.session = session;
        this.ctx = ctx;
        build();
    }

    private void build() {

        /* ── getProfile ── */
        JPanel getPanel = TabHelpers.form("Get Profile");
        TabHelpers.addButton(getPanel, "Get Profile", Color.decode("#1565c0"),
                () -> ctx.send("api/user", session.authAction("getProfile")));
        add(getPanel);

        /* ── updateProfile ── */
        JTextField upFirst     = TabHelpers.hint("(leave blank to skip)");
        JTextField upLast      = TabHelpers.hint("(leave blank to skip)");
        JTextField upMajor     = TabHelpers.hint("(leave blank to skip)");
        JTextField upInterests = TabHelpers.hint("chess, robotics, AI  (leave blank to skip)");

        JPanel updatePanel = TabHelpers.form("Update Profile",
                "First Name",     upFirst,
                "Last Name",      upLast,
                "Major",          upMajor,
                "Interests (csv)", upInterests);
        TabHelpers.addButton(updatePanel, "Update Profile", Color.decode("#1565c0"), () -> {
            JSONObject body = session.authAction("updateProfile");
            TabHelpers.putNonEmpty(body, "firstName", upFirst);
            TabHelpers.putNonEmpty(body, "lastName",  upLast);
            TabHelpers.putNonEmpty(body, "major",     upMajor);
            String iv = TabHelpers.text(upInterests);
            if (!iv.isEmpty()) {
                JSONArray arr = new JSONArray();
                for (String s : iv.split(","))
                    arr.put(s.trim());
                body.put("interests", arr);
            }
            ctx.send("api/user", body);
        });
        add(updatePanel);

        /* ── setInterests ── */
        JTextField siField = TabHelpers.hint("chess, music, robotics");
        JPanel siPanel = TabHelpers.form("Set Interests", "Interests (csv)", siField);
        TabHelpers.addButton(siPanel, "Set Interests", Color.decode("#1565c0"), () -> {
            JSONObject body = session.authAction("setInterests");
            JSONArray arr = new JSONArray();
            for (String s : TabHelpers.text(siField).split(","))
                arr.put(s.trim());
            body.put("interests", arr);
            ctx.send("api/user", body);
        });
        add(siPanel);
    }
}
