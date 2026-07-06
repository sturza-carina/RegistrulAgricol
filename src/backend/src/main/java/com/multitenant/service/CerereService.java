package com.multitenant.service;

import com.multitenant.dto.CerereDTO;
import com.multitenant.model.registru.Cerere;
import com.multitenant.model.registru.StatusCerere;
import com.multitenant.model.registru.TipCerere;
import com.multitenant.model.registru.AtestatProducator;
import com.multitenant.model.registru.CarnetComercializare;
import com.multitenant.model.persoana.PersoanaFizica;
import com.multitenant.model.core.Cetatean;
import com.multitenant.repository.CerereRepository;
import com.multitenant.repository.AtestatProducatorRepository;
import com.multitenant.repository.CarnetComercializareRepository;
import com.multitenant.repository.PersoanaRepository;
import com.multitenant.repository.core.CetateanRepository;
import com.multitenant.config.tenant.TenantContext;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CerereService {

    private final CerereRepository cerereRepository;
    private final PersoanaRepository persoanaRepository;
    private final AtestatProducatorRepository atestatProducatorRepository;
    private final CarnetComercializareRepository carnetComercializareRepository;
    private final CetateanRepository cetateanRepository;
    private final ModelMapper modelMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public CerereService(CerereRepository cerereRepository, 
                         PersoanaRepository persoanaRepository,
                         AtestatProducatorRepository atestatProducatorRepository,
                         CarnetComercializareRepository carnetComercializareRepository,
                         CetateanRepository cetateanRepository,
                         ModelMapper modelMapper, 
                         SimpMessagingTemplate messagingTemplate) {
        this.cerereRepository = cerereRepository;
        this.persoanaRepository = persoanaRepository;
        this.atestatProducatorRepository = atestatProducatorRepository;
        this.carnetComercializareRepository = carnetComercializareRepository;
        this.cetateanRepository = cetateanRepository;
        this.modelMapper = modelMapper;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public CerereDTO createCerere(CerereDTO dto) {
        Cerere cerere = modelMapper.map(dto, Cerere.class);
        cerere.setStatus(StatusCerere.PENDING);
        cerere.setCodCerere("cerere-" + UUID.randomUUID().toString().substring(0, 8)); // Example formatting
        Cerere saved = cerereRepository.save(cerere);
        CerereDTO savedDto = modelMapper.map(saved, CerereDTO.class);
        
        // Notify admins for this UAT/Tenant
        String tenantId = TenantContext.getCurrentTenant();
        messagingTemplate.convertAndSend("/topic/tenant/" + tenantId + "/cereri", savedDto);
        return savedDto;
    }

    public List<CerereDTO> getAllForTenant() {
        return cerereRepository.findAll().stream()
                .map(c -> modelMapper.map(c, CerereDTO.class))
                .collect(Collectors.toList());
    }

    public List<CerereDTO> getAllByUat(Long uatId) {
        return cerereRepository.findByUatId(uatId).stream()
                .map(c -> modelMapper.map(c, CerereDTO.class))
                .collect(Collectors.toList());
    }

    public CerereDTO getByCodCerere(String cod) {
        return cerereRepository.findByCodCerere(cod)
                .map(c -> modelMapper.map(c, CerereDTO.class))
                .orElse(null);
    }

    @Transactional
    public CerereDTO updateStatus(Long id, StatusCerere status) {
        Cerere cerere = cerereRepository.findById(id).orElseThrow(() -> new RuntimeException("Cerere not found"));
        
        if (status == StatusCerere.ACCEPTED && cerere.getStatus() != StatusCerere.ACCEPTED) {
            String cnp = cerere.getCnpCui();
            if (cnp != null && !cnp.isEmpty()) {
                PersoanaFizica persoana = (PersoanaFizica) persoanaRepository.findByCnpClar(cnp)
                        .filter(p -> p instanceof PersoanaFizica)
                        .orElse(null);

                if (persoana == null) {
                    persoana = new PersoanaFizica();
                    persoana.setFirstName(cerere.getNume() != null ? cerere.getNume().trim() : "Necunoscut");
                    persoana.setLastName("");
                    persoana.setCnp(cnp);
                    persoana.setPersonType("PHYSICAL_PERSON");
                    persoana.setEmail(cerere.getEmail());
                    persoana.setPhoneNumber(cerere.getTelefon());
                    persoana = persoanaRepository.save(persoana);
                }

                if (cerere.getTipCerere() == TipCerere.ELIBERARE_ATESTAT_PRODUCATOR) {
                    AtestatProducator atestat = new AtestatProducator();
                    atestat.setNumarAtestat("AT" + System.currentTimeMillis());
                    atestat.setSeria("CJ");
                    atestat.setPersoana(persoana);
                    atestat.setDataEliberare(LocalDate.now());
                    atestat.setValabilitateLuni(60);
                    atestatProducatorRepository.save(atestat);
                    
                    if (cerere.getUserId() != null) {
                        Cetatean cet = cetateanRepository.findById(cerere.getUserId()).orElse(null);
                        if (cet != null) {
                            cet.setAreAtestatProducator(true);
                            cetateanRepository.save(cet);
                        }
                    }
                } else if (cerere.getTipCerere() == TipCerere.ELIBERARE_CARNET_COMERCIALIZARE) {
                    List<AtestatProducator> atestate = atestatProducatorRepository.findByPersoanaId(persoana.getId());
                    if (!atestate.isEmpty()) {
                        AtestatProducator latestAtestat = atestate.get(atestate.size() - 1);
                        CarnetComercializare carnet = new CarnetComercializare();
                        carnet.setNumarCarnet("CR" + System.currentTimeMillis());
                        carnet.setSeria("CJ");
                        carnet.setPersoana(persoana);
                        carnet.setAtestat(latestAtestat);
                        carnet.setDataEliberare(LocalDate.now());
                        carnetComercializareRepository.save(carnet);
                        
                        if (cerere.getUserId() != null) {
                            Cetatean cet = cetateanRepository.findById(cerere.getUserId()).orElse(null);
                            if (cet != null) {
                                cet.setAreCarnetComercializare(true);
                                cetateanRepository.save(cet);
                            }
                        }
                    }
                }
            }
        }
        
        cerere.setStatus(status);
        Cerere saved = cerereRepository.save(cerere);
        CerereDTO savedDto = modelMapper.map(saved, CerereDTO.class);

        // Notify specific user if logged in, or just broadcast the status change
        messagingTemplate.convertAndSend("/topic/cereri/" + saved.getCodCerere(), savedDto);
        
        String tenantId = TenantContext.getCurrentTenant();
        messagingTemplate.convertAndSend("/topic/tenant/" + tenantId + "/cereri", savedDto);
        
        if (saved.getUserId() != null) {
            messagingTemplate.convertAndSend("/topic/user/" + saved.getUserId() + "/cereri", savedDto);
        }
        return savedDto;
    }

    public List<CerereDTO> getByCnp(String cnp) {
        return cerereRepository.findByCnpCui(cnp).stream()
                .map(c -> modelMapper.map(c, CerereDTO.class))
                .collect(Collectors.toList());
    }
}
