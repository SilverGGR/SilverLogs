package com.SilverGGR.SilverLogs.controller;

import com.SilverGGR.SilverLogs.dtos.DocumentBadgeDto;
import com.SilverGGR.SilverLogs.dtos.DocumentDto;
import com.SilverGGR.SilverLogs.entity.Document;
import com.SilverGGR.SilverLogs.security.AuthUserPrincipal;
import com.SilverGGR.SilverLogs.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/document")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(@RequestParam("file")MultipartFile file,
                                                 @AuthenticationPrincipal AuthUserPrincipal authUserPrincipal) {
        return documentService.uploadFile(file, authUserPrincipal.getUsername());
    }

    @GetMapping("/getAllBadges")
    public ResponseEntity<List<DocumentBadgeDto>> getAllBadges(@AuthenticationPrincipal AuthUserPrincipal authUserPrincipal) {
        return ResponseEntity.ok(documentService.getAllBadges(authUserPrincipal.getUsername()));
    }

    @GetMapping("/forUser/getAllBadges/{username}")
    public ResponseEntity<List<DocumentBadgeDto>> getAllApprenticeBadges(@PathVariable String username) {
        return ResponseEntity.ok(documentService.getAllBadges(username));
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<DocumentDto> getDocument(@PathVariable UUID documentId) {
        return ResponseEntity.ok(documentService.getDocument(documentId));
    }

    @GetMapping("/download/{documentId}")
    public ResponseEntity<Resource> downloadDocument(@PathVariable UUID documentId) {
        Resource resource = documentService.downloadDocument(documentId);
        Document document = documentService.getDocumentEntity(documentId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);

    }

    @GetMapping("/preview/{documentId}")
    public ResponseEntity<Resource> previewDocument(@PathVariable UUID documentId) {
        Resource resource = documentService.downloadDocument(documentId);
        Document document = documentService.getDocumentEntity(documentId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable UUID documentId) {
        documentService.deleteDocument(documentId);
        return ResponseEntity.ok().build();
    }


}
