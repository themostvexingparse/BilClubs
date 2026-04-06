public class SearchResult {
    enum Kind {
        CLUB, EVENT
    }

    final Kind kind;
    final Club club;
    final Event event;
    double finalScore;

    SearchResult(Club c, double s) {
        kind = Kind.CLUB;
        club = c;
        event = null;
        finalScore = s;
    }

    SearchResult(Event e, double s) {
        kind = Kind.EVENT;
        event = e;
        club = null;
        finalScore = s;
    }
}