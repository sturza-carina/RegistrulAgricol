package com.multitenant.config;

import com.multitenant.config.tenant.TenantContext;
import com.multitenant.model.Gospodarie;
import com.multitenant.model.Adresa;
import com.multitenant.model.TipGospodarie;
import com.multitenant.model.Uat;
import com.multitenant.model.User;
import com.multitenant.repository.GospodarieRepository;
import com.multitenant.repository.TenantRepository;
import com.multitenant.repository.UatRepository;
import com.multitenant.repository.UserRepository;
import com.multitenant.service.TenantService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final TenantRepository tenantRepository;
    private final TenantService tenantService;
    private final UatRepository uatRepository;
    private final UserRepository userRepository;
    private final GospodarieRepository gospodarieRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(TenantRepository tenantRepository,
                          TenantService tenantService,
                          UatRepository uatRepository,
                          UserRepository userRepository,
                          GospodarieRepository gospodarieRepository,
                          PasswordEncoder passwordEncoder) {
        this.tenantRepository = tenantRepository;
        this.tenantService = tenantService;
        this.uatRepository = uatRepository;
        this.userRepository = userRepository;
        this.gospodarieRepository = gospodarieRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("[DatabaseSeeder] Running migrations for existing tenants...");
        tenantService.migrateAllTenants();

        // Ensure superadmin exists in public schema
        if (userRepository.findByUsername("superadmin").isEmpty()) {
            System.out.println("[DatabaseSeeder] Seeding superadmin user...");
            User superadmin = new User();
            superadmin.setUsername("superadmin");
            superadmin.setPassword(passwordEncoder.encode("superadmin"));
            superadmin.setRole("ROLE_SUPER_ADMIN");
            superadmin.setNume("Super Administrator");
            superadmin.setEmail("superadmin@registru.ro");
            superadmin.setActiv(true);
            userRepository.save(superadmin);
        }

        // Check if there are any tenants in the public schema
        if (tenantRepository.count() == 0) {
            System.out.println("[DatabaseSeeder] Seeding initial development tenant...");
            
            // Create default tenant "cluj" (this creates the schema and runs Flyway on it)
            tenantService.createTenant("cluj", "Cluj-Napoca");
            
            // Seed tenant users in public schema (required for authentication/login)
            System.out.println("[DatabaseSeeder] Seeding tenant users in public schema...");
            if (userRepository.findByUsername("cluj_admin").isEmpty()) {
                User publicAdmin = new User();
                publicAdmin.setUsername("cluj_admin");
                publicAdmin.setPassword(passwordEncoder.encode("password123"));
                publicAdmin.setRole("ROLE_ADMIN");
                publicAdmin.setNume("Administrator Local");
                publicAdmin.setEmail("admin.cluj@registru.ro");
                publicAdmin.setActiv(true);
                publicAdmin.setTenantId("cluj");
                userRepository.save(publicAdmin);
            }

            if (userRepository.findByUsername("cluj_user").isEmpty()) {
                User publicUser = new User();
                publicUser.setUsername("cluj_user");
                publicUser.setPassword(passwordEncoder.encode("password123"));
                publicUser.setRole("ROLE_USER");
                publicUser.setNume("Operator Registru");
                publicUser.setEmail("operator.cluj@registru.ro");
                publicUser.setActiv(true);
                publicUser.setTenantId("cluj");
                userRepository.save(publicUser);
            }

            // Switch current thread to "cluj" tenant context for entity seeding
            TenantContext.setCurrentTenant("cluj");
            
            try {
                System.out.println("[DatabaseSeeder] Seeding UAT...");
                Uat uat = uatRepository.findByCodSiruta("54975").orElseGet(() -> {
                    Uat newUat = new Uat();
                    newUat.setCodSiruta("54975");
                    newUat.setDenumire("Cluj-Napoca");
                    newUat.setJudet("Cluj");
                    newUat.setTipUat("MUNICIPIU");
                    return uatRepository.save(newUat);
                });
                
                System.out.println("[DatabaseSeeder] Seeding Tenant Users in tenant schema...");
                // Tenant Admin user
                if (userRepository.findByUsername("cluj_admin").isEmpty()) {
                    User admin = new User();
                    admin.setUsername("cluj_admin");
                    admin.setPassword(passwordEncoder.encode("password123"));
                    admin.setRole("ROLE_ADMIN");
                    admin.setNume("Administrator Local");
                    admin.setEmail("admin.cluj@registru.ro");
                    admin.setActiv(true);
                    admin.setUat(uat);
                    admin.setTenantId("cluj");
                    userRepository.save(admin);
                }

                // Tenant Regular user
                if (userRepository.findByUsername("cluj_user").isEmpty()) {
                    User user = new User();
                    user.setUsername("cluj_user");
                    user.setPassword(passwordEncoder.encode("password123"));
                    user.setRole("ROLE_USER");
                    user.setNume("Operator Registru");
                    user.setEmail("operator.cluj@registru.ro");
                    user.setActiv(true);
                    user.setUat(uat);
                    user.setTenantId("cluj");
                    userRepository.save(user);
                }
                
                System.out.println("[DatabaseSeeder] Seeding Households (Gospodarii)...");
                if (gospodarieRepository.count() == 0) {
                    Gospodarie g1 = new Gospodarie();
                    g1.setCodGospodarie("GOSP-001");
                    Adresa a1 = new Adresa();
                    a1.setStreet("Str. Observatorului");
                    a1.setStreetNumber("2");
                    g1.setAdresa(a1);
                    g1.setTipGospodarie(TipGospodarie.INDIVIDUALA);
                    g1.setActiva(true);
                    g1.setUat(uat);
                    gospodarieRepository.save(g1);

                    Gospodarie g2 = new Gospodarie();
                    g2.setCodGospodarie("GOSP-002");
                    Adresa a2 = new Adresa();
                    a2.setStreet("Calea Turzii");
                    a2.setStreetNumber("150");
                    g2.setAdresa(a2);
                    g2.setTipGospodarie(TipGospodarie.COLECTIVA);
                    g2.setActiva(true);
                    g2.setUat(uat);
                    gospodarieRepository.save(g2);
                }

                System.out.println("[DatabaseSeeder] Development data successfully seeded.");
            } catch (Exception e) {
                System.err.println("[DatabaseSeeder] Error seeding development data: " + e.getMessage());
                e.printStackTrace();
            } finally {
                // Clean up context to avoid side effects
                TenantContext.clear();
            }
        }
    }
}
