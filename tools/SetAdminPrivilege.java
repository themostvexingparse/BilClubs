import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Scanner;

public class SetAdminPrivilege {

    private static final int TARGET_PRIVILEGE = 15;

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter user email: ");
        String email = scanner.nextLine().trim();

        if (email.isEmpty()) {
            System.out.println("[!] Email cannot be empty.");
            return;
        }

        EntityManagerFactory factory = Persistence.createEntityManagerFactory("objectdb:server/db/bilclubs.odb");
        EntityManager em = factory.createEntityManager();

        TypedQuery<User> query = em.createQuery(
            "SELECT u FROM User u WHERE u.getEmail() = :email", User.class
        );
        query.setParameter("email", email);
        List<User> results = query.getResultList();

        if (results.isEmpty()) {
            System.out.println("[!] No user found with email: " + email);
            em.close();
            factory.close();
            return;
        }

        if (results.size() > 1) {
            System.out.println("[!] Multiple users found for that email. Database may be inconsistent.");
            em.close();
            factory.close();
            return;
        }

        User user = results.get(0);
        int oldPrivilege = user.getPrivilege();

        System.out.printf("[+] Found: %s (id=%d, current privilege=%d)%n",
            user.getFullName(), user.getId(), oldPrivilege);

        Field privilegeField = User.class.getDeclaredField("privileges");
        privilegeField.setAccessible(true);

        em.getTransaction().begin();
        User managed = em.find(User.class, user.getId());
        privilegeField.set(managed, TARGET_PRIVILEGE);
        em.merge(managed);
        em.getTransaction().commit();

        em.clear();
        User verify = em.find(User.class, user.getId());
        int newPrivilege = verify.getPrivilege();

        em.close();
        factory.close();

        if (newPrivilege == TARGET_PRIVILEGE) {
            System.out.printf("[+] Done. %s privilege updated: %d -> %d%n",
                user.getFullName(), oldPrivilege, newPrivilege);
        } else {
            System.out.printf("[!] Write appeared to succeed but re-read returned %d. Check the database.%n",
                newPrivilege);
        }
    }
}
