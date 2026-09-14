package com.example.erp.service.impl;

import com.example.erp.dto.FileUploadResponse;
import com.example.erp.exception.AppException;
import com.example.erp.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {

    private static final String DEFAULT_FOLDER = "uploads";

    private final S3Client s3Client;

    @Value("${s3.bucket}")
    private String bucket;

    @Value("${s3.public-endpoint}")
    private String publicEndpoint;

    @Override
    public FileUploadResponse upload(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new AppException(HttpStatus.BAD_REQUEST, "File is required");
        }

        String originalFilename = file.getOriginalFilename();
        String key = sanitizeFolder(folder) + "/" + UUID.randomUUID() + extensionOf(originalFilename);

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .contentLength(file.getSize())
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read uploaded file");
        } catch (S3Exception e) {
            throw new AppException(HttpStatus.BAD_GATEWAY, "Failed to store file: " + e.getMessage());
        }

        return FileUploadResponse.builder()
                .key(key)
                .url(publicEndpoint + "/" + bucket + "/" + key)
                .fileName(originalFilename)
                .contentType(file.getContentType())
                .size(file.getSize())
                .build();
    }

    @Override
    public void delete(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
    }

    // Keys are namespaced by an unauthenticated, caller-supplied folder — keep
    // it to a plain path segment so it can't escape into an unrelated prefix
    // or produce a malformed key.
    private String sanitizeFolder(String folder) {
        if (folder == null || folder.isBlank()) {
            return DEFAULT_FOLDER;
        }
        String cleaned = folder.strip().replaceAll("[^a-zA-Z0-9/_-]", "");
        cleaned = cleaned.replaceAll("\\.\\.", "").replaceAll("^/+|/+$", "");
        return cleaned.isBlank() ? DEFAULT_FOLDER : cleaned;
    }

    private String extensionOf(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf('.'));
    }
}
