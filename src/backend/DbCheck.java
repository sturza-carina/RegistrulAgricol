import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbCheck {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/multi_tenant_db";
        String user = "admin";
        String password = "admin_password";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {

            System.out.println("--- Public Schema Tables ---");
            ResultSet rsPublic = stmt.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema='public'");
            while (rsPublic.next()) {
                System.out.println(rsPublic.getString(1));
            }

            System.out.println("\n--- Tenant Cluj Schema Tables ---");
            ResultSet rsCluj = stmt.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema='tenant_cluj'");
            while (rsCluj.next()) {
                System.out.println(rsCluj.getString(1));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
