package com.example.erp.service;

import com.example.erp.dto.CreateDeliveryRequest;
import com.example.erp.dto.DeliveryFilterRequest;
import com.example.erp.dto.DeliveryResponse;
import com.example.erp.dto.PageResponse;

public interface DeliveryService {

    PageResponse<DeliveryResponse> listDeliveries(DeliveryFilterRequest filter);

    DeliveryResponse getDelivery(Long id);

    DeliveryResponse createDelivery(CreateDeliveryRequest request, String actingUsername);

    DeliveryResponse pickDelivery(Long id, String actingUsername);

    DeliveryResponse packDelivery(Long id, String actingUsername);

    DeliveryResponse shipDelivery(Long id, String actingUsername);

    DeliveryResponse completeDelivery(Long id, String actingUsername);

    DeliveryResponse cancelDelivery(Long id);
}
