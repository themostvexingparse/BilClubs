public class Sanitizer {
    public static String sanitizeEscapedString(String string) {
        string = string.replace("\\", "\\\\");
        string = string.replace("\"", "\\\"");
        string = string.replace("\n", "\\n");
        string = string.replace("\r", "\\r");
        string = string.replace("\t", "\\t");
        return string;
    }
}

// TODO: Make use of sanitizer actively
// TODO: Implement HTML sanitizer for XSS vulnerabilities