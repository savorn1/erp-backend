package com.example.erp.repository;

import com.example.erp.entity.SerialNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface SerialNumberRepository extends JpaRepository<SerialNumber, Long>, JpaSpecificationExecutor<SerialNumber> {

    boolean existsBySerialNumber(String serialNumber);

    Optional<SerialNumber> findBySerialNumber(String serialNumber);

    List<SerialNumber> findByGoodsReceiptLineId(Long goodsReceiptLineId);

    List<SerialNumber> findByGoodsReceiptLineIdIn(List<Long> goodsReceiptLineIds);

    List<SerialNumber> findByDeliveryLineIdIn(List<Long> deliveryLineIds);

    List<SerialNumber> findByTransferLineId(Long transferLineId);

    List<SerialNumber> findByAdjustmentLineId(Long adjustmentLineId);
}
