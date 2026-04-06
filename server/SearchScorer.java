import java.util.Locale;

public class SearchScorer {

    private static final double NAME_MULTIPLIER = 3.0;
    private static final double DESC_MULTIPLIER = 1.0;
    private static final double START_BONUS = 0.5;

    public static double score(String query, String name, String desc) {
        String q = query.toLowerCase(Locale.ROOT);
        String n = name.toLowerCase(Locale.ROOT);
        String d = desc.toLowerCase(Locale.ROOT);

        double score = 0.0;

        if (n.contains(q)) {
            double base = (double) q.length() / n.length();
            int occurrences = countOccurrences(n, q);
            double startBonus = n.startsWith(q) ? START_BONUS : 0.0;
            score += NAME_MULTIPLIER * (base * occurrences + startBonus);
        }

        if (d.contains(q)) {
            double base = (double) q.length() / d.length();
            int occurrences = countOccurrences(d, q);
            score += DESC_MULTIPLIER * (base * occurrences);
        }

        return score;
    }

    private static int countOccurrences(String text, String query) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(query, idx)) != -1) {
            count++;
            idx += query.length();
        }
        return count;
    }
}