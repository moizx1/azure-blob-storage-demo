package com.example.azure_blob_storage.util;

public class FilePathBuilder {

    public static String buildPath(String contentType, String originalFilename) {

        String folder = resolveFolder(contentType);
        String sanitized = sanitizeFilename(originalFilename);

        return "files/" + folder + "/" + sanitized;
    }

    private static String resolveFolder(String contentType) {
        if (contentType == null) return "other";

        if (contentType.equalsIgnoreCase("application/pdf")) {
            return "pdf";
        }
        if (contentType.startsWith("image/")) {
            return "images";
        }
        if (contentType.startsWith("text/")) {
            return "text";
        }

        return "other";
    }

    private static String sanitizeFilename(String filename) {
        if (filename == null) return "file";
        return filename.replaceAll("[^a-zA-Z0-9\\.\\-\\_]", "_");
    }
}
