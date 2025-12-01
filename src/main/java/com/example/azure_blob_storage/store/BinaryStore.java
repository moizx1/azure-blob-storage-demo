package com.example.azure_blob_storage.store;

import jakarta.persistence.*;

import java.util.Date;


@Entity(name = "BinaryStore")
public class BinaryStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "RefId")
    private String refId;

    @Column(name = "BinaryContent")
    private byte[] binaryContent;

    @Column(name = "CreationTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationTime;

    // ✅ ADD THESE TWO
    @Column(name = "BlobReference")
    private String blobReference;

    @Column(name = "IsBlobMigrated")
    private Boolean isBlobMigrated;

    // ✅ Getters & Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRefId() { return refId; }
    public void setRefId(String refId) { this.refId = refId; }

    public byte[] getBinaryContent() { return binaryContent; }
    public void setBinaryContent(byte[] binaryContent) { this.binaryContent = binaryContent; }

    public Date getCreationTime() { return creationTime; }
    public void setCreationTime(Date creationTime) { this.creationTime = creationTime; }

    public String getBlobReference() { return blobReference; }
    public void setBlobReference(String blobReference) { this.blobReference = blobReference; }

    public Boolean getIsBlobMigrated() { return isBlobMigrated; }
    public void setIsBlobMigrated(Boolean isBlobMigrated) { this.isBlobMigrated = isBlobMigrated; }
}
