package com.example.erp.service;

import com.example.erp.dto.AttachmentResponse;
import com.example.erp.dto.CreateAttachmentRequest;

import java.util.List;

public interface AttachmentService {

    List<AttachmentResponse> list(String ownerType, Long ownerId);

    AttachmentResponse create(CreateAttachmentRequest request, String actingUsername);

    void delete(Long id);
}
