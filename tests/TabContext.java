import org.json.JSONObject;

/**
 * Callback interface that tab panels use to communicate with the main frame.
 * Keeps tabs decoupled from BilClubsTestSuite internals.
 */
public interface TabContext {
    /** POST body to endpoint, log the exchange, and return the response. */
    Response send(String endpoint, JSONObject body);

    /** Append a line to the shared response log. */
    void log(String text);

    /** Log a Response object with standard formatting. */
    void logResponse(String endpoint, Response resp);

    /** Show a modal error dialog. */
    void showError(String msg);

    /** Re-render the session label after login/logout. */
    void refreshSession();
}
