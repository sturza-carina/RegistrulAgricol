package com.multitenant.service;

import com.multitenant.dto.ContractUtilizareDTO;
import com.multitenant.model.registru.ContractUtilizare;
import com.multitenant.model.registru.StatusContractUtilizare;
import com.multitenant.model.persoana.Persoana;
import com.multitenant.repository.ContractUtilizareRepository;
import com.multitenant.repository.ParcelaRepository;
import com.multitenant.repository.TerenRepository;
import com.multitenant.repository.PersoanaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDate;

@Service
public class ContractUtilizareService {

    private final ContractUtilizareRepository contractUtilizareRepository;
    private final ParcelaRepository parcelaRepository;
    private final TerenRepository terenRepository;
    private final PersoanaRepository persoanaRepository;

    public ContractUtilizareService(ContractUtilizareRepository contractUtilizareRepository,
                                    ParcelaRepository parcelaRepository,
                                    TerenRepository terenRepository,
                                    PersoanaRepository persoanaRepository) {
        this.contractUtilizareRepository = contractUtilizareRepository;
        this.parcelaRepository = parcelaRepository;
        this.terenRepository = terenRepository;
        this.persoanaRepository = persoanaRepository;
    }

    public List<ContractUtilizare> getAllContracts() {
        return contractUtilizareRepository.findAll();
    }

    public ContractUtilizare getContractById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return contractUtilizareRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contractul de utilizare nu a fost găsit"));
    }

    @Transactional
    public ContractUtilizare createContract(ContractUtilizareDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Contract cannot be null");
        }
        ContractUtilizare contract = mapFromDto(dto, null);
        checkNoOverlap(dto.getParcelaId(), null, dto.getDataInceput(), dto.getDataSfarsit());
        return contractUtilizareRepository.save(contract);
    }

    @Transactional
    public ContractUtilizare updateContract(Long id, ContractUtilizareDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Contract cannot be null");
        }
        ContractUtilizare existing = getContractById(id);
        checkNoOverlap(dto.getParcelaId(), id, dto.getDataInceput(), dto.getDataSfarsit());
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
        existing.setParcela(mapped.getParcela());

        return contractUtilizareRepository.save(existing);
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

    private void checkNoOverlap(Long parcelaId, Long excludeContractId, LocalDate dataInceput, LocalDate dataSfarsit) {
        if (parcelaId == null || dataInceput == null || dataSfarsit == null) {
            return;
        }
        Long exclude = excludeContractId != null ? excludeContractId : -1L;
        if (contractUtilizareRepository.existsOverlappingContract(parcelaId, exclude, dataInceput, dataSfarsit)) {
            throw new IllegalStateException(
                "Există deja un contract activ pentru această parcelă în intervalul de date specificat.");
        }
    }

    private ContractUtilizare mapFromDto(ContractUtilizareDTO dto, ContractUtilizare target) {
        ContractUtilizare contract = target != null ? target : new ContractUtilizare();

        if (dto.getTerenId() == null) {
            throw new IllegalArgumentException("Terenul asociat este obligatoriu");
        }
        contract.setTeren(terenRepository.findById(dto.getTerenId())
                .orElseThrow(() -> new RuntimeException("Terenul asociat nu a fost găsit")));

        if (dto.getParcelaId() != null) {
            contract.setParcela(parcelaRepository.findById(dto.getParcelaId())
                    .orElseThrow(() -> new RuntimeException("Parcela asociată nu a fost găsită")));
        } else {
            contract.setParcela(null);
        }

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
}
