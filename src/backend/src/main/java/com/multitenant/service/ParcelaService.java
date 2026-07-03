package com.multitenant.service;

import com.multitenant.dto.FieldDiff;
import com.multitenant.dto.ParcelaRevisionDto;
import com.multitenant.model.audit.CustomRevisionEntity;
import com.multitenant.event.ParcelaAdaugataEvent;
import com.multitenant.model.registru.Teren;
import com.multitenant.model.registru.Parcela;
import com.multitenant.repository.TerenRepository;
import com.multitenant.repository.ParcelaRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ParcelaService {

    private final ParcelaRepository parcelaRepository;
    private final TerenRepository terenRepository;
    private final EntityManager entityManager;
    private final ApplicationEventPublisher eventPublisher;

    public ParcelaService(ParcelaRepository parcelaRepository,
                          TerenRepository terenRepository,
                          EntityManager entityManager,
                          ApplicationEventPublisher eventPublisher) {
        this.parcelaRepository = parcelaRepository;
        this.terenRepository = terenRepository;
        this.entityManager = entityManager;
        this.eventPublisher = eventPublisher;
    }

    public Page<Parcela> getAllParceleForTenant(Pageable pageable) {
        return parcelaRepository.findAll(pageable);
    }

    public Page<Parcela> getParceleForTeren(long terenId, Pageable pageable) {
        return parcelaRepository.findByTerenId(terenId, pageable);
    }

    public Parcela saveParcela(long terenId, Parcela parcela) {
        if (parcela == null) {
            throw new IllegalArgumentException("Parcela cannot be null");
        }
        Teren teren = terenRepository.findById(terenId)
                .orElseThrow(() -> new RuntimeException("Teren not found"));
        parcela.setTeren(teren);

        if (parcela.getStereo70Coordinates() != null && !parcela.getStereo70Coordinates().trim().isEmpty()) {
            // Already set by frontend or explicitly passed
        }
        Parcela saved = parcelaRepository.save(parcela);

        // Publica evenimentul dupa salvare — CarteFunciaraEventListener va recalcula
        // suprafata_totala_intabulata din CF-ul aferent terenului, AFTER_COMMIT.
        eventPublisher.publishEvent(new ParcelaAdaugataEvent(saved.getId(), terenId));

        return saved;
    }

    public Parcela updateParcela(long id, Parcela updatedParcela) {
        if (updatedParcela == null) {
            throw new IllegalArgumentException("Parcela cannot be null");
        }
        Parcela existing = parcelaRepository.findById(id).orElse(null);
        if (existing == null) {
            throw new RuntimeException("Parcela not found");
        }

        if (updatedParcela.getDenumire() != null)
            existing.setDenumire(updatedParcela.getDenumire());
        if (updatedParcela.getSuprafata() != null)
            existing.setSuprafata(updatedParcela.getSuprafata());
        if (updatedParcela.getCategorieFolosinta() != null)
            existing.setCategorieFolosinta(updatedParcela.getCategorieFolosinta());

        // Campuri noi Cap. II (HG 1627/2024)
        if (updatedParcela.getNumarCadastral() != null)
            existing.setNumarCadastral(updatedParcela.getNumarCadastral());
        if (updatedParcela.getTipZona() != null)
            existing.setTipZona(updatedParcela.getTipZona());
        if (updatedParcela.getTitularDreptFolosinta() != null)
            existing.setTitularDreptFolosinta(updatedParcela.getTitularDreptFolosinta());

        if (updatedParcela.getStereo70Coordinates() != null && !updatedParcela.getStereo70Coordinates().trim().isEmpty()) {
            existing.setStereo70Coordinates(updatedParcela.getStereo70Coordinates());
        }
        if (updatedParcela.getPolygon() != null) {
            existing.setPolygon(updatedParcela.getPolygon());
        }

        return parcelaRepository.save(existing);
    }

    public void deleteParcela(long id) {
        parcelaRepository.deleteById(id);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<ParcelaRevisionDto> getParcelaHistory(Long id) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        // Verify entity exists in the active tenant schema
        if (!parcelaRepository.existsById(id)) {
            throw new RuntimeException("Parcela with ID " + id + " not found.");
        }

        // Query historical revisions of Parcela with revision metadata
        AuditQuery query = auditReader.createQuery()
                .forRevisionsOfEntity(Parcela.class, false, true)
                .add(AuditEntity.id().eq(id));

        List<?> results = query.getResultList();
        List<ParcelaRevisionDto> history = new ArrayList<>();

        Parcela previousParcela = null;
        for (Object result : results) {
            Object[] array = (Object[]) result;
            Parcela currentParcela = (Parcela) array[0];
            CustomRevisionEntity revEntity = (CustomRevisionEntity) array[1];
            RevisionType revType = (RevisionType) array[2];

            ParcelaRevisionDto dto = new ParcelaRevisionDto();
            dto.setRevisionId(revEntity.getRev());
            dto.setTimestamp(Instant.ofEpochMilli(revEntity.getRevtstmp()));
            dto.setAuthor(revEntity.getUsername());
            dto.setActionType(revType.name());

            // Compute property differences
            Map<String, FieldDiff> diffs = computeDiff(currentParcela, previousParcela);
            dto.setDiffs(diffs);

            history.add(dto);
            previousParcela = currentParcela;
        }

        return history;
    }

    private Map<String, FieldDiff> computeDiff(Parcela current, Parcela previous) {
        Map<String, FieldDiff> diffs = new HashMap<>();
        if (current == null) return diffs;

        compareField("denumire", current.getDenumire(), previous != null ? previous.getDenumire() : null, diffs);
        compareField("suprafata", current.getSuprafata(), previous != null ? previous.getSuprafata() : null, diffs);
        compareField("categorieFolosinta", current.getCategorieFolosinta(), previous != null ? previous.getCategorieFolosinta() : null, diffs);
        compareField("polygon", current.getPolygon(), previous != null ? previous.getPolygon() : null, diffs);

        return diffs;
    }

    private void compareField(String fieldName, Object currentValue, Object previousValue, Map<String, FieldDiff> diffs) {
        if (currentValue == null && previousValue == null) {
            return;
        }
        if (currentValue != null && currentValue.equals(previousValue)) {
            return;
        }
        diffs.put(fieldName, new FieldDiff(previousValue, currentValue));
    }
}