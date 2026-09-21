package com.example.erp.repository;

import com.example.erp.entity.UomConversion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.Optional;

public interface UomConversionRepository extends JpaRepository<UomConversion, Long>, JpaSpecificationExecutor<UomConversion> {

    Optional<UomConversion> findByFromUnitOfMeasureIdAndToUnitOfMeasureId(Long fromUnitOfMeasureId, Long toUnitOfMeasureId);

    Optional<UomConversion> findByFromUnitOfMeasureIdAndToUnitOfMeasureIdAndActiveTrue(Long fromUnitOfMeasureId, Long toUnitOfMeasureId);

    boolean existsByFromUnitOfMeasureIdAndToUnitOfMeasureId(Long fromUnitOfMeasureId, Long toUnitOfMeasureId);

    boolean existsByFromUnitOfMeasureIdAndToUnitOfMeasureIdAndIdNot(Long fromUnitOfMeasureId, Long toUnitOfMeasureId, Long id);

    boolean existsByFromUnitOfMeasureIdOrToUnitOfMeasureId(Long fromUnitOfMeasureId, Long toUnitOfMeasureId);

    boolean existsByFromUnitOfMeasureIdInOrToUnitOfMeasureIdIn(Collection<Long> fromUnitOfMeasureIds, Collection<Long> toUnitOfMeasureIds);

    void deleteByFromUnitOfMeasureId(Long fromUnitOfMeasureId);

    void deleteByFromUnitOfMeasureIdOrToUnitOfMeasureId(Long fromUnitOfMeasureId, Long toUnitOfMeasureId);
}
