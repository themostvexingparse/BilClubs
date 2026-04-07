import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

public class DBManager {

    private boolean initialized = false;

    // we used to have separate persistence units but this caused reference
    // incompatibility or something like that

    // so we had to switch to a single persistence unit for all core entities (User,
    // Club, Event) so cross entity references are properly managed within one
    // ObjectDB context.
    private EntityManagerFactory coreFactory = null;

    // media files are kept separately because they don't reference other entities
    private EntityManagerFactory fileManagerFactory = null;

    public void initialize(String directory) {
        if (initialized)
            return;
        String corePersistence = String.format("objectdb:%s/bilclubs.odb", directory);
        String filePersistence = String.format("objectdb:%s/static.odb", directory);
        coreFactory = Persistence.createEntityManagerFactory(corePersistence);
        fileManagerFactory = Persistence.createEntityManagerFactory(filePersistence);
        initialized = true;
    }

    public boolean doesUniqueUserExist(Filter filter) {
        List<User> queriedUsers = queryUsers(filter);
        return (queriedUsers.size() == 1);
    }

    public List<User> queryUsers(Filter filter) {
        if (ServerConfig.PRINT_DEBUG)
            System.out.printf("Queried users for filter: %s\n", filter.toString());
        if (!initialized)
            return null;
        Map<String, String> keyMap = new HashMap<String, String>() {
            {
                put("id", "u.getId()");
                put("token", "u.getToken()");
                put("name", "u.getFullName()");
                put("email", "u.getEmail()");
            }
        };
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT u FROM User u ");
        boolean hasFilter = false;
        Map<String, Object> filterMap = filter.getMap();

        for (String key : filterMap.keySet()) {
            String format = keyMap.get(key);
            if (format == null)
                continue;
            if (!hasFilter) {
                queryBuilder.append("WHERE ");
                hasFilter = true;
            } else {
                queryBuilder.append("AND ");
            }
            queryBuilder.append(format);
            queryBuilder.append(" = :");
            queryBuilder.append(key);
            queryBuilder.append(" ");
        }
        String queryString = queryBuilder.toString().trim();
        EntityManager em = coreFactory.createEntityManager();
        TypedQuery<User> query = em.createQuery(queryString, User.class);
        for (String key : filterMap.keySet()) {
            if (!keyMap.containsKey(key))
                continue;
            query.setParameter(key, filterMap.get(key));
        }
        List<User> results = query.getResultList();
        em.close();
        return results;
    }

    public User queryUser(Filter filter) {
        if (!initialized)
            return null;
        List<User> users = queryUsers(filter);
        if (users.size() != 1) {
            if (ServerConfig.PRINT_DEBUG)
                System.out.printf("Info: %s returned %s results.\n", filter, users.size());
            return null;
        }
        return users.get(0);
    }

    public boolean addUser(User user) {
        if (!initialized)
            return false;
        Filter emailFilter = new Filter();
        emailFilter.addFilter("email", user.getEmail());
        if (queryUser(emailFilter) != null)
            return false; // prevent creation of a new account with the same email
        EntityManager em = coreFactory.createEntityManager();
        em.getTransaction().begin();
        em.persist(user);
        em.getTransaction().commit();
        em.close();
        if (ServerConfig.PRINT_DEBUG)
            System.out.printf("Added user: %s\n", user.toString());
        return true;
    }

    public boolean addUserUnsafe(User user) {
        if (!initialized)
            return false;
        EntityManager em = coreFactory.createEntityManager();
        em.getTransaction().begin();
        em.persist(user);
        em.getTransaction().commit();
        em.close();
        if (ServerConfig.PRINT_DEBUG)
            System.out.printf("Added user: %s\n", user.toString());
        return true;
    }

    public boolean removeUserFromClubDirect(int userId, int clubId) {
        if (!initialized)
            return false;
        EntityManager em = coreFactory.createEntityManager();
        em.getTransaction().begin();
        User managed = em.find(User.class, userId);
        if (managed == null) {
            em.getTransaction().rollback();
            em.close();
            return false;
        }
        managed.getClubPrivileges().remove(clubId);
        em.getTransaction().commit();
        em.close();
        return true;
    }

    public boolean updateUser(User user) {
        if (user == null || user.getId() == null || !initialized)
            return false;
        EntityManager em = coreFactory.createEntityManager();
        em.getTransaction().begin();
        User queriedUser = em.find(User.class, user.getId());
        if (queriedUser == null) {
            em.close();
            return false;
        }
        em.merge(user);
        em.getTransaction().commit();
        em.close();
        if (ServerConfig.PRINT_DEBUG)
            System.out.printf("Updated user: %s\n", user.toString());
        return true;
    }

    public boolean addClub(Club club) {
        if (club == null || !initialized)
            return false;
        EntityManager em = coreFactory.createEntityManager();
        em.getTransaction().begin();
        em.persist(club);
        em.persist(club.getDiscussion());
        em.getTransaction().commit();
        em.close();
        if (ServerConfig.PRINT_DEBUG)
            System.out.printf("Added club: %s\n", club.toString());
        return true;
    }

    public boolean updateClub(Club club) {
        if (club == null || club.getId() == null || !initialized)
            return false;
        EntityManager em = coreFactory.createEntityManager();
        em.getTransaction().begin();
        Club queriedClub = em.find(Club.class, club.getId());
        if (queriedClub == null) {
            em.close();
            return false;
        }
        em.merge(club);
        em.getTransaction().commit();
        em.close();
        if (ServerConfig.PRINT_DEBUG)
            System.out.printf("Updated club: %s\n", club.toString());
        return true;
    }

    public boolean addEvent(Event event) {
        if (event == null || !initialized)
            return false;
        EntityManager em = coreFactory.createEntityManager();
        em.getTransaction().begin();
        em.persist(event);
        em.persist(event.getDiscussion());
        em.getTransaction().commit();
        em.close();
        if (ServerConfig.PRINT_DEBUG)
            System.out.printf("Added event: %s\n", event.toString());
        return true;
    }

    public boolean updateEvent(Event event) {
        if (event == null || event.getId() == null || !initialized)
            return false;
        EntityManager em = coreFactory.createEntityManager();
        em.getTransaction().begin();
        Event queriedEvent = em.find(Event.class, event.getId());
        if (queriedEvent == null) {
            em.close();
            return false;
        }
        em.merge(event);
        em.getTransaction().commit();
        em.close();
        if (ServerConfig.PRINT_DEBUG)
            System.out.printf("Updated event: %s\n", event.toString());
        return true;
    }

    public boolean addComment(Comment comment) {
        if (comment == null || !initialized)
            return false;
        EntityManager em = coreFactory.createEntityManager();
        em.getTransaction().begin();
        em.persist(comment);
        em.getTransaction().commit();
        em.close();
        if (ServerConfig.PRINT_DEBUG)
            System.out.printf("Added comment: %s\n", comment.toString());
        return true;
    }

    public boolean updateComment(Comment comment) {
        if (comment == null || comment.getId() == null || !initialized)
            return false;
        EntityManager em = coreFactory.createEntityManager();
        em.getTransaction().begin();
        Event queriedEvent = em.find(Event.class, comment.getId());
        if (queriedEvent == null) {
            em.close();
            return false;
        }
        em.merge(comment);
        em.getTransaction().commit();
        em.close();
        if (ServerConfig.PRINT_DEBUG)
            System.out.printf("Updated comment: %s\n", comment.toString());
        return true;
    }

    public boolean addDiscussion(Discussion discussion) {
        if (discussion == null || !initialized)
            return false;
        EntityManager em = coreFactory.createEntityManager();
        em.getTransaction().begin();
        em.persist(discussion);
        em.getTransaction().commit();
        em.close();
        if (ServerConfig.PRINT_DEBUG)
            System.out.printf("Added discussion: %s\n", discussion.toString());
        return true;
    }

    public boolean updateDiscussion(Discussion discussion) {
        if (discussion == null || discussion.getId() == null || !initialized)
            return false;
        EntityManager em = coreFactory.createEntityManager();
        em.getTransaction().begin();
        Event queriedEvent = em.find(Event.class, discussion.getId());
        if (queriedEvent == null) {
            em.close();
            return false;
        }
        em.merge(discussion);
        em.getTransaction().commit();
        em.close();
        if (ServerConfig.PRINT_DEBUG)
            System.out.printf("Updated discussion: %s\n", discussion.toString());
        return true;
    }

    public List<Club> queryClubs(Filter filter) {
        if (ServerConfig.PRINT_DEBUG)
            System.out.printf("Queried clubs for filter: %s\n", filter.toString());
        if (!initialized)
            return null;
        Map<String, String> keyMap = new HashMap<String, String>() {
            {
                put("id", "c.getId()");
                put("name", "c.getClubName()");
            }
        };
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT c FROM Club c ");
        boolean hasFilter = false;
        Map<String, Object> filterMap = filter.getMap();

        for (String key : filterMap.keySet()) {
            String format = keyMap.get(key);
            if (format == null)
                continue;
            if (!hasFilter) {
                queryBuilder.append("WHERE ");
                hasFilter = true;
            } else {
                queryBuilder.append("AND ");
            }
            queryBuilder.append(format);
            queryBuilder.append(" = :");
            queryBuilder.append(key);
            queryBuilder.append(" ");
        }
        String queryString = queryBuilder.toString().trim();
        EntityManager em = coreFactory.createEntityManager();
        TypedQuery<Club> query = em.createQuery(queryString, Club.class);
        for (String key : filterMap.keySet()) {
            if (!keyMap.containsKey(key))
                continue;
            query.setParameter(key, filterMap.get(key));
        }
        List<Club> results = query.getResultList();
        em.close();
        return results;
    }

    public Club queryClub(Filter filter) {
        if (!initialized)
            return null;
        List<Club> clubs = queryClubs(filter);
        if (clubs.size() != 1) {
            if (ServerConfig.PRINT_DEBUG)
                System.out.printf("Info: %s returned %s results.\n", filter, clubs.size());
            return null;
        }
        return clubs.get(0);
    }

    public List<Event> queryEvents(Filter filter) {
        if (ServerConfig.PRINT_DEBUG)
            System.out.printf("Queried events for filter: %s\n", filter.toString());
        if (!initialized)
            return null;
        Map<String, String> keyMap = new HashMap<String, String>() {
            {
                put("id", "e.getId() ");
                put("name", "e.getEventName() ");
                put("startDate", "e.getStartEpoch() >"); // if startEpoch > currentDate + 5 days i.e.
                put("endDate", "e.getEndEpoch() <"); // if endEpoch < currentDate i.e.
                // we get events that are in the range of the given start and end dates
            }
        };
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT e FROM Event e ");
        boolean hasFilter = false;
        Map<String, Object> filterMap = filter.getMap();

        for (String key : filterMap.keySet()) {
            String format = keyMap.get(key);
            if (format == null)
                continue;
            if (!hasFilter) {
                queryBuilder.append("WHERE ");
                hasFilter = true;
            } else {
                queryBuilder.append("AND ");
            }
            queryBuilder.append(format);
            queryBuilder.append("= :");
            queryBuilder.append(key);
            queryBuilder.append(" ");
        }
        String queryString = queryBuilder.toString().trim();
        EntityManager em = coreFactory.createEntityManager();
        TypedQuery<Event> query = em.createQuery(queryString, Event.class);
        for (String key : filterMap.keySet()) {
            if (!keyMap.containsKey(key))
                continue;
            query.setParameter(key, filterMap.get(key));
        }
        List<Event> results = query.getResultList();
        em.close();
        return results;
    }

    public Event queryEvent(Filter filter) {
        if (!initialized)
            return null;
        List<Event> events = queryEvents(filter);
        if (events.size() != 1) {
            if (ServerConfig.PRINT_DEBUG)
                System.out.printf("Info: %s returned %s results.\n", filter, events.size());
            return null;
        }
        return events.get(0);
    }

    public List<Comment> queryComments(Filter filter) {
        if (ServerConfig.PRINT_DEBUG)
            System.out.printf("Queried comments for filter: %s\n", filter.toString());
        if (!initialized)
            return null;
        Map<String, String> keyMap = new HashMap<String, String>() {
            {
                put("id", "c.getId() ");
                put("name", "c.getEventName() ");
            }
        };
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT c FROM Comment c ");
        boolean hasFilter = false;
        Map<String, Object> filterMap = filter.getMap();

        for (String key : filterMap.keySet()) {
            String format = keyMap.get(key);
            if (format == null)
                continue;
            if (!hasFilter) {
                queryBuilder.append("WHERE ");
                hasFilter = true;
            } else {
                queryBuilder.append("AND ");
            }
            queryBuilder.append(format);
            queryBuilder.append("= :");
            queryBuilder.append(key);
            queryBuilder.append(" ");
        }
        String queryString = queryBuilder.toString().trim();
        EntityManager em = coreFactory.createEntityManager();
        TypedQuery<Comment> query = em.createQuery(queryString, Comment.class);
        for (String key : filterMap.keySet()) {
            if (!keyMap.containsKey(key))
                continue;
            query.setParameter(key, filterMap.get(key));
        }
        List<Comment> results = query.getResultList();
        em.close();
        return results;
    }

    public Comment queryComment(Filter filter) {
        if (!initialized)
            return null;
        List<Comment> comments = queryComments(filter);
        if (comments.size() != 1) {
            if (ServerConfig.PRINT_DEBUG)
                System.out.printf("Info: %s returned %s results.\n", filter, comments.size());
            return null;
        }
        return comments.get(0);
    }

    public List<Discussion> queryDiscussions(Filter filter) {
        if (ServerConfig.PRINT_DEBUG)
            System.out.printf("Queried discussions for filter: %s\n", filter.toString());
        if (!initialized)
            return null;
        Map<String, String> keyMap = new HashMap<String, String>() {
            {
                put("id", "d.getId() ");
            }
        };
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT d FROM Discussion d ");
        boolean hasFilter = false;
        Map<String, Object> filterMap = filter.getMap();

        for (String key : filterMap.keySet()) {
            String format = keyMap.get(key);
            if (format == null)
                continue;
            if (!hasFilter) {
                queryBuilder.append("WHERE ");
                hasFilter = true;
            } else {
                queryBuilder.append("AND ");
            }
            queryBuilder.append(format);
            queryBuilder.append("= :");
            queryBuilder.append(key);
            queryBuilder.append(" ");
        }
        String queryString = queryBuilder.toString().trim();
        EntityManager em = coreFactory.createEntityManager();
        TypedQuery<Discussion> query = em.createQuery(queryString, Discussion.class);
        for (String key : filterMap.keySet()) {
            if (!keyMap.containsKey(key))
                continue;
            query.setParameter(key, filterMap.get(key));
        }
        List<Discussion> results = query.getResultList();
        em.close();
        return results;
    }

    public Discussion queryDiscussion(Filter filter) {
        if (!initialized)
            return null;
        List<Discussion> discussions = queryDiscussions(filter);
        if (discussions.size() != 1) {
            if (ServerConfig.PRINT_DEBUG)
                System.out.printf("Info: %s returned %s results.\n", filter, discussions.size());
            return null;
        }
        return discussions.get(0);
    }

    public boolean addUserToClub(User newMember, Club club, User manager) {
        if (newMember.isBannedFromClub(club) || !manager.hasClubPrivilege(club, Privileges.MANAGER))
            return false;
        club.addMember(newMember);
        newMember.joinClub(club);
        updateUser(newMember);
        updateClub(club);
        return true;
    }

    public boolean createClub(User admin, User firstManager, String name, String description){
        if (firstManager.isBanned() || admin.hasGeneralPrivilege(Privileges.ADMIN)) return false;
        Club club = new Club(name, description);
        firstManager.joinClub(club);
        firstManager.setClubPrivilege(club, Privileges.MANAGER | Privileges.NORMAL_USER);
        club.addMember(firstManager);
        club.setMemberPrivilege(firstManager, Privileges.MANAGER | Privileges.NORMAL_USER);
        addClub(club);
        addDiscussion(club.getDiscussion());
        updateUser(firstManager);
        return true;
    }

    public boolean removeUserFromClub(User member, Club club) {
        if (member.isBannedFromClub(club))
            return false; // User privileges are erased when they leave a club, therefore banned users
                          // should be prevented from leaving
        club.removeMember(member);
        member.leaveClub(club);
        updateUser(member);
        updateClub(club);
        return true;
    }

    public boolean setUserClubPrivilege(User member, Club club, int privilege, User manager) {
        if (!manager.hasClubPrivilege(club, Privileges.MANAGER))
            return false;
        member.setClubPrivilege(club, privilege);
        club.setMemberPrivilege(member, privilege);
        updateUser(member);
        updateClub(club);
        return true;
    }

    public boolean createEventForClub(User manager, String eventName, Club eventClub, String eventDescription,
            String eventPlace, LocalDateTime eventStart, LocalDateTime eventEnd, int eventQuota) {
        if (!manager.hasClubPrivilege(eventClub, Privileges.MANAGER))
            return false;
        Event newEvent = new Event(eventName, eventClub, eventDescription, eventPlace, eventStart, eventEnd,
                eventQuota);
        eventClub.addEvent(newEvent);
        addEvent(newEvent);
        addDiscussion(newEvent.getDiscussion());
        updateClub(eventClub);
        return true;
    }

    public boolean addUserToEvent(User user, Event event) {
        if (!user.canRegisterToEvent(event))
            return false;
        user.registerToEvent(event);
        event.registerUser(user);
        updateUser(user);
        updateEvent(event);
        return true;
    }

    public boolean removeUserFromEvent(User user, Event event) {
        if (!user.isRegisteredToEvent(event))
            return false;
        user.leaveEvent(event);
        event.removeUser(user);
        updateUser(user);
        updateEvent(event);
        return true;
    }

    public boolean postComment(Comment parent, User author, String content, Discussion discussion){
        if (author.isBanned() || content.isEmpty()) return false;
        Comment comment = new Comment(parent, author, content, discussion);
        discussion.addNewComment(comment);
        addComment(comment);
        if (parent != null) updateComment(parent);
        updateDiscussion(discussion);
        return true;
    }

    public boolean editComment(Comment comment, User author, String newContent){
        if (author != comment.getAuthor() || author.isBanned() || newContent.isEmpty()) return false;
        comment.setContent(newContent);
        updateComment(comment);
        return true;
    }

    public boolean deleteComment(Comment comment, User author){
        if (author != comment.getAuthor() || author.isBanned()) return false;
        comment.getDiscussion().deleteComment(comment.getId());
        updateDiscussion(comment.getDiscussion());
        // FIXME: After deleting a comment the comment will still be stored within
        // the ODB file because we haven't implemented Delete operations
        // If you don't access Comments via Discussions this may cause bugs...
        return true;
    }

    // Files

    public boolean addFile(Media media) {
        if (!initialized)
            return false;
        EntityManager fileManager = fileManagerFactory.createEntityManager();
        fileManager.getTransaction().begin();
        fileManager.persist(media);
        fileManager.getTransaction().commit();
        fileManager.close();
        if (ServerConfig.PRINT_DEBUG)
            System.out.printf("Added file: %s\n", media.toString());
        return true;
    }

    public List<Media> queryFiles(Filter filter) {
        if (ServerConfig.PRINT_DEBUG)
            System.out.printf("Queried files for filter: %s\n", filter.toString());
        if (!initialized)
            return null;
        Map<String, String> keyMap = new HashMap<String, String>() {
            {
                put("id", "u.getId()");
                put("realName", "u.getRealFileName()");
                put("storedName", "u.getStoredFileName()");
            }
        };
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT u FROM Media u ");
        boolean hasFilter = false;
        Map<String, Object> filterMap = filter.getMap();

        for (String key : filterMap.keySet()) {
            String format = keyMap.get(key);
            if (format == null)
                continue;
            if (!hasFilter) {
                queryBuilder.append("WHERE ");
                hasFilter = true;
            } else {
                queryBuilder.append("AND ");
            }
            queryBuilder.append(format);
            queryBuilder.append(" = :");
            queryBuilder.append(key);
            queryBuilder.append(" ");
        }

        String queryString = queryBuilder.toString().trim();
        EntityManager fileManager = fileManagerFactory.createEntityManager();
        TypedQuery<Media> query = fileManager.createQuery(queryString, Media.class);
        for (String key : filterMap.keySet()) {
            if (!keyMap.containsKey(key))
                continue;
            query.setParameter(key, filterMap.get(key));
        }
        List<Media> results = query.getResultList();
        fileManager.close();
        return results;
    }

    public Media queryFile(Filter filter) {
        if (!initialized)
            return null;
        List<Media> files = queryFiles(filter);
        if (files.size() != 1) {
            if (ServerConfig.PRINT_DEBUG)
                System.out.printf("Info: %s returned %s results.\n", filter, files.size());
            return null;
        }
        return files.get(0);
    }
}
