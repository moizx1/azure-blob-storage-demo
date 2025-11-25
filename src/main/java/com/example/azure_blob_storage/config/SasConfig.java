package com.example.azure_blob_storage.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SasConfig {

    @Value("${azure.sas.expiry.minutes}")
    private int sasExpiryMinutes;

    @Value("${azure.storage.default-container}")
    private String container;

    public int getSasExpiryMinutes() {
        return sasExpiryMinutes;
    }

    public String getContainer() {
        return container;
    }

}
