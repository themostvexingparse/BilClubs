import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Objects;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import embeddings.Embeddable;

@Entity
public class Event implements Embeddable {
    @Id
    @GeneratedValue
    private Integer id;
    private String name;
    private String description;
    private String location;
    private LocalDateTime start;
    private LocalDateTime end;
    private Integer quota;
    @ElementCollection(fetch = FetchType.EAGER)
    private ArrayList<User> registeredUsers = null;
    private String poster = "static/default-event-poster.jpg";
    private Integer clubId;
    private String clubName;
    private Integer GE250 = 0;
    private Discussion discussion;

    private float[] embeddings;

    public Event() {
    }

    public Event(String argName, Club argClub, String argDescription, String argPlace, LocalDateTime argStart,
            LocalDateTime argEnd, Integer argQuota) {
        name = argName;
        description = argDescription;
        clubId = argClub.getId();
        clubName = argClub.getClubName();
        location = argPlace;
        start = argStart;
        end = argEnd;
        quota = argQuota; // set to null for infinite quota
        registeredUsers = new ArrayList<>();
        discussion = new Discussion();
    }

    public Discussion getDiscussion() {
        return discussion;
    }

    public boolean isOpen() {
        return (quota == null || registeredUsers.size() != quota) && LocalDateTime.now(ZoneOffset.UTC).isBefore(start);
    }

    public boolean conflictsWith(Event other) {
        LocalDateTime otherStart = other.getStart();
        LocalDateTime otherEnd = other.getEnd();
        return !(end.isBefore(otherStart) || otherEnd.isBefore(start));
    }

    public void registerUser(User user) {
        if (registeredUsers.contains(user))
            return;
        registeredUsers.add(user);
    }

    public void removeUser(User user) {
        registeredUsers.remove(user);
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getPoster() {
        return poster;
    }

    public Integer getId() {
        return id;
    }

    public String getEventName() {
        return name;
    }

    public String getClubName() {
        return clubName;
    }

    public String getDescription() {
        return description;
    }

    public Integer getClubId() {
        return clubId;
    }

    public String getLocation() {
        return location;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public long getStartEpoch() {
        return start.toEpochSecond(ZoneOffset.UTC);
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public long getEndEpoch() {
        return end.toEpochSecond(ZoneOffset.UTC);
    }

    public Integer getQuota() {
        return quota;
    }

    public int getRegistreeCount() {
        return registeredUsers.size();
    }

    public void setName(String argName) {
        name = argName;
    }

    public void setDescription(String argDescription) {
        description = argDescription;
    }

    public void setLocation(String argLocation) {
        location = argLocation;
    }

    public void setStartAndEnd(LocalDateTime argStart, LocalDateTime argEnd) {
        start = argStart;
        end = argEnd;
    }

    public ArrayList<Integer> getRegisteredUserIds() {
        ArrayList<Integer> ids = new ArrayList<>();
        for (User user : registeredUsers)
            ids.add(user.getId());
        return ids;
    }

    public boolean setQuota(int argQuota) {
        if (argQuota < registeredUsers.size())
            return false;
        quota = argQuota;
        return true;
    }

    public void setGE250(Integer GE250) {
        this.GE250 = GE250;
    }

    public Integer getGE250() {
        return GE250;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Event other = (Event) o;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Event ID: " + id + " named " + name;
    }

    /*
     * Embeddings logic
     */

    @Override
    public String generateEmbeddingText() {
        return "Event Name: " + name + "\nEvent Description: " + description + "\nLocation: " + location;
    }

    @Override
    public float[] getEmbedding() {
        return embeddings;
    }

    @Override
    public void setEmbedding(float[] embeddings) {
        this.embeddings = embeddings;
    }

    @Override
    public final String getTaskType() {
        return "RETRIEVAL_DOCUMENT";
    }
}