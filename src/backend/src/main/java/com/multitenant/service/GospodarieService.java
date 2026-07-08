package com.multitenant.service;

import com.multitenant.model.registru.Gospodarie;
import com.multitenant.model.registru.IstoricMembruGospodarie;
import com.multitenant.model.registru.Document;
import com.multitenant.repository.GospodarieRepository;
import com.multitenant.repository.UatRepository;
import com.multitenant.repository.PersoanaRepository;
import com.multitenant.repository.IstoricMembruRepository;
import com.multitenant.repository.DocumentRepository;
import com.multitenant.model.core.Uat;
import com.multitenant.model.persoana.Persoana;
import com.multitenant.model.persoana.PersoanaFizica;
import com.multitenant.model.persoana.PersoanaJuridica;
import org.springframework.stereotype.Service;

import java.util.List;

import com.multitenant.dto.GospodarieDTO;
import com.multitenant.dto.IstoricMembruDTO;
import com.multitenant.dto.PersoanaDTO;
import org.modelmapper.ModelMapper;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@Transactional
public class GospodarieService {

    private final GospodarieRepository gospodarieRepository;
    private final UatRepository uatRepository;
    private final PersoanaRepository persoanaRepository;
    private final IstoricMembruRepository istoricMembruRepository;
    private final DocumentRepository documentRepository;
    private final ModelMapper modelMapper;

    public GospodarieService(
            GospodarieRepository gospodarieRepository,
            UatRepository uatRepository,
            PersoanaRepository persoanaRepository,
            IstoricMembruRepository istoricMembruRepository,
            DocumentRepository documentRepository,
            ModelMapper modelMapper) {
        this.gospodarieRepository = gospodarieRepository;
        this.uatRepository = uatRepository;
        this.persoanaRepository = persoanaRepository;
        this.istoricMembruRepository = istoricMembruRepository;
        this.documentRepository = documentRepository;
        this.modelMapper = modelMapper;
    }

    private GospodarieDTO convertToDTO(Gospodarie entity) {
        if (entity == null) return null;
        GospodarieDTO dto = modelMapper.map(entity, GospodarieDTO.class);
        if (entity.getCapGospodarie() != null) {
            Persoana cap = entity.getCapGospodarie();
            PersoanaDTO capDTO = new PersoanaDTO();
            capDTO.setId(cap.getId());
            capDTO.setPersonType(cap.getPersonType());
            capDTO.setPhoneNumber(cap.getPhoneNumber());
            capDTO.setEmail(cap.getEmail());
            if (cap.getAdresa() != null) {
                capDTO.setAdresa(cap.getAdresa());
            }
            capDTO.setGospodarieIds(cap.getGospodarieIds());

            // Safely unproxy to get subclass properties
            Object unproxiedCap = org.hibernate.Hibernate.unproxy(cap);
            if (unproxiedCap instanceof PersoanaFizica pf) {
                capDTO.setFirstName(pf.getFirstName());
                capDTO.setLastName(pf.getLastName());
                capDTO.setCnp(pf.getCnp());
                capDTO.setPersonType("PHYSICAL_PERSON");
            } else if (unproxiedCap instanceof PersoanaJuridica pj) {
                capDTO.setCompanyName(pj.getCompanyName());
                capDTO.setCui(pj.getCui());
                capDTO.setPersonType("LEGAL_ENTITY");
            }
            dto.setCapGospodarie(capDTO);
        } else {
            dto.setCapGospodarie(null);
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public Page<GospodarieDTO> getAllGospodarii(Pageable pageable) {
        return gospodarieRepository.findAllByOrderByIdDesc(pageable)
                .map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public GospodarieDTO getGospodarieById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        Gospodarie entity = gospodarieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gospodarie not found"));
        return convertToDTO(entity);
    }

    @Transactional
    public GospodarieDTO createGospodarie(Gospodarie gospodarie) {
        if (gospodarie == null) {
            throw new IllegalArgumentException("Gospodarie cannot be null");
        }
        
        if (gospodarie.getUat() != null && gospodarie.getUat().getId() != null) {
            Uat managedUat = uatRepository.findById(gospodarie.getUat().getId())
                    .orElseThrow(() -> new IllegalArgumentException("UAT not found"));
            gospodarie.setUat(managedUat);
        }
        
        Gospodarie saved = gospodarieRepository.save(gospodarie);
        return convertToDTO(saved);
    }

    public GospodarieDTO updateGospodarie(Long id, Gospodarie updatedGospodarie) {
        if (updatedGospodarie == null) {
            throw new IllegalArgumentException("Gospodarie cannot be null");
        }
        Gospodarie existing = gospodarieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gospodarie not found"));
        
        if (updatedGospodarie.getCodGospodarie() != null) existing.setCodGospodarie(updatedGospodarie.getCodGospodarie());
        if (updatedGospodarie.getAdresa() != null) existing.setAdresa(updatedGospodarie.getAdresa());
        if (updatedGospodarie.getTipGospodarie() != null) existing.setTipGospodarie(updatedGospodarie.getTipGospodarie());
        existing.setActiva(updatedGospodarie.isActiva());
        if (updatedGospodarie.getUat() != null) {
            if (updatedGospodarie.getUat().getId() != null) {
                Uat managedUat = uatRepository.findById(updatedGospodarie.getUat().getId())
                        .orElseThrow(() -> new IllegalArgumentException("UAT not found"));
                existing.setUat(managedUat);
            } else {
                existing.setUat(updatedGospodarie.getUat());
            }
        }

        Gospodarie saved = gospodarieRepository.save(existing);
        return convertToDTO(saved);
    }

    public void deleteGospodarie(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        gospodarieRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<GospodarieDTO> getAllGospodarii(String uatCode, Pageable pageable) {
        Page<Gospodarie> result;
        if (uatCode != null && !uatCode.isBlank()) {
            result = gospodarieRepository.findByUat_CodSirutaOrderByIdDesc(uatCode, pageable);
        } else {
            result = gospodarieRepository.findAllByOrderByIdDesc(pageable);
        }
        return result.map(this::convertToDTO);
    }

    @Transactional
    public void seteazaCapGospodarie(Long gospodarieId, Long persoanaId) {
        Gospodarie gospodarie = gospodarieRepository.findById(gospodarieId)
                .orElseThrow(() -> new RuntimeException("Gospodarie not found"));
        
        if (persoanaId == null) {
            gospodarie.setCapGospodarie(null);
        } else {
            Persoana persoana = persoanaRepository.findById(persoanaId)
                    .orElseThrow(() -> new RuntimeException("Persoana not found"));
            gospodarie.setCapGospodarie(persoana);
        }
        gospodarieRepository.save(gospodarie);
    }

    @Transactional(readOnly = true)
    public List<IstoricMembruDTO> getIstoricMembri(Long gospodarieId) {
        List<IstoricMembruGospodarie> list = istoricMembruRepository.findByGospodarieIdOrderByDataEvenimentDescIdDesc(gospodarieId);
        return list.stream().map(entity -> {
            IstoricMembruDTO dto = new IstoricMembruDTO();
            dto.setId(entity.getId());
            dto.setGospodarieId(entity.getGospodarie().getId());
            dto.setPersoanaId(entity.getPersoana().getId());
            dto.setTipEveniment(entity.getTipEveniment());
            dto.setDataEveniment(entity.getDataEveniment());
            dto.setObservatii(entity.getObservatii());
            dto.setCreatedAt(entity.getCreatedAt());

            if (entity.getDocument() != null) {
                dto.setDocumentId(entity.getDocument().getId());
                dto.setNumeFisierDocument(entity.getDocument().getNumeFisier());
            }

            Persoana p = (Persoana) org.hibernate.Hibernate.unproxy(entity.getPersoana());
            if (p instanceof PersoanaFizica pf) {
                dto.setNumeCompletPersoana((pf.getFirstName() != null ? pf.getFirstName() : "") + " " + (pf.getLastName() != null ? pf.getLastName() : ""));
            } else if (p instanceof PersoanaJuridica pj) {
                dto.setNumeCompletPersoana(pj.getCompanyName());
            } else {
                dto.setNumeCompletPersoana("Persoană #" + p.getId());
            }
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void adaugaEvenimentIstoric(Long gospodarieId, IstoricMembruDTO dto) {
        Gospodarie gospodarie = gospodarieRepository.findById(gospodarieId)
                .orElseThrow(() -> new RuntimeException("Gospodarie not found"));
        Persoana persoana = persoanaRepository.findById(dto.getPersoanaId())
                .orElseThrow(() -> new RuntimeException("Persoana not found"));
                
        IstoricMembruGospodarie entity = new IstoricMembruGospodarie();
        entity.setGospodarie(gospodarie);
        entity.setPersoana(persoana);
        entity.setTipEveniment(dto.getTipEveniment());
        entity.setDataEveniment(dto.getDataEveniment());
        entity.setObservatii(dto.getObservatii());
        
        if (dto.getDocumentId() != null) {
            Document doc = documentRepository.findById(dto.getDocumentId())
                    .orElseThrow(() -> new RuntimeException("Document not found"));
            entity.setDocument(doc);
        }
        
        istoricMembruRepository.save(entity);
    }

    @Transactional
    public void updateEvenimentIstoric(Long gospodarieId, Long evenimentId, IstoricMembruDTO dto) {
        IstoricMembruGospodarie entity = istoricMembruRepository.findById(evenimentId)
                .orElseThrow(() -> new RuntimeException("Eveniment not found"));
        
        if (!entity.getGospodarie().getId().equals(gospodarieId)) {
            throw new IllegalArgumentException("Eveniment does not belong to this gospodarie");
        }
        
        Persoana persoana = persoanaRepository.findById(dto.getPersoanaId())
                .orElseThrow(() -> new RuntimeException("Persoana not found"));
                
        entity.setPersoana(persoana);
        entity.setTipEveniment(dto.getTipEveniment());
        entity.setDataEveniment(dto.getDataEveniment());
        entity.setObservatii(dto.getObservatii());
        
        if (dto.getDocumentId() != null) {
            Document doc = documentRepository.findById(dto.getDocumentId())
                    .orElseThrow(() -> new RuntimeException("Document not found"));
            entity.setDocument(doc);
        } else {
            entity.setDocument(null);
        }
        
        istoricMembruRepository.save(entity);
    }
}
