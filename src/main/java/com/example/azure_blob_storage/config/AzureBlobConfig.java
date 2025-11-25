package com.example.azure_blob_storage.config;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureBlobConfig {

    @Value("${azure.storage.credential.mode:connection-string}")
    private String credentialMode;

    @Value("${SPRING_AZURE_STORAGE_CONNECTION_STRING:}")
    private String connectionString;

    @Value("${azure.storage.account-name:}")
    private String accountName;

    @Bean
    public BlobServiceClient blobServiceClient() {
        if ("managed-identity".equalsIgnoreCase(credentialMode)) {
            // Uses DefaultAzureCredential (Managed Identity when in Azure, or environment credentials)
            TokenCredential credential = new DefaultAzureCredentialBuilder().build();

            if (accountName == null || accountName.isBlank()) {
                throw new IllegalStateException("azure.storage.account-name must be set when using managed-identity mode");
            }

            String endpoint = String.format("https://%s.blob.core.windows.net", accountName);
            return new BlobServiceClientBuilder()
                    .credential(credential)
                    .endpoint(endpoint)
                    .buildClient();
        } else {
            // connection-string mode (local dev)
            if (connectionString == null || connectionString.isBlank()) {
                throw new IllegalStateException("SPRING_AZURE_STORAGE_CONNECTION_STRING environment variable must be set for connection-string mode");
            }
            return new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();
        }
    }
}
