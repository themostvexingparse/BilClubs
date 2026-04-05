import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DurationFormatter {
    public static String format(LocalDateTime start, LocalDateTime end) {
        return format(Duration.between(start, end));
    }

    public static String format(Duration duration) {
        if (duration == null || duration.isNegative()) {
            return "";
        }

        if (duration.isZero()) {
            return "0 seconds";
        }

        List<String> parts = new ArrayList<>();

        long days = duration.toDaysPart();
        long hours = duration.toHoursPart();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        addPart(parts, days, "day", "days");
        addPart(parts, hours, "hour", "hours");
        addPart(parts, minutes, "minute", "minutes");
        addPart(parts, seconds, "second", "seconds");

        String result = String.join(", ", parts);
        return result;
    }

    private static void addPart(List<String> parts, long value, String singular, String plural) {
        if (value > 0) {
            // so if the corresponding value does not exist we don't add it to the final
            // string
            parts.add(value + " " + (value == 1 ? singular : plural));
        }
    }
}