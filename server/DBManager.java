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

    public boolean addUserToClub(User newMember, Club club, User manager) {
        if (newMember.isBannedFromClub(club) || !manager.hasClubPrivilege(club, Privileges.MANAGER))
            return false;
        club.addMember(newMember);
        newMember.joinClub(club);
        updateUser(newMember);
        updateClub(club);
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
