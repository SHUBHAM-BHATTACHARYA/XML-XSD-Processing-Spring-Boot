package com.example.shubham.xmlService.xmlProcessing.controller;

import com.example.shubham.xmlService.xmlProcessing.entity.XmlPayloadRecord;
import com.example.shubham.xmlService.xmlProcessing.service.XmlProcessingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.util.Map;

@RestController
@RequestMapping("/api/xml")
public class XmlPayloadController {

    private final XmlProcessingService processingService;

    public XmlPayloadController(XmlProcessingService processingService) {
        this.processingService = processingService;
    }

    @Operation(summary = "1. Upload XML file, validate against XSD, and save to DB",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadXmlFile(
            @Parameter(
                    description = "XML file matching order.xsd",
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestPart("file") MultipartFile file) {
        try {
            XmlPayloadRecord saved = processingService.validateAndSaveXml(file);
            return ResponseEntity.ok(saved);
        } catch (SAXException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "XSD Validation Failed", "details", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Processing Error", "details", e.getMessage()));
        }
    }

    @Operation(summary = "2. Fetch XML by ID, process operation, and save to response field",
            security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/{id}/process")
    public ResponseEntity<?> processXmlRecord(@PathVariable Long id) {
        try {
            XmlPayloadRecord updated = processingService.processAndSaveResponse(id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process XML", "details", e.getMessage()));
        }
    }

    @Operation(summary = "3. Fetch only the request XML payload by ID",
                security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/{id}/requestPayload", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<?> getRequestPayload(@PathVariable Long id) {
        try {
            String responseXml = processingService.getRequestPayload(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .body(responseXml);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "4. Fetch only the processed response XML payload by ID",
            security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/{id}/responsePayload", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<?> getResponsePayload(@PathVariable Long id) {
        try {
            String responseXml = processingService.getResponsePayload(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .body(responseXml);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}