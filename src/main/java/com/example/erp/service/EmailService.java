package com.example.erp.service;

public interface EmailService {

    void sendWithAttachment(String to, String subject, String body,
                             byte[] attachment, String attachmentFilename, String attachmentContentType);
}
