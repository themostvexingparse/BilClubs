import java.time.LocalDateTime;
import java.util.Set;
import java.util.TreeSet;


public class Comment implements Comparable<Comment>{

    private int id;
    private int parentId; 
    private LocalDateTime time;
    private User author;
    private String content;
    private Set<Comment> replies = new TreeSet<>();
    private Comment parent;
    
    
    



 public Comment(int id, Comment parent, User author, String content){

    this.id = id;
    this.parent = parent;
    this.author = author;
    this.content = content;
    this.time = LocalDateTime.now();

    if(parent != null) {this.parentId = parent.getId();}
    else{ this.parentId = -1;}
    
}

// returns id of the comment
public int getId(){
    return id;
}
// Adds the reply to the replies TreeSet
public void addReply(Comment reply){
    replies.add(reply);
}

// returns parentId of the comment
public int getParentId(){
    return parentId;
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
public Set<Comment> getReplies(){
    return replies;
}

// shows which comment is being replied, returns "" if it is a parent comment
public User replyingTo(){

    if(this.parent == null){return null;}

    return this.parent.getAuthor();
}

// Changes the content of the comment to newComment
public void setContent(String newContent){
    this.content = newContent;
}











}