package com.multitenant.config;

import com.multitenant.config.tenant.TenantContext;
import com.multitenant.model.registru.Gospodarie;
import com.multitenant.model.registru.CarteFunciara;
import com.multitenant.model.registru.TipZona;
import com.multitenant.model.common.Adresa;
import com.multitenant.model.registru.TipGospodarie;
import com.multitenant.model.core.User;
import com.multitenant.model.core.Cetatean;
import com.multitenant.repository.GospodarieRepository;
import com.multitenant.repository.CarteFunciaraRepository;
import com.multitenant.repository.UserRepository;
import com.multitenant.repository.core.CetateanRepository;
import com.multitenant.service.TenantService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.multitenant.model.registru.CatalogPpp;
import com.multitenant.model.registru.CatalogIngrasaminte;
import com.multitenant.model.registru.TratamentFitosanitar;
import com.multitenant.model.registru.Fertilizare;
import com.multitenant.repository.CatalogPppRepository;
import com.multitenant.repository.CatalogIngrasaminteRepository;
import com.multitenant.repository.TratamentFitosanitarRepository;
import com.multitenant.repository.FertilizareRepository;

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
    private final CarteFunciaraRepository carteFunciaraRepository;
    private final CetateanRepository cetateanRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.multitenant.repository.CerereRepository cerereRepository;
    private final com.multitenant.repository.ContractUtilizareRepository contractUtilizareRepository;
    private final com.multitenant.repository.CulturaParcelaRepository culturaParcelaRepository;
    private final com.multitenant.repository.PomRepository pomRepository;
    private final com.multitenant.repository.VitaDeVieRepository vitaDeVieRepository;
    private final com.multitenant.repository.PasuneFaneataRepository pasuneFaneataRepository;
    
    // NEW REPOSITORIES FOR FERTILIZERS AND PPP SEEDING
    private final CatalogPppRepository catalogPppRepository;
    private final CatalogIngrasaminteRepository catalogIngrasaminteRepository;
    private final TratamentFitosanitarRepository fitosanitarRepository;
    private final FertilizareRepository fertilizareRepository;
    private final com.multitenant.repository.AnimalIndividualRepository animalIndividualRepository;
    private final com.multitenant.repository.EfectivGrupRepository efectivGrupRepository;

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
                          CarteFunciaraRepository carteFunciaraRepository,
                          CetateanRepository cetateanRepository,
                          PasswordEncoder passwordEncoder,
                          com.multitenant.repository.CerereRepository cerereRepository,
                          com.multitenant.repository.ContractUtilizareRepository contractUtilizareRepository,
                          com.multitenant.repository.CulturaParcelaRepository culturaParcelaRepository,
                          com.multitenant.repository.PomRepository pomRepository,
                          com.multitenant.repository.VitaDeVieRepository vitaDeVieRepository,
                          com.multitenant.repository.PasuneFaneataRepository pasuneFaneataRepository,
                          CatalogPppRepository catalogPppRepository,
                          CatalogIngrasaminteRepository catalogIngrasaminteRepository,
                          TratamentFitosanitarRepository fitosanitarRepository,
                          FertilizareRepository fertilizareRepository,
                          com.multitenant.repository.AnimalIndividualRepository animalIndividualRepository,
                          com.multitenant.repository.EfectivGrupRepository efectivGrupRepository) {
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
        this.carteFunciaraRepository = carteFunciaraRepository;
        this.cetateanRepository = cetateanRepository;
        this.passwordEncoder = passwordEncoder;
        this.cerereRepository = cerereRepository;
        this.contractUtilizareRepository = contractUtilizareRepository;
        this.culturaParcelaRepository = culturaParcelaRepository;
        this.pomRepository = pomRepository;
        this.vitaDeVieRepository = vitaDeVieRepository;
        this.pasuneFaneataRepository = pasuneFaneataRepository;
        this.catalogPppRepository = catalogPppRepository;
        this.catalogIngrasaminteRepository = catalogIngrasaminteRepository;
        this.fitosanitarRepository = fitosanitarRepository;
        this.fertilizareRepository = fertilizareRepository;
        this.animalIndividualRepository = animalIndividualRepository;
        this.efectivGrupRepository = efectivGrupRepository;
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

            if (cetateanRepository.findByEmail("cetatean@registru.ro").isEmpty()) {
                System.out.println("[DatabaseSeeder] Seeding dummy cetatean in public schema...");
                Cetatean cetatean = new Cetatean();
                cetatean.setNume("Popescu");
                cetatean.setPrenume("Cetatean");
                cetatean.setCnp("1800101123456");
                cetatean.setEmail("cetatean@registru.ro");
                cetatean.setParola(passwordEncoder.encode("password123"));
                cetatean.setTelefon("0712345678");
                cetatean.setJudet("Cluj");
                cetatean.setLocalitate("Cluj-Napoca");
                cetatean.setStrada("Str. Libertatii");
                cetatean.setNumar("1");
                cetateanRepository.save(cetatean);
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

                // Categorii de folosință variate, câte una pentru fiecare din cele 10 parcele CJ
                String[] categoriiFolosintaCluj = {
                        "Arabil", "Pășune", "Fânețe", "Livadă", "Vii",
                        "Pădure", "Ape", "Alte", "Arabil", "Livadă"
                };

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

                    // Seeder-ul ocoleste TerenService (nu publica eveniment Spring),
                    // deci CarteFunciara trebuie creata explicit aici.
                    // In productie, TerenService.createTerenFromDTO() o creeaza automat.
                    if (carteFunciaraRepository.findByTerenId(t.getId()).isEmpty()) {
                        CarteFunciara cf = new CarteFunciara();
                        cf.setTeren(t);
                        // Numere CF realiste pentru Cluj-Napoca (format ANCPI)
                        cf.setNumarCf(String.format("CF-%05d-CJ", 100000 + i));
                        cf.setNumarTopografic(String.format("Top/%d/%d/a", 1200 + i, i));
                        // Suprafata initiala = suprafata parcelei care va fi adaugata mai jos
                        cf.setSuprafataTotalaIntabulata(1.0 + (i * 0.1));
                        carteFunciaraRepository.save(cf);
                    }

                    com.multitenant.model.registru.Parcela p = new com.multitenant.model.registru.Parcela();
                    p.setDenumire("Parcela CJ " + i);
                    p.setSuprafata(1.0 + (i * 0.1));
                    p.setCategorieFolosinta(categoriiFolosintaCluj[i - 1]);
                    p.setTeren(t);
                    // Campuri noi Cap. II (HG 1627/2024)
                    p.setNumarCadastral(String.format("54975-%05d", i));
                    p.setTipZona(i % 3 == 0 ? TipZona.INTRAVILAN : TipZona.EXTRAVILAN);
                    p.setTitularDreptFolosinta("Ion" + i + " Popescu CJ");
                    String pJson = "{\"type\": \"Feature\", \"geometry\": {\"type\": \"Polygon\", \"coordinates\": [[[23.569717, 46.810456], [23.569705, 46.810905], [23.571013, 46.810921], [23.571025, 46.810472], [23.569717, 46.810456]]]}, \"properties\": {}}";
                    p.setPolygon(mapper.readTree(pJson));
                    p = parcelaRepository.save(p);

                    // Seed Tratamente si Fertilizari pentru parcelele Arabil (Cluj)
                    if ("Arabil".equals(p.getCategorieFolosinta())) {
                        java.util.List<CatalogPpp> ppps = catalogPppRepository.findAll();
                        java.util.List<CatalogIngrasaminte> ingrasaminte = catalogIngrasaminteRepository.findAll();

                        if (!ppps.isEmpty() && fitosanitarRepository.count() < 10) {
                            CatalogPpp ppp = ppps.get(i % ppps.size());
                            TratamentFitosanitar tf = new TratamentFitosanitar();
                            tf.setParcela(p);
                            tf.setCatalogPpp(ppp);
                            tf.setDataEfectuarii(java.time.LocalDateTime.now().minusDays(15 + i));
                            tf.setFenofaza(i % 2 == 0 ? "înfrățire" : "înspicare");
                            tf.setAgentDaunator(i % 2 == 0 ? "Fuzarioză" : "Buruieni dicotiledonate");
                            tf.setDozaUtilizata(ppp.getDozaOmologata() * 0.95);
                            tf.setSuprafataTratata(p.getSuprafata());
                            tf.setCantitateTotala(tf.getDozaUtilizata() * p.getSuprafata());
                            tf.setResponsabil("Ing. Vasile Popescu");
                            tf.setSemnaturaElectronica("Semnat Electronic");
                            fitosanitarRepository.save(tf);
                        }

                        if (!ingrasaminte.isEmpty() && fertilizareRepository.count() < 10) {
                            CatalogIngrasaminte ing = ingrasaminte.get(i % ingrasaminte.size());
                            Fertilizare fert = new Fertilizare();
                            fert.setParcela(p);
                            fert.setCatalogIngrasaminte(ing);
                            fert.setDataAplicarii(java.time.LocalDate.now().minusDays(25 + i));
                            fert.setCantitateBruta(200.0);
                            fert.setUnitateMasura("kg/ha");
                            fert.setAportAzot(ing.getProcentAzot() * 200.0 / 100.0);
                            fert.setAportFosfor(ing.getProcentFosfor() * 200.0 / 100.0);
                            fert.setAportPotasiu(ing.getProcentPotasiu() * 200.0 / 100.0);
                            fertilizareRepository.save(fert);
                        }
                    }

                    // Seed CulturaParcela pentru parcelele Arabil
                    if ("Arabil".equals(p.getCategorieFolosinta())) {
                        com.multitenant.model.registru.CulturaParcela cultura2025 = new com.multitenant.model.registru.CulturaParcela();
                        cultura2025.setAnAgricol(2025);
                        cultura2025.setSpecieCultura(i % 2 == 0 ? "Grâu" : "Porumb");
                        cultura2025.setSuprafataCultivataHa(p.getSuprafata());
                        cultura2025.setDataInsamantare(java.time.LocalDate.of(2025, 3, 15));
                        cultura2025.setDataRecoltare(java.time.LocalDate.of(2025, 8, 20));
                        cultura2025.setProductieHectarKg(4500.0 + i * 100);
                        cultura2025.setProductieTotalaTone((4500.0 + i * 100) * p.getSuprafata() / 1000);
                        cultura2025.setSistemIrigare("Aspersiune");
                        cultura2025.setTipSol("Cernoziom");
                        cultura2025.setCulturaPrecedenta("Rapiță");
                        cultura2025.setParcela(p);
                        culturaParcelaRepository.save(cultura2025);

                        com.multitenant.model.registru.CulturaParcela cultura2026 = new com.multitenant.model.registru.CulturaParcela();
                        cultura2026.setAnAgricol(2026);
                        cultura2026.setSpecieCultura(i % 2 == 0 ? "Orz" : "Floarea-soarelui");
                        cultura2026.setSuprafataCultivataHa(p.getSuprafata());
                        cultura2026.setDataInsamantare(java.time.LocalDate.of(2026, 4, 5));
                        cultura2026.setDataRecoltare(java.time.LocalDate.of(2026, 9, 10));
                        cultura2026.setProductieHectarKg(4800.0 + i * 120);
                        cultura2026.setProductieTotalaTone((4800.0 + i * 120) * p.getSuprafata() / 1000);
                        cultura2026.setSistemIrigare("Picurare");
                        cultura2026.setTipSol("Cernoziom");
                        cultura2026.setCulturaPrecedenta(i % 2 == 0 ? "Grâu" : "Porumb");
                        cultura2026.setParcela(p);
                        culturaParcelaRepository.save(cultura2026);
                    }

                    // Seed Pom pentru parcelele Livadă
                    if ("Livadă".equals(p.getCategorieFolosinta())) {
                        com.multitenant.model.registru.Pom pom = new com.multitenant.model.registru.Pom();
                        pom.setTipInregistrare(i % 2 == 0
                                ? com.multitenant.model.registru.TipInregistrarePom.IZOLAT
                                : com.multitenant.model.registru.TipInregistrarePom.PLANTATIE);
                        pom.setSpecie(i % 2 == 0 ? "Măr" : "Prun");
                        pom.setSoi(i % 2 == 0 ? "Golden Delicious" : "Stanley");
                        pom.setAnPlantare(2018 + (i % 5));
                        if (pom.getTipInregistrare() == com.multitenant.model.registru.TipInregistrarePom.IZOLAT) {
                            pom.setNumarPomi(8 + i);
                        } else {
                            pom.setSuprafataHa(p.getSuprafata());
                            pom.setDensitatePomiHa(400);
                        }
                        pom.setStarePomi("Pe rod");
                        pom.setSistemIntretinere("Ecologic");
                        pom.setSistemIrigare("Picurare");
                        pom.setProductieEstimataKg(1200.0 + i * 50);
                        pom.setObservatii("Sămânță de test generată prin seeder");
                        pom.setParcela(p);
                        pomRepository.save(pom);
                    }

                    // Seed VitaDeVie pentru parcelele Vii
                    if ("Vii".equals(p.getCategorieFolosinta())) {
                        com.multitenant.model.registru.VitaDeVie vita = new com.multitenant.model.registru.VitaDeVie();
                        vita.setTipInregistrare(com.multitenant.model.registru.TipInregistrareVita.PLANTATIE);
                        vita.setSpecie("Viță de vie");
                        vita.setSoi("Fetească Regală");
                        vita.setAnPlantare(2016);
                        vita.setSuprafataHa(p.getSuprafata());
                        vita.setDensitateViteHa(4000);
                        vita.setStareVita("Pe rod");
                        vita.setSistemIntretinere("Convențional");
                        vita.setSistemIrigare("Fără irigare");
                        vita.setProductieEstimataKg(8000.0);
                        vita.setObservatii("Sămânță de test generată prin seeder");
                        vita.setParcela(p);
                        vitaDeVieRepository.save(vita);
                    }

                    // Seed PasuneFaneata pentru parcelele Pasune/Fanete
                    if ("Pășune".equals(p.getCategorieFolosinta()) || "Fânețe".equals(p.getCategorieFolosinta())) {
                        com.multitenant.model.registru.PasuneFaneata pf = new com.multitenant.model.registru.PasuneFaneata();
                        boolean pasunat = "Pășune".equals(p.getCategorieFolosinta());
                        pf.setTipFolosinta(pasunat
                                ? com.multitenant.model.registru.TipFolosintaPasune.PASUNAT
                                : com.multitenant.model.registru.TipFolosintaPasune.COSIT);
                        pf.setSuprafataHa(p.getSuprafata());
                        pf.setSpeciiDominante(pasunat ? "Păiuș, Trifoi alb" : "Timoftică, Trifoi roșu");
                        if (pasunat) {
                            pf.setNumarAnimalePasunat(12);
                        } else {
                            pf.setNumarCosiriAnuale(2);
                        }
                        pf.setProductieEstimataKgHa(pasunat ? 3500.0 : 4200.0);
                        pf.setStareVegetatie("Bună");
                        pf.setSistemIntretinere("Convențional");
                        pf.setSistemIrigare("Fără irigare");
                        pf.setObservatii("Sămânță de test generată prin seeder");
                        pf.setParcela(p);
                        pasuneFaneataRepository.save(pf);
                    }

                    com.multitenant.model.persoana.PersoanaFizica persoana = new com.multitenant.model.persoana.PersoanaFizica();
                    persoana.setFirstName("Ion" + i);
                    persoana.setLastName("Popescu CJ");
                    persoana.setCnp(String.format("190010112%04d", i));
                    persoana.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1).plusDays(i));
                    persoana.setAdresa(a);
                    persoana.getGospodarii().add(savedG);
                    persoana.setIsHeadOfHousehold(true);
                    persoana = persoanaRepository.save(persoana);

                    if (i == 1) {
                        com.multitenant.model.registru.ContractUtilizare contract = new com.multitenant.model.registru.ContractUtilizare();
                        contract.setParcela(p);
                        contract.setLocatorProprietar(persoana);
                        contract.setLocatorUtilizator(persoana);
                        contract.setTipContract(com.multitenant.model.registru.TipContractUtilizare.ARENDA);
                        contract.setNumarContract("CT-TEST-001");
                        contract.setDataSemnare(java.time.LocalDate.now());
                        contract.setDataInceput(java.time.LocalDate.now());
                        contract.setDataSfarsit(java.time.LocalDate.now().plusDays(20));
                        contract.setStatusContract(com.multitenant.model.registru.StatusContractUtilizare.ACTIV);
                        contract.setEsteActiv(true);
                        contract.setUtilizatorOperareId(1L);
                        contractUtilizareRepository.save(contract);
                        System.out.println("[DatabaseSeeder] Seeded mock expiring contract CT-TEST-001 for CNP " + persoana.getCnp());
                    }

                    com.multitenant.model.registru.Cladire cladire = new com.multitenant.model.registru.Cladire();
                    cladire.setDestinatie("Locuinta");
                    cladire.setSuprafataConstruita(100.0 + i * 5);
                    cladire.setAnTerminare(2000 + i);
                    cladire.setMateriale("Caramida");
                    cladire.setGospodarie(savedG);
                    cladireRepository.save(cladire);

                    com.multitenant.model.registru.Machinery machinery = new com.multitenant.model.registru.Machinery();
                    String[] tipuriUtilaje = {"Tractor", "Plug", "Remorcă", "Semănătoare", "Combină", "Grapă"};
                    String tipUtilaj = tipuriUtilaje[i % tipuriUtilaje.length];
                    machinery.setTipUtilaj(tipUtilaj);
                    machinery.setMarca(i % 2 == 0 ? "John Deere" : "New Holland");
                    machinery.setModel("Model " + i);
                    machinery.setAnFabricatie(2010 + (i % 15));
                    machinery.setNumarInmatriculare(String.format("CJ-%02d-AGR", i));
                    machinery.setGospodarie(savedG);
                    machineryRepository.save(machinery);

                    // Seed animal individual for Cluj
                    if (i % 3 == 0) {
                        com.multitenant.model.animal.AnimalIndividual bovine = new com.multitenant.model.animal.AnimalIndividual();
                        bovine.setGospodarie(savedG);
                        bovine.setProprietar(persoana);
                        bovine.setNumarCrotal("RO-CJ-" + String.format("%05d", 1000 + i));
                        bovine.setSpecie(com.multitenant.model.animal.SpecieAnimal.BOVINE);
                        bovine.setRasa(i % 2 == 0 ? "Bălțată Românească" : "Holstein");
                        bovine.setSex(i % 2 == 0 ? com.multitenant.model.animal.SexAnimal.FEMININ : com.multitenant.model.animal.SexAnimal.MASCULIN);
                        bovine.setDataNastere(java.time.LocalDate.now().minusYears(2).minusMonths(i));
                        bovine.setGreutateKg(450.0 + i * 15);
                        bovine.setStareActiva(true);
                        bovine.setTenantId("cluj");
                        animalIndividualRepository.save(bovine);
                    } else if (i % 3 == 1) {
                        com.multitenant.model.animal.AnimalIndividual ovine = new com.multitenant.model.animal.AnimalIndividual();
                        ovine.setGospodarie(savedG);
                        ovine.setProprietar(persoana);
                        ovine.setNumarCrotal("RO-CJ-" + String.format("%05d", 2000 + i));
                        ovine.setSpecie(com.multitenant.model.animal.SpecieAnimal.OVINE);
                        ovine.setRasa("Țurcană");
                        ovine.setSex(com.multitenant.model.animal.SexAnimal.FEMININ);
                        ovine.setDataNastere(java.time.LocalDate.now().minusYears(1).minusMonths(i));
                        ovine.setGreutateKg(60.0 + i);
                        ovine.setStareActiva(true);
                        ovine.setTenantId("cluj");
                        animalIndividualRepository.save(ovine);
                    } else {
                        com.multitenant.model.animal.AnimalIndividual caprine = new com.multitenant.model.animal.AnimalIndividual();
                        caprine.setGospodarie(savedG);
                        caprine.setProprietar(persoana);
                        caprine.setNumarCrotal("RO-CJ-" + String.format("%05d", 3000 + i));
                        caprine.setSpecie(com.multitenant.model.animal.SpecieAnimal.CAPRINE);
                        caprine.setRasa("Carpatină");
                        caprine.setSex(com.multitenant.model.animal.SexAnimal.FEMININ);
                        caprine.setDataNastere(java.time.LocalDate.now().minusYears(1).minusMonths(i));
                        caprine.setGreutateKg(45.0 + i);
                        caprine.setStareActiva(true);
                        caprine.setTenantId("cluj");
                        animalIndividualRepository.save(caprine);
                    }

                    // Seed animal grup for Cluj
                    if (i % 2 == 0) {
                        com.multitenant.model.animal.EfectivGrup pasari = new com.multitenant.model.animal.EfectivGrup();
                        pasari.setGospodarie(savedG);
                        pasari.setProprietar(persoana);
                        pasari.setSpecie(com.multitenant.model.animal.SpecieAnimal.PASARI);
                        pasari.setNumarCapeteFamilii(50 + i * 5);
                        pasari.setDataInregistrare(java.time.LocalDate.now());
                        pasari.setDetalii("Găini, Rațe, Gâște");
                        pasari.setTenantId("cluj");
                        efectivGrupRepository.save(pasari);
                    } else {
                        com.multitenant.model.animal.EfectivGrup apicole = new com.multitenant.model.animal.EfectivGrup();
                        apicole.setGospodarie(savedG);
                        apicole.setProprietar(persoana);
                        apicole.setSpecie(com.multitenant.model.animal.SpecieAnimal.APICOLE);
                        apicole.setNumarCapeteFamilii(10 + i);
                        apicole.setDataInregistrare(java.time.LocalDate.now());
                        apicole.setDetalii("Familii de albine productive");
                        apicole.setTenantId("cluj");
                        efectivGrupRepository.save(apicole);
                    }
                }

                // Adaugam o cerere dummy pentru cetatean
                com.multitenant.model.core.Cetatean c = cetateanRepository.findByEmail("cetatean@registru.ro").orElse(null);
                if (c != null && cerereRepository.count() == 0) {
                    com.multitenant.model.registru.Cerere cerere = new com.multitenant.model.registru.Cerere();
                    cerere.setNume(c.getNume() + " " + c.getPrenume());
                    cerere.setDomiciliu(c.getLocalitate() + ", " + c.getStrada() + " " + c.getNumar());
                    cerere.setTelefon(c.getTelefon());
                    cerere.setEmail(c.getEmail());
                    cerere.setCnpCui(c.getCnp());
                    cerere.setCodCerere("REQ-" + System.currentTimeMillis());
                    cerere.setUatId(clujNapoca.getId());
                    cerere.setUserId(c.getId());
                    cerere.setStatus(com.multitenant.model.registru.StatusCerere.PENDING);
                    cerere.setTipCerere(com.multitenant.model.registru.TipCerere.ADEVERINTA_ROL);
                    cerereRepository.save(cerere);
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

                // Categorii de folosință variate, câte una pentru fiecare din cele 10 parcele BUC
                // (ordine diferită față de Cluj, ca seturile de test să nu fie identice)
                String[] categoriiFolosintaBuc = {
                        "Pășune", "Arabil", "Fânețe", "Vii", "Livadă",
                        "Ape", "Pădure", "Alte", "Livadă", "Arabil"
                };

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

                    // Seeder-ul ocoleste TerenService (nu publica eveniment Spring),
                    // deci CarteFunciara trebuie creata explicit aici.
                    if (carteFunciaraRepository.findByTerenId(t.getId()).isEmpty()) {
                        CarteFunciara cf = new CarteFunciara();
                        cf.setTeren(t);
                        // Numere CF realiste pentru Bucuresti (format ANCPI)
                        cf.setNumarCf(String.format("CF-%05d-B", 200000 + i));
                        cf.setNumarTopografic(String.format("Top/%d/%d/b", 3400 + i, i));
                        // Suprafata initiala = suprafata parcelei care va fi adaugata mai jos
                        cf.setSuprafataTotalaIntabulata(0.5 + (i * 0.05));
                        carteFunciaraRepository.save(cf);
                    }

                    com.multitenant.model.registru.Parcela p = new com.multitenant.model.registru.Parcela();
                    p.setDenumire("Parcela BUC " + i);
                    p.setSuprafata(0.5 + (i * 0.05));
                    p.setCategorieFolosinta(categoriiFolosintaBuc[i - 1]);
                    p.setTeren(t);
                    // Campuri noi Cap. II (HG 1627/2024)
                    p.setNumarCadastral(String.format("1017-%05d", i));
                    p.setTipZona(TipZona.INTRAVILAN);
                    p.setTitularDreptFolosinta("Vasile" + i + " Ionescu BUC");
                    String pJson = "{\"type\": \"Feature\", \"geometry\": {\"type\": \"Polygon\", \"coordinates\": [[[26.1000, 44.4250], [26.1000, 44.4270], [26.1025, 44.4270], [26.1025, 44.4250], [26.1000, 44.4250]]]}, \"properties\": {}}";
                    p.setPolygon(mapper.readTree(pJson));
                    p = parcelaRepository.save(p);

                    // Seed Tratamente si Fertilizari pentru parcelele Arabil (Bucuresti)
                    if ("Arabil".equals(p.getCategorieFolosinta())) {
                        java.util.List<CatalogPpp> ppps = catalogPppRepository.findAll();
                        java.util.List<CatalogIngrasaminte> ingrasaminte = catalogIngrasaminteRepository.findAll();

                        if (!ppps.isEmpty() && fitosanitarRepository.count() < 20) {
                            CatalogPpp ppp = ppps.get(i % ppps.size());
                            TratamentFitosanitar tf = new TratamentFitosanitar();
                            tf.setParcela(p);
                            tf.setCatalogPpp(ppp);
                            tf.setDataEfectuarii(java.time.LocalDateTime.now().minusDays(15 + i));
                            tf.setFenofaza(i % 2 == 0 ? "înfrățire" : "înspicare");
                            tf.setAgentDaunator(i % 2 == 0 ? "Fuzarioză" : "Buruieni dicotiledonate");
                            tf.setDozaUtilizata(ppp.getDozaOmologata() * 0.95);
                            tf.setSuprafataTratata(p.getSuprafata());
                            tf.setCantitateTotala(tf.getDozaUtilizata() * p.getSuprafata());
                            tf.setResponsabil("Ing. Vasile Popescu");
                            tf.setSemnaturaElectronica("Semnat Electronic");
                            fitosanitarRepository.save(tf);
                        }

                        if (!ingrasaminte.isEmpty() && fertilizareRepository.count() < 20) {
                            CatalogIngrasaminte ing = ingrasaminte.get(i % ingrasaminte.size());
                            Fertilizare fert = new Fertilizare();
                            fert.setParcela(p);
                            fert.setCatalogIngrasaminte(ing);
                            fert.setDataAplicarii(java.time.LocalDate.now().minusDays(25 + i));
                            fert.setCantitateBruta(200.0);
                            fert.setUnitateMasura("kg/ha");
                            fert.setAportAzot(ing.getProcentAzot() * 200.0 / 100.0);
                            fert.setAportFosfor(ing.getProcentFosfor() * 200.0 / 100.0);
                            fert.setAportPotasiu(ing.getProcentPotasiu() * 200.0 / 100.0);
                            fertilizareRepository.save(fert);
                        }
                    }

                    // Seed CulturaParcela pentru parcelele Arabil
                    if ("Arabil".equals(p.getCategorieFolosinta())) {
                        com.multitenant.model.registru.CulturaParcela cultura2025 = new com.multitenant.model.registru.CulturaParcela();
                        cultura2025.setAnAgricol(2025);
                        cultura2025.setSpecieCultura("Floarea-soarelui");
                        cultura2025.setSuprafataCultivataHa(p.getSuprafata());
                        cultura2025.setDataInsamantare(java.time.LocalDate.of(2025, 4, 10));
                        cultura2025.setDataRecoltare(java.time.LocalDate.of(2025, 9, 5));
                        cultura2025.setProductieHectarKg(2800.0 + i * 30);
                        cultura2025.setProductieTotalaTone((2800.0 + i * 30) * p.getSuprafata() / 1000);
                        cultura2025.setSistemIrigare("Fără irigare");
                        cultura2025.setTipSol("Brun-roșcat");
                        cultura2025.setCulturaPrecedenta("Grâu");
                        cultura2025.setParcela(p);
                        culturaParcelaRepository.save(cultura2025);

                        com.multitenant.model.registru.CulturaParcela cultura2026 = new com.multitenant.model.registru.CulturaParcela();
                        cultura2026.setAnAgricol(2026);
                        cultura2026.setSpecieCultura("Sfeclă de zahăr");
                        cultura2026.setSuprafataCultivataHa(p.getSuprafata());
                        cultura2026.setDataInsamantare(java.time.LocalDate.of(2026, 3, 20));
                        cultura2026.setDataRecoltare(java.time.LocalDate.of(2026, 10, 5));
                        cultura2026.setProductieHectarKg(55000.0 + i * 500);
                        cultura2026.setProductieTotalaTone((55000.0 + i * 500) * p.getSuprafata() / 1000);
                        cultura2026.setSistemIrigare("Aspersiune");
                        cultura2026.setTipSol("Brun-roșcat");
                        cultura2026.setCulturaPrecedenta("Floarea-soarelui");
                        cultura2026.setParcela(p);
                        culturaParcelaRepository.save(cultura2026);
                    }

                    // Seed Pom pentru parcelele Livadă
                    if ("Livadă".equals(p.getCategorieFolosinta())) {
                        com.multitenant.model.registru.Pom pom = new com.multitenant.model.registru.Pom();
                        pom.setTipInregistrare(com.multitenant.model.registru.TipInregistrarePom.PLANTATIE);
                        pom.setSpecie("Cireș");
                        pom.setSoi("Van");
                        pom.setAnPlantare(2020);
                        pom.setSuprafataHa(p.getSuprafata());
                        pom.setDensitatePomiHa(350);
                        pom.setStarePomi("Tânăr");
                        pom.setSistemIntretinere("Convențional");
                        pom.setSistemIrigare("Aspersiune");
                        pom.setProductieEstimataKg(600.0);
                        pom.setObservatii("Sămânță de test generată prin seeder");
                        pom.setParcela(p);
                        pomRepository.save(pom);
                    }

                    // Seed VitaDeVie pentru parcelele Vii
                    if ("Vii".equals(p.getCategorieFolosinta())) {
                        com.multitenant.model.registru.VitaDeVie vita = new com.multitenant.model.registru.VitaDeVie();
                        vita.setTipInregistrare(com.multitenant.model.registru.TipInregistrareVita.IZOLAT);
                        vita.setSpecie("Viță de vie");
                        vita.setSoi("Merlot");
                        vita.setAnPlantare(2019);
                        vita.setNumarVite(120);
                        vita.setStareVita("Tânără");
                        vita.setSistemIntretinere("Ecologic");
                        vita.setSistemIrigare("Picurare");
                        vita.setProductieEstimataKg(350.0);
                        vita.setObservatii("Sămânță de test generată prin seeder");
                        vita.setParcela(p);
                        vitaDeVieRepository.save(vita);
                    }

                    // Seed PasuneFaneata pentru parcelele Pasune/Fanete
                    if ("Pășune".equals(p.getCategorieFolosinta()) || "Fânețe".equals(p.getCategorieFolosinta())) {
                        com.multitenant.model.registru.PasuneFaneata pf = new com.multitenant.model.registru.PasuneFaneata();
                        boolean pasunat = "Pășune".equals(p.getCategorieFolosinta());
                        pf.setTipFolosinta(pasunat
                                ? com.multitenant.model.registru.TipFolosintaPasune.PASUNAT
                                : com.multitenant.model.registru.TipFolosintaPasune.COSIT);
                        pf.setSuprafataHa(p.getSuprafata());
                        pf.setSpeciiDominante(pasunat ? "Golomăț, Trifoi alb" : "Lucernă, Timoftică");
                        if (pasunat) {
                            pf.setNumarAnimalePasunat(8);
                        } else {
                            pf.setNumarCosiriAnuale(3);
                        }
                        pf.setProductieEstimataKgHa(pasunat ? 3000.0 : 4800.0);
                        pf.setStareVegetatie("În regenerare");
                        pf.setSistemIntretinere("Ecologic");
                        pf.setSistemIrigare("Aspersiune");
                        pf.setObservatii("Sămânță de test generată prin seeder");
                        pf.setParcela(p);
                        pasuneFaneataRepository.save(pf);
                    }

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
                    String[] tipuriUtilaje = {"Tractor", "Semănătoare", "Plug", "Combină", "Presă de balotat"};
                    String tipUtilaj = tipuriUtilaje[i % tipuriUtilaje.length];
                    machinery.setTipUtilaj(tipUtilaj);
                    machinery.setMarca(i % 2 == 0 ? "Claas" : "Fendt");
                    machinery.setModel("Lexion " + i);
                    machinery.setAnFabricatie(2015 + (i % 10));
                    machinery.setNumarInmatriculare(String.format("B-%02d-CMB", i));
                    machinery.setGospodarie(savedG);
                    machineryRepository.save(machinery);

                    // Seed animal individual for Bucuresti
                    if (i % 3 == 0) {
                        com.multitenant.model.animal.AnimalIndividual porcine = new com.multitenant.model.animal.AnimalIndividual();
                        porcine.setGospodarie(savedG);
                        porcine.setProprietar(persoana);
                        porcine.setNumarCrotal("RO-B-" + String.format("%05d", 4000 + i));
                        porcine.setSpecie(com.multitenant.model.animal.SpecieAnimal.PORCINE);
                        porcine.setRasa("Marele Alb");
                        porcine.setSex(com.multitenant.model.animal.SexAnimal.FEMININ);
                        porcine.setDataNastere(java.time.LocalDate.now().minusMonths(6).minusDays(i));
                        porcine.setGreutateKg(110.0 + i * 5);
                        porcine.setStareActiva(true);
                        porcine.setTenantId("bucuresti");
                        animalIndividualRepository.save(porcine);
                    } else if (i % 3 == 1) {
                        com.multitenant.model.animal.AnimalIndividual bovine = new com.multitenant.model.animal.AnimalIndividual();
                        bovine.setGospodarie(savedG);
                        bovine.setProprietar(persoana);
                        bovine.setNumarCrotal("RO-B-" + String.format("%05d", 5000 + i));
                        bovine.setSpecie(com.multitenant.model.animal.SpecieAnimal.BOVINE);
                        bovine.setRasa("Brună de Maramureș");
                        bovine.setSex(com.multitenant.model.animal.SexAnimal.FEMININ);
                        bovine.setDataNastere(java.time.LocalDate.now().minusYears(3).minusMonths(i));
                        bovine.setGreutateKg(500.0 + i * 10);
                        bovine.setStareActiva(true);
                        bovine.setTenantId("bucuresti");
                        animalIndividualRepository.save(bovine);
                    } else {
                        com.multitenant.model.animal.AnimalIndividual ecvine = new com.multitenant.model.animal.AnimalIndividual();
                        ecvine.setGospodarie(savedG);
                        ecvine.setProprietar(persoana);
                        ecvine.setNumarCrotal("RO-B-" + String.format("%05d", 6000 + i));
                        ecvine.setSpecie(com.multitenant.model.animal.SpecieAnimal.ECVINE);
                        ecvine.setRasa("Semigreu Românesc");
                        ecvine.setSex(com.multitenant.model.animal.SexAnimal.MASCULIN);
                        ecvine.setDataNastere(java.time.LocalDate.now().minusYears(4).minusMonths(i));
                        ecvine.setGreutateKg(600.0 + i * 8);
                        ecvine.setStareActiva(true);
                        ecvine.setTenantId("bucuresti");
                        animalIndividualRepository.save(ecvine);
                    }

                    // Seed animal grup for Bucuresti
                    if (i % 2 == 0) {
                        com.multitenant.model.animal.EfectivGrup pasari = new com.multitenant.model.animal.EfectivGrup();
                        pasari.setGospodarie(savedG);
                        pasari.setProprietar(persoana);
                        pasari.setSpecie(com.multitenant.model.animal.SpecieAnimal.PASARI);
                        pasari.setNumarCapeteFamilii(80 + i * 10);
                        pasari.setDataInregistrare(java.time.LocalDate.now());
                        pasari.setDetalii("Păsări curte asortate");
                        pasari.setTenantId("bucuresti");
                        efectivGrupRepository.save(pasari);
                    } else {
                        com.multitenant.model.animal.EfectivGrup altele = new com.multitenant.model.animal.EfectivGrup();
                        altele.setGospodarie(savedG);
                        altele.setProprietar(persoana);
                        altele.setSpecie(com.multitenant.model.animal.SpecieAnimal.ALTELE);
                        altele.setNumarCapeteFamilii(100 + i * 20);
                        altele.setDataInregistrare(java.time.LocalDate.now());
                        altele.setDetalii("Iepuri de casă");
                        altele.setTenantId("bucuresti");
                        efectivGrupRepository.save(altele);
                    }
                }
            }


            // Force seed treatments and fertilizations if tables are empty (independent of household count)
            TenantContext.setCurrentTenant("cluj");
            seedFitosanitarAndFertilizareIfEmpty();

            TenantContext.setCurrentTenant("bucuresti");
            seedFitosanitarAndFertilizareIfEmpty();

            System.out.println("[DatabaseSeeder] Development data successfully seeded.");
        } catch (Exception e) {
            System.err.println("[DatabaseSeeder] Error seeding development data: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up context to avoid side effects
            TenantContext.clear();
        }
    }

    private void seedFitosanitarAndFertilizareIfEmpty() {
        if (fitosanitarRepository.count() == 0 && fertilizareRepository.count() == 0) {
            System.out.println("[DatabaseSeeder] Independent seeding of fitosanitar and fertilizare for: " + TenantContext.getCurrentTenant());
            java.util.List<com.multitenant.model.registru.Parcela> parceleList = parcelaRepository.findAll();
            java.util.List<CatalogPpp> ppps = catalogPppRepository.findAll();
            java.util.List<CatalogIngrasaminte> ingrasaminte = catalogIngrasaminteRepository.findAll();

            int count = 0;
            for (com.multitenant.model.registru.Parcela p : parceleList) {
                if ("Arabil".equals(p.getCategorieFolosinta())) {
                    if (!ppps.isEmpty()) {
                        CatalogPpp ppp = ppps.get(count % ppps.size());
                        TratamentFitosanitar tf = new TratamentFitosanitar();
                        tf.setParcela(p);
                        tf.setCatalogPpp(ppp);
                        tf.setDataEfectuarii(java.time.LocalDateTime.now().minusDays(10 + count));
                        tf.setFenofaza(count % 2 == 0 ? "înfrățire" : "înspicare");
                        tf.setAgentDaunator(count % 2 == 0 ? "Fuzarioză" : "Buruieni dicotiledonate");
                        tf.setDozaUtilizata(ppp.getDozaOmologata() * 0.9);
                        tf.setSuprafataTratata(p.getSuprafata());
                        tf.setCantitateTotala(tf.getDozaUtilizata() * p.getSuprafata());
                        tf.setResponsabil("Ing. Vasile Popescu");
                        tf.setSemnaturaElectronica("Semnat Electronic");
                        fitosanitarRepository.save(tf);
                    }

                    if (!ingrasaminte.isEmpty()) {
                        CatalogIngrasaminte ing = ingrasaminte.get(count % ingrasaminte.size());
                        Fertilizare fert = new Fertilizare();
                        fert.setParcela(p);
                        fert.setCatalogIngrasaminte(ing);
                        fert.setDataAplicarii(java.time.LocalDate.now().minusDays(20 + count));
                        fert.setCantitateBruta(200.0);
                        fert.setUnitateMasura("kg/ha");
                        fert.setAportAzot(ing.getProcentAzot() * 200.0 / 100.0);
                        fert.setAportFosfor(ing.getProcentFosfor() * 200.0 / 100.0);
                        fert.setAportPotasiu(ing.getProcentPotasiu() * 200.0 / 100.0);
                        fertilizareRepository.save(fert);
                    }
                    count++;
                }
            }
        }
    }
}