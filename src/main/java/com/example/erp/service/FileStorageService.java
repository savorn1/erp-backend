package com.example.erp.service;

import com.example.erp.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    FileUploadResponse upload(MultipartFile file, String folder);

    void delete(String key);
}
