import org.json.JSONObject;

/**
 * Mutable session holder shared across all tabs.
 * Also provides authAction() as a convenience factory so tabs don't
 * have to manually assemble userId + sessionToken every time.
 */
public class SessionState {
    int userId = 0;
    String token = null;

    /**
     * Builds a request body pre-populated with the current session credentials
     * and the given action name.
     */
    public JSONObject authAction(String actionName) {
        JSONObject body = new JSONObject();
        body.put("action", actionName);
        if (userId != 0)
            body.put("userId", userId);
        if (token != null)
            body.put("sessionToken", token);
        return body;
    }

    public void clear() {
        userId = 0;
        token = null;
    }
}
