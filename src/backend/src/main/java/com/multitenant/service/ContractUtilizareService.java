package com.multitenant.service;

import com.multitenant.model.registru.ContractUtilizare;
import com.multitenant.repository.ContractUtilizareRepository;
import com.multitenant.repository.TerenRepository;
import com.multitenant.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContractUtilizareService {

    private final ContractUtilizareRepository contractUtilizareRepository;
    private final TerenRepository terenRepository;
    private final UserRepository userRepository;

    public ContractUtilizareService(ContractUtilizareRepository contractUtilizareRepository,
                                    TerenRepository terenRepository,
                                    UserRepository userRepository) {
        this.contractUtilizareRepository = contractUtilizareRepository;
        this.terenRepository = terenRepository;
        this.userRepository = userRepository;
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
    public ContractUtilizare createContract(ContractUtilizare contract) {
        if (contract == null) {
            throw new IllegalArgumentException("Contract cannot be null");
        }
        // Verify relationship targets if provided
        if (contract.getTeren() != null && contract.getTeren().getId() != null) {
            contract.setTeren(terenRepository.findById(contract.getTeren().getId())
                    .orElseThrow(() -> new RuntimeException("Terenul asociat nu a fost găsit")));
        }
        if (contract.getLocatorProprietar() != null && contract.getLocatorProprietar().getId() != null) {
            contract.setLocatorProprietar(userRepository.findById(contract.getLocatorProprietar().getId())
                    .orElseThrow(() -> new RuntimeException("Proprietarul locator nu a fost găsit")));
        }
        if (contract.getLocatorUtilizator() != null && contract.getLocatorUtilizator().getId() != null) {
            contract.setLocatorUtilizator(userRepository.findById(contract.getLocatorUtilizator().getId())
                    .orElseThrow(() -> new RuntimeException("Utilizatorul locator nu a fost găsit")));
        }
        if (contract.getUtilizatorOperare() != null && contract.getUtilizatorOperare().getId() != null) {
            contract.setUtilizatorOperare(userRepository.findById(contract.getUtilizatorOperare().getId())
                    .orElseThrow(() -> new RuntimeException("Utilizatorul de operare nu a fost găsit")));
        }
        return contractUtilizareRepository.save(contract);
    }

    @Transactional
    public ContractUtilizare updateContract(Long id, ContractUtilizare updated) {
        if (updated == null) {
            throw new IllegalArgumentException("Contract cannot be null");
        }
        ContractUtilizare existing = getContractById(id);

        if (updated.getTeren() != null && updated.getTeren().getId() != null) {
            existing.setTeren(terenRepository.findById(updated.getTeren().getId())
                    .orElseThrow(() -> new RuntimeException("Terenul asociat nu a fost găsit")));
        }
        if (updated.getLocatorProprietar() != null && updated.getLocatorProprietar().getId() != null) {
            existing.setLocatorProprietar(userRepository.findById(updated.getLocatorProprietar().getId())
                    .orElseThrow(() -> new RuntimeException("Proprietarul locator nu a fost găsit")));
        } else {
            existing.setLocatorProprietar(null);
        }
        if (updated.getLocatorUtilizator() != null && updated.getLocatorUtilizator().getId() != null) {
            existing.setLocatorUtilizator(userRepository.findById(updated.getLocatorUtilizator().getId())
                    .orElseThrow(() -> new RuntimeException("Utilizatorul locator nu a fost găsit")));
        } else {
            existing.setLocatorUtilizator(null);
        }
        if (updated.getUtilizatorOperare() != null && updated.getUtilizatorOperare().getId() != null) {
            existing.setUtilizatorOperare(userRepository.findById(updated.getUtilizatorOperare().getId())
                    .orElseThrow(() -> new RuntimeException("Utilizatorul de operare nu a fost găsit")));
        } else {
            existing.setUtilizatorOperare(null);
        }

        existing.setTipContract(updated.getTipContract());
        existing.setNumarContract(updated.getNumarContract());
        existing.setDataSemnare(updated.getDataSemnare());
        existing.setDataInceput(updated.getDataInceput());
        existing.setDataSfarsit(updated.getDataSfarsit());
        existing.setPretArendaRonAn(updated.getPretArendaRonAn());
        existing.setPretArendaGrauKgHa(updated.getPretArendaGrauKgHa());
        existing.setIndexarePret(updated.isIndexarePret());
        existing.setStatusContract(updated.getStatusContract());
        existing.setMotivIncetare(updated.getMotivIncetare());
        existing.setDataOperare(updated.getDataOperare());
        existing.setEsteActiv(updated.isEsteActiv());

        return contractUtilizareRepository.save(existing);
    }

    public void deleteContract(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        contractUtilizareRepository.deleteById(id);
    }
}
