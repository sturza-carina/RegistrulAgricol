package com.multitenant.config;

import com.multitenant.config.tenant.TenantContext;
import com.multitenant.model.registru.Gospodarie;
import com.multitenant.model.common.Adresa;
import com.multitenant.model.registru.TipGospodarie;
import com.multitenant.model.core.Uat;
import com.multitenant.model.core.User;
import com.multitenant.repository.GospodarieRepository;
import com.multitenant.repository.UatRepository;
import com.multitenant.repository.UserRepository;
import com.multitenant.service.TenantService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final TenantService tenantService;
    private final UatRepository uatRepository;
    private final UserRepository userRepository;
    private final GospodarieRepository gospodarieRepository;
    private final com.multitenant.repository.TerenRepository terenRepository;
    private final com.multitenant.repository.ParcelaRepository parcelaRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(TenantService tenantService,
                          UatRepository uatRepository,
                          UserRepository userRepository,
                          GospodarieRepository gospodarieRepository,
                          com.multitenant.repository.TerenRepository terenRepository,
                          com.multitenant.repository.ParcelaRepository parcelaRepository,
                          PasswordEncoder passwordEncoder) {
        this.tenantService = tenantService;
        this.uatRepository = uatRepository;
        this.userRepository = userRepository;
        this.gospodarieRepository = gospodarieRepository;
        this.terenRepository = terenRepository;
        this.parcelaRepository = parcelaRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        TenantContext.setCurrentTenant("public");

        System.out.println("[DatabaseSeeder] Running migrations for existing tenants...");
        tenantService.migrateAllTenants();

        TenantContext.setCurrentTenant("public");

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

        // Tenant schemas are created and migrated natively by TenantService.migrateAllTenants()
            // Tenant schemas are created and migrated natively by TenantService.migrateAllTenants() 
            // the UATs and Tenants have been inserted by the V3 migration.
            
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

            try {
                System.out.println("[DatabaseSeeder] Seeding Tenant Users in tenant schema...");
                
                Uat clujNapoca = uatRepository.findByCodSiruta("54975").orElseThrow();
                Uat bucurestiUat = uatRepository.findByCodSiruta("1017").orElseThrow();

                // Tenant Admin user Cluj
                if (userRepository.findByUsername("cluj_admin").isEmpty()) {
                    User admin = new User();
                    admin.setUsername("cluj_admin");
                    admin.setPassword(passwordEncoder.encode("password123"));
                    admin.setRole("ROLE_ADMIN");
                    admin.setNume("Administrator Local Cluj");
                    admin.setEmail("admin.cluj@registru.ro");
                    admin.setActiv(true);
                    admin.setUat(clujNapoca);
                    admin.setTenantId("cluj");
                    userRepository.save(admin);
                }

                // Tenant Regular user Cluj
                if (userRepository.findByUsername("cluj_user").isEmpty()) {
                    User user = new User();
                    user.setUsername("cluj_user");
                    user.setPassword(passwordEncoder.encode("password123"));
                    user.setRole("ROLE_USER");
                    user.setNume("Operator Registru Cluj");
                    user.setEmail("operator.cluj@registru.ro");
                    user.setActiv(true);
                    user.setUat(clujNapoca);
                    user.setTenantId("cluj");
                    userRepository.save(user);
                }

                // Tenant Admin user Bucuresti
                if (userRepository.findByUsername("buc_admin").isEmpty()) {
                    User adminBuc = new User();
                    adminBuc.setUsername("buc_admin");
                    adminBuc.setPassword(passwordEncoder.encode("password123"));
                    adminBuc.setRole("ROLE_ADMIN");
                    adminBuc.setNume("Administrator Local Bucuresti");
                    adminBuc.setEmail("admin.buc@registru.ro");
                    adminBuc.setActiv(true);
                    adminBuc.setUat(bucurestiUat);
                    adminBuc.setTenantId("bucuresti");
                    userRepository.save(adminBuc);
                }

                // Switch current thread to "cluj" tenant context for entity seeding
                TenantContext.setCurrentTenant("cluj");
                System.out.println("[DatabaseSeeder] Seeding Households (Gospodarii) for Cluj...");
                if (gospodarieRepository.count() == 0) {
                    Gospodarie g1 = new Gospodarie();
                    g1.setCodGospodarie("GOSP-001");
                    Adresa a1 = new Adresa();
                    a1.setStreet("Str. Observatorului");
                    a1.setStreetNumber("2");
                    g1.setAdresa(a1);
                    g1.setTipGospodarie(TipGospodarie.INDIVIDUALA);
                    g1.setActiva(true);
                    g1.setUat(clujNapoca);
                    Gospodarie savedG1 = gospodarieRepository.save(g1);
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

                    com.multitenant.model.registru.Teren t1 = new com.multitenant.model.registru.Teren();
                    t1.setDenumire("Teren Agricol " + savedG1.getCodGospodarie());
                    t1.setGospodarie(savedG1);
                    t1.setTipTeren("Extravilan");
                    t1.setStereo70Coordinates("591250 390800\n591350 390800\n591350 390900\n591250 390900");
                    String t1Json = "{\"type\": \"Feature\", \"geometry\": {\"type\": \"Polygon\", \"coordinates\": [[[23.569717, 46.810456], [23.569694, 46.811354], [23.571001, 46.811370], [23.571025, 46.810472], [23.569717, 46.810456]]]}, \"properties\": {}}";
                    t1.setPolygon(mapper.readTree(t1Json));
                    t1 = terenRepository.save(t1);

                    com.multitenant.model.registru.Parcela p1 = new com.multitenant.model.registru.Parcela();
                    p1.setDenumire("Parcela Grau");
                    p1.setSuprafata(1.0);
                    p1.setCategorieFolosinta("Arabil");
                    p1.setTeren(t1);
                    String p1Json = "{\"type\": \"Feature\", \"geometry\": {\"type\": \"Polygon\", \"coordinates\": [[[23.569717, 46.810456], [23.569705, 46.810905], [23.571013, 46.810921], [23.571025, 46.810472], [23.569717, 46.810456]]]}, \"properties\": {}}";
                    p1.setPolygon(mapper.readTree(p1Json));
                    parcelaRepository.save(p1);

                    com.multitenant.model.registru.Parcela p2 = new com.multitenant.model.registru.Parcela();
                    p2.setDenumire("Parcela Meri");
                    p2.setSuprafata(1.0);
                    p2.setCategorieFolosinta("Livada");
                    p2.setTeren(t1);
                    String p2Json = "{\"type\": \"Feature\", \"geometry\": {\"type\": \"Polygon\", \"coordinates\": [[[23.569705, 46.810905], [23.569694, 46.811354], [23.571001, 46.811370], [23.571013, 46.810921], [23.569705, 46.810905]]]}, \"properties\": {}}";
                    p2.setPolygon(mapper.readTree(p2Json));
                    parcelaRepository.save(p2);
                }
                
                // Switch current thread to "bucuresti" tenant context for entity seeding
                TenantContext.setCurrentTenant("bucuresti");
                System.out.println("[DatabaseSeeder] Seeding Households (Gospodarii) for Bucuresti...");
                if (gospodarieRepository.count() == 0) {
                    Gospodarie g2 = new Gospodarie();
                    g2.setCodGospodarie("BUC-001");
                    Adresa a2 = new Adresa();
                    a2.setStreet("Calea Victoriei");
                    a2.setStreetNumber("150");
                    g2.setAdresa(a2);
                    g2.setTipGospodarie(TipGospodarie.COLECTIVA);
                    g2.setActiva(true);
                    g2.setUat(bucurestiUat);
                    Gospodarie savedG2 = gospodarieRepository.save(g2);
                    
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    
                    com.multitenant.model.registru.Teren t2 = new com.multitenant.model.registru.Teren();
                    t2.setDenumire("Teren Agricol " + savedG2.getCodGospodarie());
                    t2.setGospodarie(savedG2);
                    t2.setTipTeren("Intravilan");
                    String t2Json = "{\"type\": \"Feature\", \"geometry\": {\"type\": \"Polygon\", \"coordinates\": [[[26.1000, 44.4250], [26.1000, 44.4270], [26.1050, 44.4270], [26.1050, 44.4250], [26.1000, 44.4250]]]}, \"properties\": {}}";
                    t2.setPolygon(mapper.readTree(t2Json));
                    t2 = terenRepository.save(t2);

                    com.multitenant.model.registru.Parcela p3 = new com.multitenant.model.registru.Parcela();
                    p3.setDenumire("Parcela Stanga");
                    p3.setSuprafata(0.5);
                    p3.setCategorieFolosinta("Arabil");
                    p3.setTeren(t2);
                    String p3Json = "{\"type\": \"Feature\", \"geometry\": {\"type\": \"Polygon\", \"coordinates\": [[[26.1000, 44.4250], [26.1000, 44.4270], [26.1025, 44.4270], [26.1025, 44.4250], [26.1000, 44.4250]]]}, \"properties\": {}}";
                    p3.setPolygon(mapper.readTree(p3Json));
                    parcelaRepository.save(p3);

                    com.multitenant.model.registru.Parcela p4 = new com.multitenant.model.registru.Parcela();
                    p4.setDenumire("Parcela Dreapta");
                    p4.setSuprafata(0.5);
                    p4.setCategorieFolosinta("Pasune");
                    p4.setTeren(t2);
                    String p4Json = "{\"type\": \"Feature\", \"geometry\": {\"type\": \"Polygon\", \"coordinates\": [[[26.1025, 44.4250], [26.1025, 44.4270], [26.1050, 44.4270], [26.1050, 44.4250], [26.1025, 44.4250]]]}, \"properties\": {}}";
                    p4.setPolygon(mapper.readTree(p4Json));
                    parcelaRepository.save(p4);
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
