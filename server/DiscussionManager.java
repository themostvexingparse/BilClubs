import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Collection;

public class DiscussionManager {

 private HashMap<Integer,Comment> allComments = new LinkedHashMap<>();
 private static int idCounter = 1; // Assigns an id to a new comment starting from 1


/*Checks if the content is valid, finds parent and posts the new comment*/
public void postComment(String author, String content, int parentId){
if(isValid(content)){

Comment parent = null;
 if(parentId != -1){
parent = findCommentById(parentId);


    if(parent == null){return;}

}
int assignedId = idCounter++;
Comment newComment = new Comment(assignedId, parent, author, content);
addNewComment(newComment);
 if(parent != null){

    parent.addReply(newComment);
 }

}


}

/*Adds the new posted comment to the allComments HashMap */
public void addNewComment(Comment c){

    allComments.put(c.getId(), c);
}

/*Changes the content of the comment to newContent if newContent is valid. */
public boolean editComment(int id, String newContent){
    if(!isValid(newContent)){return false;}

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
    if(target == null)return;

int parentId = target.getParentId();
Comment parent = findCommentById(parentId);

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



/*Checks whether the content is not empty. */
public boolean isValid(String content){

    if(content.isBlank()){return false;}
   

return true;
}

/*Finds the comment with the id */
public Comment findCommentById(int targetId){

 return allComments.get(targetId);


}

/*returns all comments */
public Collection<Comment> getAllComments(){
    return allComments.values();
}




}
