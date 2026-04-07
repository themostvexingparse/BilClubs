import java.util.HashMap;
import java.util.LinkedHashMap;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import java.util.Collection;

@Entity
public class Discussion {
    @Id
    @GeneratedValue
    private Integer id;
    private HashMap<Integer, Comment> allComments;

    public Discussion(){
        allComments = new LinkedHashMap<>();
    }

    /*Adds a new posted comment to the allComments HashMap */
    public void addNewComment(Comment c){
        allComments.put(c.getId(), c);
    }

    /*Changes the content of the comment to newContent if newContent is valid. */
    public boolean editComment(int id, String newContent){
        Comment target = findCommentById(id);
        if(target != null){
            target.setContent(newContent);
            return true;
        }
        return false;
    }

    /*Finds the comment to delete by id and deletes current comment 
    and all comments below by calling removeRecursively. */
    public void deleteComment(int id){
        Comment target = findCommentById(id);
        if(target == null) return;
        Comment parent = target.getParent();
        if(parent != null){
            parent.getReplies().remove(target);
        }
        removeRecursively(target);
    }

    /*Deletes the bottom comment first, 
    and then goes up till it deletes the selected comment */
    public void removeRecursively(Comment current){
        for(Comment reply : current.getReplies()){
            removeRecursively(reply);
        }
        allComments.remove(current.getId());
    }

    /*Finds the comment with the id */
    public Comment findCommentById(int targetId){
        return allComments.get(targetId);
    }

    public Integer getId(){
        return id;
    }

    /*returns all comments */
    public Collection<Comment> getAllComments(){
        return allComments.values();
    }
}
