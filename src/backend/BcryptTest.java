import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("Matches: " + encoder.matches("superadmin", "$2a$10$wE1mG1h8/r5q9aK5/r6/GOCvU33f9m6m/G.s8uT0s8P9X00V2YmUa"));
        System.out.println("New hash: " + encoder.encode("superadmin"));
    }
}
