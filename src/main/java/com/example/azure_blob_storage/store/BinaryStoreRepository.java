package com.example.azure_blob_storage.store;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BinaryStoreRepository extends JpaRepository<BinaryStore, Long> {
}
