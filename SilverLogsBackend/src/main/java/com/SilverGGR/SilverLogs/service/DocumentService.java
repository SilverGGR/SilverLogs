package com.SilverGGR.SilverLogs.service;

import com.SilverGGR.SilverLogs.dtos.DocumentBadgeDto;
import com.SilverGGR.SilverLogs.dtos.DocumentDto;
import com.SilverGGR.SilverLogs.entity.AuthUser;
import com.SilverGGR.SilverLogs.entity.Document;
import com.SilverGGR.SilverLogs.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final AuthUserService authUserService;
    private final DocumentRepository documentRepository;
    private final DtoMapper dtoMapper;

    @Transactional
    public ResponseEntity<String> uploadFile(MultipartFile file, String username) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File is empty");
            }

            AuthUser user = authUserService.findByUsername(username);
            if (user == null) {
                return ResponseEntity.badRequest().body("User not found");
            }

            // Validierung des Dateityps
            String contentType = file.getContentType();
            if (contentType == null ) {
                return ResponseEntity.badRequest().body("File type not supported");
            }

            Document document = new Document();
            document.setAuthUser(user);
            document.setFileContent(file.getBytes());
            document.setFileType(contentType);
            document.setFileName(file.getOriginalFilename());

            documentRepository.save(document);

            return ResponseEntity.ok("File uploaded successfully");
        } catch (Exception e) {
            System.err.println("Fehler beim Hochladen des Dokumentes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Hochladen: " + e.getMessage());
        }
    }

    @Transactional
    public DocumentDto getDocument(UUID id) {
        Document document = documentRepository.findById(id);
        if (document == null) {
            throw new RuntimeException("Dokument nicht gefunden");
        }
        return dtoMapper.convertDocumentToDto(document);
    }

    @Transactional(readOnly = true)
    public Document getDocumentEntity(UUID id) {
        Document document = documentRepository.findById(id);
        if (document == null) {
            throw new RuntimeException("Dokument nicht gefunden");
        }
        return document;
    }

    @Transactional
    public List<DocumentBadgeDto> getAllBadges(String username) {
        List<Document> documents = documentRepository.findByAuthUser_Username(username);
        return documents.stream()
                .map(dtoMapper::convertDocumentToBadgeDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Resource downloadDocument(UUID documentId) {
        Document document = documentRepository.findById(documentId);
        if (document == null) {
            throw new RuntimeException("Dokument nicht gefunden");
        }

        // Erstelle eine ByteArrayResource aus dem fileContent
        return new ByteArrayResource(document.getFileContent()) {
            @Override
            public String getFilename() {
                return document.getFileName();
            }
        };
    }

    @Transactional
    public void deleteDocument(UUID documentId) {
        Document document = documentRepository.findById(documentId);
        if (document == null) {
            return;
        }
        documentRepository.delete(document);
    }


}
