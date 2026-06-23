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
    private final com.multitenant.repository.PersoanaRepository persoanaRepository;
    private final com.multitenant.repository.CladireRepository cladireRepository;
    private final com.multitenant.repository.MachineryRepository machineryRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(TenantService tenantService,
                          UatRepository uatRepository,
                          UserRepository userRepository,
                          GospodarieRepository gospodarieRepository,
                          com.multitenant.repository.TerenRepository terenRepository,
                          com.multitenant.repository.ParcelaRepository parcelaRepository,
                          com.multitenant.repository.PersoanaRepository persoanaRepository,
                          com.multitenant.repository.CladireRepository cladireRepository,
                          com.multitenant.repository.MachineryRepository machineryRepository,
                          PasswordEncoder passwordEncoder) {
        this.tenantService = tenantService;
        this.uatRepository = uatRepository;
        this.userRepository = userRepository;
        this.gospodarieRepository = gospodarieRepository;
        this.terenRepository = terenRepository;
        this.parcelaRepository = parcelaRepository;
        this.persoanaRepository = persoanaRepository;
        this.cladireRepository = cladireRepository;
        this.machineryRepository = machineryRepository;
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
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

                    for (int i = 1; i <= 10; i++) {
                        Gospodarie g = new Gospodarie();
                        g.setCodGospodarie("CJ-GOSP-" + String.format("%03d", i));
                        Adresa a = new Adresa();
                        a.setStreet("Str. Observatorului");
                        a.setStreetNumber(String.valueOf(i));
                        g.setAdresa(a);
                        g.setTipGospodarie(i % 2 == 0 ? TipGospodarie.INDIVIDUALA : TipGospodarie.COLECTIVA);
                        g.setActiva(true);
                        g.setUat(clujNapoca);
                        Gospodarie savedG = gospodarieRepository.save(g);

                        com.multitenant.model.registru.Teren t = new com.multitenant.model.registru.Teren();
                        t.setDenumire("Teren Agricol CJ " + i);
                        t.setGospodarie(savedG);
                        t.setTipTeren("Extravilan");
                        t.setStereo70Coordinates("591250 390800\n591350 390800\n591350 390900\n591250 390900");
                        String tJson = "{\"type\": \"Feature\", \"geometry\": {\"type\": \"Polygon\", \"coordinates\": [[[23.569717, 46.810456], [23.569694, 46.811354], [23.571001, 46.811370], [23.571025, 46.810472], [23.569717, 46.810456]]]}, \"properties\": {}}";
                        t.setPolygon(mapper.readTree(tJson));
                        t = terenRepository.save(t);

                        com.multitenant.model.registru.Parcela p = new com.multitenant.model.registru.Parcela();
                        p.setDenumire("Parcela CJ " + i);
                        p.setSuprafata(1.0 + (i * 0.1));
                        p.setCategorieFolosinta(i % 2 == 0 ? "Arabil" : "Livada");
                        p.setTeren(t);
                        String pJson = "{\"type\": \"Feature\", \"geometry\": {\"type\": \"Polygon\", \"coordinates\": [[[23.569717, 46.810456], [23.569705, 46.810905], [23.571013, 46.810921], [23.571025, 46.810472], [23.569717, 46.810456]]]}, \"properties\": {}}";
                        p.setPolygon(mapper.readTree(pJson));
                        parcelaRepository.save(p);

                        com.multitenant.model.persoana.PersoanaFizica persoana = new com.multitenant.model.persoana.PersoanaFizica();
                        persoana.setFirstName("Ion" + i);
                        persoana.setLastName("Popescu CJ");
                        persoana.setCnp(String.format("190010112%04d", i));
                        persoana.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1).plusDays(i));
                        persoana.setAdresa(a);
                        persoana.getGospodarii().add(savedG);
                        persoana.setIsHeadOfHousehold(true);
                        persoanaRepository.save(persoana);

                        com.multitenant.model.registru.Cladire cladire = new com.multitenant.model.registru.Cladire();
                        cladire.setDestinatie("Locuinta");
                        cladire.setSuprafataConstruita(100.0 + i * 5);
                        cladire.setAnTerminare(2000 + i);
                        cladire.setMateriale("Caramida");
                        cladire.setGospodarie(savedG);
                        cladireRepository.save(cladire);

                        com.multitenant.model.registru.Machinery machinery = new com.multitenant.model.registru.Machinery();
                        machinery.setTipUtilaj("Tractor");
                        machinery.setMarca("John Deere");
                        machinery.setModel("X" + i);
                        machinery.setAnFabricatie(2010 + i);
                        machinery.setNumarInmatriculare(String.format("CJ-%02d-TRC", i));
                        machinery.setGospodarie(savedG);
                        machineryRepository.save(machinery);
                    }
                }
                
                // Switch current thread to "bucuresti" tenant context for entity seeding
                TenantContext.setCurrentTenant("bucuresti");
                System.out.println("[DatabaseSeeder] Seeding Households (Gospodarii) for Bucuresti...");
                if (gospodarieRepository.count() == 0) {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

                    for (int i = 1; i <= 10; i++) {
                        Gospodarie g = new Gospodarie();
                        g.setCodGospodarie("BUC-GOSP-" + String.format("%03d", i));
                        Adresa a = new Adresa();
                        a.setStreet("Calea Victoriei");
                        a.setStreetNumber(String.valueOf(i * 10));
                        g.setAdresa(a);
                        g.setTipGospodarie(i % 2 == 0 ? TipGospodarie.COLECTIVA : TipGospodarie.INDIVIDUALA);
                        g.setActiva(true);
                        g.setUat(bucurestiUat);
                        Gospodarie savedG = gospodarieRepository.save(g);

                        com.multitenant.model.registru.Teren t = new com.multitenant.model.registru.Teren();
                        t.setDenumire("Teren Agricol BUC " + i);
                        t.setGospodarie(savedG);
                        t.setTipTeren("Intravilan");
                        String tJson = "{\"type\": \"Feature\", \"geometry\": {\"type\": \"Polygon\", \"coordinates\": [[[26.1000, 44.4250], [26.1000, 44.4270], [26.1050, 44.4270], [26.1050, 44.4250], [26.1000, 44.4250]]]}, \"properties\": {}}";
                        t.setPolygon(mapper.readTree(tJson));
                        t = terenRepository.save(t);

                        com.multitenant.model.registru.Parcela p = new com.multitenant.model.registru.Parcela();
                        p.setDenumire("Parcela BUC " + i);
                        p.setSuprafata(0.5 + (i * 0.05));
                        p.setCategorieFolosinta(i % 2 == 0 ? "Pasune" : "Arabil");
                        p.setTeren(t);
                        String pJson = "{\"type\": \"Feature\", \"geometry\": {\"type\": \"Polygon\", \"coordinates\": [[[26.1000, 44.4250], [26.1000, 44.4270], [26.1025, 44.4270], [26.1025, 44.4250], [26.1000, 44.4250]]]}, \"properties\": {}}";
                        p.setPolygon(mapper.readTree(pJson));
                        parcelaRepository.save(p);

                        com.multitenant.model.persoana.PersoanaFizica persoana = new com.multitenant.model.persoana.PersoanaFizica();
                        persoana.setFirstName("Vasile" + i);
                        persoana.setLastName("Ionescu BUC");
                        persoana.setCnp(String.format("180020223%04d", i));
                        persoana.setDateOfBirth(java.time.LocalDate.of(1980, 2, 2).plusDays(i));
                        persoana.setAdresa(a);
                        persoana.getGospodarii().add(savedG);
                        persoana.setIsHeadOfHousehold(true);
                        persoanaRepository.save(persoana);

                        com.multitenant.model.registru.Cladire cladire = new com.multitenant.model.registru.Cladire();
                        cladire.setDestinatie("Anexa");
                        cladire.setSuprafataConstruita(50.0 + i * 2);
                        cladire.setAnTerminare(1990 + i);
                        cladire.setMateriale("Lemn");
                        cladire.setGospodarie(savedG);
                        cladireRepository.save(cladire);

                        com.multitenant.model.registru.Machinery machinery = new com.multitenant.model.registru.Machinery();
                        machinery.setTipUtilaj("Combina");
                        machinery.setMarca("Claas");
                        machinery.setModel("Lexion " + i);
                        machinery.setAnFabricatie(2015 + i);
                        machinery.setNumarInmatriculare(String.format("B-%02d-CMB", i));
                        machinery.setGospodarie(savedG);
                        machineryRepository.save(machinery);
                    }
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
