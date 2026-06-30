package com.multitenant.config;

import com.multitenant.config.tenant.TenantContext;
import com.multitenant.model.registru.Gospodarie;
import com.multitenant.model.common.Adresa;
import com.multitenant.model.registru.TipGospodarie;
import com.multitenant.model.core.User;
import com.multitenant.repository.GospodarieRepository;
import com.multitenant.repository.UserRepository;
import com.multitenant.service.TenantService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final TenantService tenantService;
    private final UserRepository userRepository;
    private final GospodarieRepository gospodarieRepository;
    private final com.multitenant.repository.TerenRepository terenRepository;
    private final com.multitenant.repository.ParcelaRepository parcelaRepository;
    private final com.multitenant.repository.PersoanaRepository persoanaRepository;
    private final com.multitenant.repository.CladireRepository cladireRepository;
    private final com.multitenant.repository.MachineryRepository machineryRepository;
    private final com.multitenant.repository.UatRepository uatRepository;
    private final com.multitenant.repository.PublicUatRepository publicUatRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(TenantService tenantService,
                          UserRepository userRepository,
                          GospodarieRepository gospodarieRepository,
                          com.multitenant.repository.TerenRepository terenRepository,
                          com.multitenant.repository.ParcelaRepository parcelaRepository,
                          com.multitenant.repository.PersoanaRepository persoanaRepository,
                          com.multitenant.repository.CladireRepository cladireRepository,
                          com.multitenant.repository.MachineryRepository machineryRepository,
                          com.multitenant.repository.UatRepository uatRepository,
                          com.multitenant.repository.PublicUatRepository publicUatRepository,
                          PasswordEncoder passwordEncoder) {
        this.tenantService = tenantService;
        this.userRepository = userRepository;
        this.gospodarieRepository = gospodarieRepository;
        this.terenRepository = terenRepository;
        this.parcelaRepository = parcelaRepository;
        this.persoanaRepository = persoanaRepository;
        this.cladireRepository = cladireRepository;
        this.machineryRepository = machineryRepository;
        this.uatRepository = uatRepository;
        this.publicUatRepository = publicUatRepository;
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
            
            try {
                // Seed tenant users in public schema (required for authentication/login)
                System.out.println("[DatabaseSeeder] Seeding tenant users in public schema...");
                // UATs are now in tenant schemas, not in public — reference by a known ID value
                // These IDs correspond to the UATs seeded in each tenant's V1 migration (if applicable)
                Long clujNapocaUatId = 1L;
                Long bucurestiUatId = 1L;

            if (userRepository.findByUsername("cluj_admin").isEmpty()) {
                User publicAdmin = new User();
                publicAdmin.setUsername("cluj_admin");
                publicAdmin.setPassword(passwordEncoder.encode("password123"));
                publicAdmin.setRole("ROLE_ADMIN");
                publicAdmin.setNume("Administrator Local Cluj");
                publicAdmin.setEmail("admin.cluj@registru.ro");
                publicAdmin.setActiv(true);
                publicAdmin.setUatId(clujNapocaUatId);
                publicAdmin.setTenantId("cluj");
                userRepository.save(publicAdmin);
            }

            if (userRepository.findByUsername("cluj_user").isEmpty()) {
                User publicUser = new User();
                publicUser.setUsername("cluj_user");
                publicUser.setPassword(passwordEncoder.encode("password123"));
                publicUser.setRole("ROLE_USER");
                publicUser.setNume("Operator Registru Cluj");
                publicUser.setEmail("operator.cluj@registru.ro");
                publicUser.setActiv(true);
                publicUser.setUatId(clujNapocaUatId);
                publicUser.setTenantId("cluj");
                userRepository.save(publicUser);
            }

            if (userRepository.findByUsername("buc_admin").isEmpty()) {
                User adminBuc = new User();
                adminBuc.setUsername("buc_admin");
                adminBuc.setPassword(passwordEncoder.encode("password123"));
                adminBuc.setRole("ROLE_ADMIN");
                adminBuc.setNume("Administrator Local Bucuresti");
                adminBuc.setEmail("admin.buc@registru.ro");
                adminBuc.setActiv(true);
                adminBuc.setUatId(bucurestiUatId);
                adminBuc.setTenantId("bucuresti");
                userRepository.save(adminBuc);
            }

                // Switch current thread to "cluj" tenant context for entity seeding
                TenantContext.setCurrentTenant("cluj");
                System.out.println("[DatabaseSeeder] Seeding UATs and Households for Cluj...");
                
                com.multitenant.model.core.Uat clujNapoca = null;
                if (!uatRepository.existsByCodSiruta("54975")) {
                    com.multitenant.model.core.PublicUat pUat = publicUatRepository.findByCodSiruta("54975").orElse(null);
                    if (pUat != null) {
                        clujNapoca = new com.multitenant.model.core.Uat();
                        clujNapoca.setCodSiruta(pUat.getCodSiruta());
                        clujNapoca.setDenumire(pUat.getDenumire());
                        clujNapoca.setJudet(pUat.getJudet());
                        clujNapoca.setTipUat(pUat.getTipUat());
                        clujNapoca.setIsActive(true);
                        clujNapoca = uatRepository.save(clujNapoca);
                        
                        pUat.setTenantId("cluj");
                        publicUatRepository.save(pUat);
                    }
                } else {
                    clujNapoca = uatRepository.findByCodSiruta("54975").orElse(null);
                }

                if (!uatRepository.existsByCodSiruta("57706")) {
                    com.multitenant.model.core.PublicUat pUat = publicUatRepository.findByCodSiruta("57706").orElse(null);
                    if (pUat != null) {
                        com.multitenant.model.core.Uat floresti = new com.multitenant.model.core.Uat();
                        floresti.setCodSiruta(pUat.getCodSiruta());
                        floresti.setDenumire(pUat.getDenumire());
                        floresti.setJudet(pUat.getJudet());
                        floresti.setTipUat(pUat.getTipUat());
                        floresti.setIsActive(true);
                        uatRepository.save(floresti);
                        
                        pUat.setTenantId("cluj");
                        publicUatRepository.save(pUat);
                    }
                }

                if (gospodarieRepository.count() == 0 && clujNapoca != null) {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

                    for (int i = 1; i <= 10; i++) {
                        Gospodarie g = new Gospodarie();
                        g.setCodGospodarie("CJ-GOSP-" + String.format("%03d", i));
                        Adresa a = new Adresa();
                        a.setStreet("Str. Observatorului");
                        a.setStreetNumber(String.valueOf(i));
                        a.setCounty("Cluj");
                        a.setLocalitate("Cluj-Napoca");
                        a.setBuilding("C" + i);
                        a.setStaircase("A");
                        a.setFloor(i % 4);
                        a.setApartmentNumber(i * 2);
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
                System.out.println("[DatabaseSeeder] Seeding UATs and Households for Bucuresti...");
                
                com.multitenant.model.core.Uat bucuresti = null;
                if (!uatRepository.existsByCodSiruta("1017")) {
                    com.multitenant.model.core.PublicUat pUat = publicUatRepository.findByCodSiruta("1017").orElse(null);
                    if (pUat != null) {
                        bucuresti = new com.multitenant.model.core.Uat();
                        bucuresti.setCodSiruta(pUat.getCodSiruta());
                        bucuresti.setDenumire(pUat.getDenumire());
                        bucuresti.setJudet(pUat.getJudet());
                        bucuresti.setTipUat(pUat.getTipUat());
                        bucuresti.setIsActive(true);
                        bucuresti = uatRepository.save(bucuresti);
                        
                        pUat.setTenantId("bucuresti");
                        publicUatRepository.save(pUat);
                    }
                } else {
                    bucuresti = uatRepository.findByCodSiruta("1017").orElse(null);
                }

                if (gospodarieRepository.count() == 0 && bucuresti != null) {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

                    for (int i = 1; i <= 10; i++) {
                        Gospodarie g = new Gospodarie();
                        g.setCodGospodarie("BUC-GOSP-" + String.format("%03d", i));
                        Adresa a = new Adresa();
                        a.setStreet("Calea Victoriei");
                        a.setStreetNumber(String.valueOf(i * 10));
                        a.setCounty("București");
                        a.setLocalitate("București Sectorul 1");
                        a.setBuilding("B" + i);
                        a.setStaircase("1");
                        a.setFloor(i % 10);
                        a.setApartmentNumber(i * 5);
                        g.setAdresa(a);
                        g.setTipGospodarie(i % 2 == 0 ? TipGospodarie.COLECTIVA : TipGospodarie.INDIVIDUALA);
                        g.setActiva(true);
                        g.setUat(bucuresti);
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
