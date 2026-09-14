package com.example.erp.service;

import com.example.erp.dto.CreateStockTransferRequest;
import com.example.erp.dto.PageResponse;
import com.example.erp.dto.ShipStockTransferRequest;
import com.example.erp.dto.StockTransferFilterRequest;
import com.example.erp.dto.StockTransferResponse;

public interface StockTransferService {

    PageResponse<StockTransferResponse> listStockTransfers(StockTransferFilterRequest filter);

    StockTransferResponse getStockTransfer(Long id);

    StockTransferResponse createStockTransfer(CreateStockTransferRequest request, String actingUsername);

    StockTransferResponse approveStockTransfer(Long id, String actingUsername);

    StockTransferResponse rejectStockTransfer(Long id);

    StockTransferResponse shipStockTransfer(Long id, ShipStockTransferRequest request, String actingUsername);

    StockTransferResponse receiveStockTransfer(Long id, String actingUsername);

    StockTransferResponse cancelStockTransfer(Long id);

    void deleteStockTransfer(Long id);
}
