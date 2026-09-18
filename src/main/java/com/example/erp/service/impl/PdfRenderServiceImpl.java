package com.example.erp.service.impl;

import com.example.erp.exception.AppException;
import com.example.erp.service.PdfRenderService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

// Renders a self-contained XHTML string (no external resources — inline
// styles only) to PDF bytes via OpenHTMLtoPDF/PDFBox. Used by
// InvoiceServiceImpl/QuotationServiceImpl/PurchaseOrderServiceImpl to build
// the attachment for "email this document" — deterministic output,
// independent of the sender's browser.
@Service
public class PdfRenderServiceImpl implements PdfRenderService {

    @Override
    public byte[] renderHtmlToPdf(String html) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();
            return out.toByteArray();
        } catch (IOException e) {
            throw new AppException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not render PDF: " + e.getMessage());
        }
    }
}
