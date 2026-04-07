import java.time.LocalDateTime;
import java.util.ArrayList;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;


@Entity
public class Comment implements Comparable<Comment>{
    @Id
    @GeneratedValue
    private Integer id;
    private LocalDateTime time;
    private User author;
    private String content;
    private ArrayList<Comment> replies;
    private Comment parent;
    private Discussion discussion;
    
    public Comment(){}

    public Comment(Comment parent, User author, String content, Discussion discussion){
        this.parent = parent;
        if (parent != null) parent.addReply(this);
        replies = new ArrayList<>();
        this.author = author;
        this.content = content;
        this.discussion = discussion;
        this.time = LocalDateTime.now();
    }

    // returns id of the comment
    public Integer getId(){
        return id;
    }

    // Adds the reply to the replies ArrayList
    public void addReply(Comment reply){
        replies.add(reply);
    }

    // returns parentId of the comment
    public Comment getParent(){
        return parent;
    }

    // compares id's to determine which comment should come first
    public int compareTo(Comment other){
        return Integer.compare(this.id, other.id);
    }

    // returns the time that the comment was posted, in format: dd m yyyy, HH:MM
    public String getTime(){
        String FormattedHour = String.format("%02d",time.getHour());
        String FormattedMinute = String.format("%02d",time.getMinute());
        return time.getDayOfMonth() + " " + time.getMonth() + " " + time.getYear() + ", " + FormattedHour + ":" + FormattedMinute;
    }

    // returns the author of the comment
    public User getAuthor(){
        return author;
    }

    // returns the content of the comment
    public String getContent(){
        return content;
    }

    // returns the replies of a comment
    public ArrayList<Comment> getReplies(){
        return replies;
    }

    public Discussion getDiscussion(){
        return discussion;
    }

    // shows which comment is being replied, returns null if it is a parent comment
    public User replyingTo(){
        if(this.parent == null){return null;}
        return this.parent.getAuthor();
    }

    // Changes the content of the comment to newComment
    public void setContent(String newContent){
        this.content = newContent;
    }
}