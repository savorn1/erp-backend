package com.example.erp.repository;

import com.example.erp.entity.GoodsReceiptLine;
import com.example.erp.entity.QualityCheckStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface GoodsReceiptLineRepository extends JpaRepository<GoodsReceiptLine, Long> {

    List<GoodsReceiptLine> findByGoodsReceiptId(Long goodsReceiptId);

    // Quantity already claimed by other receipts against this PO line that
    // haven't cleared quality check yet — subtracted from the remaining
    // receivable quantity so two in-flight receipts can't both claim the same
    // stock before either is inspected (see GoodsReceiptServiceImpl.createGoodsReceipt).
    @Query("select coalesce(sum(l.quantityReceived), 0) from GoodsReceiptLine l "
            + "where l.purchaseOrderLineId = :purchaseOrderLineId and l.qualityStatus = :status")
    BigDecimal sumQuantityByPurchaseOrderLineIdAndQualityStatus(@Param("purchaseOrderLineId") Long purchaseOrderLineId,
                                                                 @Param("status") QualityCheckStatus status);
}
