package com.multitenant.service;

import com.multitenant.dto.ContractUtilizareDTO;
import com.multitenant.model.registru.ContractUtilizare;
import com.multitenant.model.registru.StatusContractUtilizare;
import com.multitenant.model.persoana.Persoana;
import com.multitenant.repository.ContractUtilizareRepository;
import com.multitenant.repository.ParcelaRepository;
import com.multitenant.repository.PersoanaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.multitenant.model.persoana.PersoanaFizica;

import java.util.List;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class ContractUtilizareService {

    private final ContractUtilizareRepository contractUtilizareRepository;
    private final ParcelaRepository parcelaRepository;
    private final PersoanaRepository persoanaRepository;
    private final ContractKafkaProducer contractKafkaProducer;

    public ContractUtilizareService(ContractUtilizareRepository contractUtilizareRepository,
                                    ParcelaRepository parcelaRepository,
                                    PersoanaRepository persoanaRepository,
                                    ContractKafkaProducer contractKafkaProducer) {
        this.contractUtilizareRepository = contractUtilizareRepository;
        this.parcelaRepository = parcelaRepository;
        this.persoanaRepository = persoanaRepository;
        this.contractKafkaProducer = contractKafkaProducer;
    }

    public Page<ContractUtilizare> getAllContracts(String uatCode, Pageable pageable) {
        if (uatCode != null && !uatCode.isBlank()) {
            return contractUtilizareRepository.findByUatCode(uatCode, pageable);
        }
        return contractUtilizareRepository.findAll(pageable);
    }

    public ContractUtilizare getContractById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        return contractUtilizareRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contractul de utilizare nu a fost găsit"));
    }

    @Transactional
    public ContractUtilizare createContract(ContractUtilizareDTO dto, Long currentUserId) {
        if (dto == null) {
            throw new IllegalArgumentException("Contract cannot be null");
        }
        ContractUtilizare contract = mapFromDto(dto, null);
        contract.setUtilizatorOperareId(currentUserId);
        return contractUtilizareRepository.save(contract);
    }

    @Transactional
    public ContractUtilizare updateContract(Long id, ContractUtilizareDTO dto, Long currentUserId) {
        if (dto == null) {
            throw new IllegalArgumentException("Contract cannot be null");
        }
        ContractUtilizare existing = getContractById(id);
        ContractUtilizare mapped = mapFromDto(dto, existing);
        existing.setParcela(mapped.getParcela());
        existing.setLocatorProprietar(mapped.getLocatorProprietar());
        existing.setLocatorUtilizator(mapped.getLocatorUtilizator());
        existing.setUtilizatorOperareId(currentUserId);
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

        if (dto.getParcelaId() == null) {
            throw new IllegalArgumentException("Parcela asociată este obligatorie");
        }
        contract.setParcela(parcelaRepository.findById(dto.getParcelaId())
                .orElseThrow(() -> new RuntimeException("Parcela asociată nu a fost găsită")));

        contract.setLocatorProprietar(resolvePersoanaId(dto.getLocatorProprietarId(), "Proprietarul locator nu a fost găsit"));
        contract.setLocatorUtilizator(resolvePersoanaId(dto.getLocatorUtilizatorId(), "Utilizatorul locator nu a fost găsit"));

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

    @Transactional
    public void notificaContracteAproapeExpirate(LocalDate dataLimita) {
        if (dataLimita == null) {
            throw new IllegalArgumentException("dataLimita cannot be null");
        }
        List<ContractUtilizare> contracte = contractUtilizareRepository.findByStatusContractAndDataSfarsitLessThan(
                StatusContractUtilizare.ACTIV, dataLimita);
        for (ContractUtilizare contract : contracte) {
            Persoana locatorUtilizator = contract.getLocatorUtilizator();
            if (locatorUtilizator != null) {
                locatorUtilizator = (Persoana) org.hibernate.Hibernate.unproxy(locatorUtilizator);
            }
            String cnp = null;
            if (locatorUtilizator instanceof PersoanaFizica) {
                cnp = ((PersoanaFizica) locatorUtilizator).getCnp();
            } else {
                Persoana locatorProprietar = contract.getLocatorProprietar();
                if (locatorProprietar != null) {
                    locatorProprietar = (Persoana) org.hibernate.Hibernate.unproxy(locatorProprietar);
                    if (locatorProprietar instanceof PersoanaFizica) {
                        cnp = ((PersoanaFizica) locatorProprietar).getCnp();
                    }
                }
            }

            if (cnp != null) {
                String mesajText = "Contractul cu numarul " + contract.getNumarContract() + " expira la data de " + contract.getDataSfarsit();
                contractKafkaProducer.trimiteAlertaExpirare(cnp, mesajText);
            }
        }
    }
}
