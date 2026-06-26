package com.multitenant.service;

import com.multitenant.dto.ContractUtilizareDTO;
import com.multitenant.dto.ContractUtilizareResponseDTO;
import com.multitenant.model.registru.ContractUtilizare;
import com.multitenant.model.registru.StatusContractUtilizare;
import com.multitenant.model.persoana.Persoana;
import com.multitenant.repository.ContractUtilizareRepository;
import com.multitenant.repository.TerenRepository;
import com.multitenant.repository.PersoanaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Service
public class ContractUtilizareService {

    private final ContractUtilizareRepository contractUtilizareRepository;
    private final TerenRepository terenRepository;
    private final PersoanaRepository persoanaRepository;

    public ContractUtilizareService(ContractUtilizareRepository contractUtilizareRepository,
                                    TerenRepository terenRepository,
                                    PersoanaRepository persoanaRepository) {
        this.contractUtilizareRepository = contractUtilizareRepository;
        this.terenRepository = terenRepository;
        this.persoanaRepository = persoanaRepository;
    }

    @Transactional(readOnly = true)
    public List<ContractUtilizareResponseDTO> getAllContracts() {
        return contractUtilizareRepository.findAll().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContractUtilizareResponseDTO getContractById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        ContractUtilizare entity = contractUtilizareRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contractul de utilizare nu a fost găsit"));
        return toResponseDto(entity);
    }

    @Transactional
    public ContractUtilizareResponseDTO createContract(ContractUtilizareDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Contract cannot be null");
        }
        if (dto.getStatusContract() == StatusContractUtilizare.ACTIV) {
            if (contractUtilizareRepository.existsActiveOverlap(dto.getTerenId(), StatusContractUtilizare.ACTIV, dto.getDataInceput(), dto.getDataSfarsit(), null)) {
                throw new RuntimeException("Terenul are deja un contract activ în această perioadă");
            }
        }
        ContractUtilizare contract = mapFromDto(dto, null);
        contract = contractUtilizareRepository.save(contract);
        return toResponseDto(contract);
    }

    @Transactional
    public ContractUtilizareResponseDTO updateContract(Long id, ContractUtilizareDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Contract cannot be null");
        }
        if (dto.getStatusContract() == StatusContractUtilizare.ACTIV) {
            if (contractUtilizareRepository.existsActiveOverlap(dto.getTerenId(), StatusContractUtilizare.ACTIV, dto.getDataInceput(), dto.getDataSfarsit(), id)) {
                throw new RuntimeException("Terenul are deja un contract activ în această perioadă");
            }
        }
        ContractUtilizare existing = contractUtilizareRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contractul de utilizare nu a fost găsit"));
        ContractUtilizare mapped = mapFromDto(dto, existing);
        existing.setTeren(mapped.getTeren());
        existing.setLocatorProprietar(mapped.getLocatorProprietar());
        existing.setLocatorUtilizator(mapped.getLocatorUtilizator());
        existing.setUtilizatorOperare(mapped.getUtilizatorOperare());
        existing.setTipContract(mapped.getTipContract());
        existing.setNumarContract(mapped.getNumarContract());
        existing.setDataSemnare(mapped.getDataSemnare());
        existing.setDataInceput(mapped.getDataInceput());
        existing.setDataSfarsit(mapped.getDataSfarsit());
        existing.setPretArendaRonAn(mapped.getPretArendaRonAn());
        existing.setPretArendaGrauKgHa(mapped.getPretArendaGrauKgHa());
        existing.setIndexarePret(mapped.isIndexarePret());
        existing.setStatusContract(mapped.getStatusContract());
        existing.setMotivIncetare(mapped.getMotivIncetare());
        if (dto.getDataOperare() != null) {
            existing.setDataOperare(dto.getDataOperare());
        }
        existing.setEsteActiv(dto.isEsteActiv());

        existing = contractUtilizareRepository.save(existing);
        return toResponseDto(existing);
    }

    public void deleteContract(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        contractUtilizareRepository.deleteById(id);
    }

    @Transactional
    public int expireActiveContracts(LocalDate currentDate) {
        if (currentDate == null) {
            throw new IllegalArgumentException("currentDate cannot be null");
        }
        return contractUtilizareRepository.markExpiredContracts(
                StatusContractUtilizare.ACTIV,
                StatusContractUtilizare.EXPIRAT,
                currentDate);
    }

    private Persoana resolvePersoana(Persoana persoana, String errorMessage) {
        if (persoana == null) {
            return null;
        }
        if (persoana.getId() == null) {
            throw new IllegalArgumentException("Persoana referită trebuie să aibă un ID valid");
        }
        return persoanaRepository.findById(persoana.getId())
                .orElseThrow(() -> new RuntimeException(errorMessage));
    }

    private ContractUtilizare mapFromDto(ContractUtilizareDTO dto, ContractUtilizare target) {
        ContractUtilizare contract = target != null ? target : new ContractUtilizare();

        if (dto.getTerenId() == null) {
            throw new IllegalArgumentException("Terenul asociat este obligatoriu");
        }
        contract.setTeren(terenRepository.findById(dto.getTerenId())
                .orElseThrow(() -> new RuntimeException("Terenul asociat nu a fost găsit")));

        contract.setLocatorProprietar(resolvePersoanaId(dto.getLocatorProprietarId(), "Proprietarul locator nu a fost găsit"));
        contract.setLocatorUtilizator(resolvePersoanaId(dto.getLocatorUtilizatorId(), "Utilizatorul locator nu a fost găsit"));
        contract.setUtilizatorOperare(resolvePersoanaId(dto.getUtilizatorOperareId(), "Persoana de operare nu a fost găsită"));

        contract.setTipContract(dto.getTipContract());
        contract.setNumarContract(dto.getNumarContract());
        contract.setDataSemnare(dto.getDataSemnare());
        contract.setDataInceput(dto.getDataInceput());
        contract.setDataSfarsit(dto.getDataSfarsit());
        contract.setPretArendaRonAn(dto.getPretArendaRonAn());
        contract.setPretArendaGrauKgHa(dto.getPretArendaGrauKgHa());
        contract.setIndexarePret(dto.isIndexarePret());
        contract.setStatusContract(dto.getStatusContract());
        contract.setMotivIncetare(dto.getMotivIncetare());
        if (dto.getDataOperare() != null) {
            contract.setDataOperare(dto.getDataOperare());
        }
        contract.setEsteActiv(dto.isEsteActiv());

        return contract;
    }

    private Persoana resolvePersoanaId(Long id, String errorMessage) {
        if (id == null) {
            return null;
        }
        return persoanaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(errorMessage));
    }

    private ContractUtilizareResponseDTO toResponseDto(ContractUtilizare entity) {
        ContractUtilizareResponseDTO dto = new ContractUtilizareResponseDTO();
        dto.setId(entity.getId());
        if (entity.getTeren() != null) {
            dto.setTeren(new ContractUtilizareResponseDTO.TerenRef(entity.getTeren().getId(), entity.getTeren().getDenumire()));
        }
        if (entity.getLocatorProprietar() != null) {
            dto.setLocatorProprietar(new ContractUtilizareResponseDTO.PersoanaRef(entity.getLocatorProprietar().getId()));
        }
        if (entity.getLocatorUtilizator() != null) {
            dto.setLocatorUtilizator(new ContractUtilizareResponseDTO.PersoanaRef(entity.getLocatorUtilizator().getId()));
        }
        if (entity.getUtilizatorOperare() != null) {
            dto.setUtilizatorOperare(new ContractUtilizareResponseDTO.PersoanaRef(entity.getUtilizatorOperare().getId()));
        }
        dto.setTipContract(entity.getTipContract());
        dto.setNumarContract(entity.getNumarContract());
        dto.setDataSemnare(entity.getDataSemnare());
        dto.setDataInceput(entity.getDataInceput());
        dto.setDataSfarsit(entity.getDataSfarsit());
        dto.setPretArendaRonAn(entity.getPretArendaRonAn());
        dto.setPretArendaGrauKgHa(entity.getPretArendaGrauKgHa());
        dto.setIndexarePret(entity.isIndexarePret());
        dto.setStatusContract(entity.getStatusContract());
        dto.setMotivIncetare(entity.getMotivIncetare());
        dto.setDataOperare(entity.getDataOperare());
        dto.setEsteActiv(entity.isEsteActiv());
        return dto;
    }
}
