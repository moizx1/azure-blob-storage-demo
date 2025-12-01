package com.example.azure_blob_storage.controller;

import com.example.azure_blob_storage.service.BlobStorageService;
import com.example.azure_blob_storage.store.BinaryStore;
import com.example.azure_blob_storage.store.BinaryStoreService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("${api.endpoint.base-url}")
public class AzureStorageController {

    private final BlobStorageService storageService;
    private final BinaryStoreService binaryStoreService;

    public AzureStorageController(BlobStorageService storageService, BinaryStoreService binaryStoreService) {
        this.storageService = storageService;
        this.binaryStoreService = binaryStoreService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam String clientAgreementId,
            @RequestPart MultipartFile file
    ) {
        try {
            var result = storageService.uploadFile(file, clientAgreementId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    "upload_failed", e.getMessage()
            ));
        }
    }

    @GetMapping("/download-url")
    public ResponseEntity<?> getDownloadUrl(
            @RequestParam String clientAgreementId,
            @RequestParam String blobName
    ) {
        try {
            String url = storageService.generateReadSasUrl(clientAgreementId, blobName);
            return ResponseEntity.ok(new DownloadUrlResponse(url));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    "generate_url_failed", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(
            @RequestParam String clientAgreementId,
            @RequestParam String blobName
    ) {
        try {
            storageService.deleteBlob(clientAgreementId, blobName);
            return ResponseEntity.ok(new SimpleResponse("deleted", blobName));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    "delete_failed", e.getMessage()
            ));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> list(
            @RequestParam String clientAgreementId,
            @RequestParam(value = "includeSas", defaultValue = "false") boolean includeSas
    ) {
        try {
            List<?> data = storageService.listBlobs(clientAgreementId, includeSas);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    "list_failed", e.getMessage()
            ));
        }
    }

    @GetMapping("/db-list")
    public ResponseEntity<?> listFirst100() {
        try {
            Page<BinaryStore> page = binaryStoreService.findFirst100();

            List<BinaryStoreDto> dtos = page.getContent().stream()
                    .map(this::toDto)
                    .toList();

            return ResponseEntity.ok(new BinaryStoreListResponse(
                    dtos,
                    page.getTotalElements(),
                    page.getNumber(),
                    page.getSize()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ErrorResponse("list_failed", e.getMessage())
            );
        }
    }

    private BinaryStoreDto toDto(BinaryStore entity) {
        return new BinaryStoreDto(
                entity.getId(),
                entity.getRefId(),
                entity.getBinaryContent() != null ? entity.getBinaryContent().length : 0,
                entity.getCreationTime(),
                entity.getBlobReference(),
                entity.getIsBlobMigrated()
        );
    }

    record BinaryStoreDto(
            Long id,
            String refId,
            int binaryContentSize,
            java.util.Date creationTime,
            String blobReference,
            Boolean isBlobMigrated
    ) {}
    record BinaryStoreListResponse(
            List<BinaryStoreDto> items,
            long totalElements,
            int pageNumber,
            int pageSize
    ) {}
    record DownloadUrlResponse(String url) {}
    record ErrorResponse(String error, String message) {}
    record SimpleResponse(String status, String blobName) {}
}
