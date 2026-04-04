import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import org.json.JSONObject;

/**
 * Auth tab — Signup / Login / Logout.
 * Endpoint: POST api/user
 */
public class AuthTab extends JPanel {

    private final SessionState session;
    private final TabContext ctx;

    public AuthTab(SessionState session, TabContext ctx) {
        super(new GridLayout(1, 3, 8, 0));
        setBorder(new EmptyBorder(8, 8, 8, 8));
        this.session = session;
        this.ctx = ctx;
        build();
    }

    private void build() {

        /* ── Signup ── */
        JTextField signupEmail = TabHelpers.hint("student@ug.bilkent.edu.tr");
        JPasswordField signupPass = new JPasswordField(18);
        JTextField signupFirst = TabHelpers.hint("Ada");
        JTextField signupLast  = TabHelpers.hint("Lovelace");
        JTextField signupMajor = TabHelpers.hint("Computer Engineering  (optional)");

        JPanel signupPanel = TabHelpers.form("Signup",
                "Email",      signupEmail,
                "Password",   signupPass,
                "First Name", signupFirst,
                "Last Name",  signupLast,
                "Major",      signupMajor);
        TabHelpers.addButton(signupPanel, "Sign Up", Color.decode("#2e7d32"), () -> {
            JSONObject body = TabHelpers.action("signup");
            body.put("email",     TabHelpers.text(signupEmail));
            body.put("password",  new String(signupPass.getPassword()));
            body.put("firstName", TabHelpers.text(signupFirst));
            body.put("lastName",  TabHelpers.text(signupLast));
            String major = TabHelpers.text(signupMajor);
            if (!major.isEmpty())
                body.put("major", major);
            ctx.send("api/user", body);
        });
        add(signupPanel);

        /* ── Login ── */
        JTextField loginEmail = TabHelpers.hint("student@ug.bilkent.edu.tr");
        JPasswordField loginPass = new JPasswordField(18);

        JPanel loginPanel = TabHelpers.form("Login",
                "Email",    loginEmail,
                "Password", loginPass);
        TabHelpers.addButton(loginPanel, "Log In", Color.decode("#1565c0"), () -> {
            JSONObject body = TabHelpers.action("login");
            body.put("email",    TabHelpers.text(loginEmail));
            body.put("password", new String(loginPass.getPassword()));
            Response resp = ctx.send("api/user", body);
            if (resp != null && !resp.isNullResponse() && resp.isSuccess()) {
                session.userId = resp.getPayload().optInt("userId", 0);
                session.token  = resp.getPayload().optString("sessionToken", "");
                ctx.refreshSession();
            }
        });
        add(loginPanel);

        /* ── Logout ── */
        JTextField logoutUserId = TabHelpers.hint("(uses session — override here)");
        JTextField logoutToken  = TabHelpers.hint("(uses session — override here)");

        JPanel logoutPanel = TabHelpers.form("Logout",
                "userId override",       logoutUserId,
                "sessionToken override", logoutToken);
        TabHelpers.addButton(logoutPanel, "Log Out", Color.decode("#b71c1c"), () -> {
            JSONObject body = session.authAction("logout");
            TabHelpers.overrideAuth(body, logoutUserId, logoutToken);
            Response resp = ctx.send("api/user", body);
            if (resp != null && resp.isSuccess()) {
                session.clear();
                ctx.refreshSession();
            }
        });
        add(logoutPanel);
    }
}
