import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;

import org.json.JSONObject;

/**
 * Static UI / body-building helpers shared by all tab panels.
 * All methods are stateless — they operate purely on their arguments.
 */
public class TabHelpers {

    private TabHelpers() {}

    // ── Body builders ────────────────────────────────────────────────────────

    /** Builds a minimal {@code {"action": actionName}} JSONObject. */
    public static JSONObject action(String actionName) {
        JSONObject b = new JSONObject();
        b.put("action", actionName);
        return b;
    }

    /**
     * Copies {@code field}'s text into {@code body} under {@code key}
     * only when the field is non-empty (i.e. not showing its placeholder hint).
     */
    public static void putNonEmpty(JSONObject body, String key, JTextField field) {
        String v = text(field);
        if (!v.isEmpty())
            body.put(key, v);
    }

    /**
     * Overrides userId / sessionToken in {@code body} from the given text fields,
     * ignoring fields that still show their placeholder text.
     */
    public static void overrideAuth(JSONObject body, JTextField uidField, JTextField tokField) {
        String uid = text(uidField);
        String tok = text(tokField);
        if (!uid.isEmpty()) {
            try {
                body.put("userId", Integer.parseInt(uid));
            } catch (NumberFormatException ignored) {}
        }
        if (!tok.isEmpty())
            body.put("sessionToken", tok);
    }

    // ── Widget factories ─────────────────────────────────────────────────────

    /**
     * Builds a {@link GridBagLayout} panel with a titled border from interleaved
     * {@code (String label, Component field)} varargs pairs.
     */
    public static JPanel form(String title, Object... pairs) {
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

    /**
     * Creates a styled button and appends it to {@code panel} spanning both
     * GridBag columns.
     */
    public static void addButton(JPanel panel, String label, Color bg, Runnable action) {
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

    /**
     * Creates a text field that displays {@code placeholder} in gray and clears
     * itself on first focus (hint / ghost text pattern).
     */
    public static JTextField hint(String placeholder) {
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
     * Returns the field's text trimmed, or an empty string if it is still
     * showing its placeholder hint.
     */
    public static String text(JTextField f) {
        return f.getForeground() == Color.GRAY ? "" : f.getText().trim();
    }
}
