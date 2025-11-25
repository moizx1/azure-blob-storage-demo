package com.example.azure_blob_storage.service;

import com.azure.storage.common.sas.SasProtocol;
import com.example.azure_blob_storage.util.FilePathBuilder;
import com.example.azure_blob_storage.config.SasConfig;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.sas.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BlobStorageService {

    private final Logger log = LoggerFactory.getLogger(BlobStorageService.class);

    private final BlobServiceClient blobServiceClient;
    private final SasConfig sasConfig;

    public BlobStorageService(BlobServiceClient blobServiceClient,
                              SasConfig sasConfig,
                              org.springframework.core.env.Environment env) {

        this.blobServiceClient = blobServiceClient;
        this.sasConfig = sasConfig;
    }

    public UploadResult uploadFile(MultipartFile file) throws Exception {
        String originalFilename = (file.getOriginalFilename() == null)
                ? "file"
                : file.getOriginalFilename();

        String blobPath = FilePathBuilder.buildPath(file.getContentType(), originalFilename);

        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(sasConfig.getContainer());
        if (!containerClient.exists()) {
            containerClient.create();
        }

        BlobClient blobClient = containerClient.getBlobClient(blobPath);

        try (InputStream is = file.getInputStream()) {
            blobClient.upload(is, file.getSize(), true);
        }

        String sasUrl = generateReadSasUrl(blobClient);

        return new UploadResult(blobPath, sasUrl, file.getContentType(), file.getSize());
    }

    public String generateReadSasUrl(String blobName) {
        BlobClient blobClient = blobServiceClient
                .getBlobContainerClient(sasConfig.getContainer())
                .getBlobClient(blobName);

        if (!blobClient.exists()) {
            throw new IllegalArgumentException("Blob does not exist: " + blobName);
        }

        return generateReadSasUrl(blobClient);
    }

    private String generateReadSasUrl(BlobClient blobClient) {

        int expiryMinutes = sasConfig.getSasExpiryMinutes();

        BlobSasPermission permission = new BlobSasPermission()
                .setReadPermission(true);

        OffsetDateTime expiry = OffsetDateTime.now().plusMinutes(expiryMinutes);
        OffsetDateTime start = OffsetDateTime.now().minusMinutes(2);

        BlobServiceSasSignatureValues values =
                new BlobServiceSasSignatureValues(expiry, permission)
                        .setStartTime(start)
                        .setProtocol(SasProtocol.HTTPS_ONLY);

        String sas = blobClient.generateSas(values);
        return blobClient.getBlobUrl() + "?" + sas;
    }

    public void deleteBlob(String blobName) {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(sasConfig.getContainer());
        BlobClient blobClient = containerClient.getBlobClient(blobName);

        if (!blobClient.exists()) {
            throw new IllegalArgumentException("Blob does not exist: " + blobName);
        }

        blobClient.delete();
    }

    public List<ListedBlob> listBlobs(boolean includeSas) {
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(sasConfig.getContainer());

        if (!containerClient.exists()) {
            throw new IllegalArgumentException("Container does not exist: " + sasConfig.getContainer());
        }

        List<ListedBlob> result = new ArrayList<>();

        for (BlobItem item : containerClient.listBlobs()) {

            String name = item.getName();
            String url = includeSas
                    ? generateReadSasUrl(name)
                    : containerClient.getBlobClient(name).getBlobUrl();

            result.add(new ListedBlob(
                    name,
                    url,
                    item.getProperties().getContentLength()
            ));
        }

        return result;
    }

    public static record UploadResult(String blobName, String url, String contentType, long size) {}
    public static record ListedBlob(String blobName, String url, Long size) {}
}
