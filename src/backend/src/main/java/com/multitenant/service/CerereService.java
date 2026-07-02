package com.multitenant.service;

import com.multitenant.dto.CerereDTO;
import com.multitenant.model.registru.Cerere;
import com.multitenant.model.registru.StatusCerere;
import com.multitenant.repository.CerereRepository;
import com.multitenant.config.tenant.TenantContext;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CerereService {

    private final CerereRepository cerereRepository;
    private final ModelMapper modelMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public CerereService(CerereRepository cerereRepository, ModelMapper modelMapper, SimpMessagingTemplate messagingTemplate) {
        this.cerereRepository = cerereRepository;
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
}
