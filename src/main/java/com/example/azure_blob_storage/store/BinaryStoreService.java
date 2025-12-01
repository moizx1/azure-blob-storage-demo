package com.example.azure_blob_storage.store;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.azure_blob_storage.util.ApiTemplate;
import com.example.azure_blob_storage.util.ConversionUtils;
import com.example.azure_blob_storage.util.SyncContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BinaryStoreService {

    private final BinaryStoreRepository repository;
    private final ConversionUtils jsonMapper;

    public BinaryStoreService(BinaryStoreRepository repository, ConversionUtils jsonMapper) {
        this.repository = repository;
        this.jsonMapper = jsonMapper;
    }

    @Transactional(readOnly = true)
    public BinaryStore find(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Transactional(readOnly = true)
    public Page<BinaryStore> findFirst100() {
        Pageable pageable = PageRequest.of(0, 100, Sort.by(Sort.Direction.DESC, "id"));
        return repository.findAll(pageable);
    }

    @Transactional
    public BinaryStore save(BinaryStore entity) {
        return repository.save(entity);
    }

    public BinaryStore save(String refId, byte[] contents) {
        return doSave(refId, contents);
    }

    private BinaryStore doSave(String refId, byte[] contents) {
        BinaryStore binaryStore = new BinaryStore();
        binaryStore.setRefId(refId);
        binaryStore.setBinaryContent(contents);
        binaryStore.setCreationTime(new Date());
        return save(binaryStore);
    }

    public byte[] toGzipBlob(byte[] bytes) {
        return jsonMapper.toGzip(bytes);
    }

    public byte[] toBlob(byte[] bytes) {
        return bytes;
    }

    public byte[] toBlob(SyncContext context, Class<?> responseType) {
        return binaryContent(context, responseType);
    }

    public byte[] toBlob(Object obiResponse, List<RequestEntity<?>> request, List<ResponseEntity<?>> response) {
        return binaryContent(obiResponse, request, response);
    }

    private byte[] binaryContent(SyncContext context, Class<?> responseType) {
        return binaryContent(context.get(responseType.getName(), responseType),
                context.get(ApiTemplate.OBI_CONNECT_REQUEST, List.class),
                context.get(ApiTemplate.OBI_CONNECT_RESPONSE, List.class));
    }

    private byte[] binaryContent(Object obiResponse, List<RequestEntity<?>> requests,
                                 List<ResponseEntity<?>> responses) {
        Map<String, Object> contents = new LinkedHashMap<>();
        contents.put("obiApiResponse", obiResponse);
        if (requests != null) {
            addRequest(contents, requests);
        }
        if (responses != null) {
            addResponse(contents, responses);
        }
        return jsonMapper.toZip(jsonMapper.toJson(contents));
    }

    private void addRequest(Map<String, Object> contents, List<RequestEntity<?>> requests) {
        int counter = 1;
        for (RequestEntity request : requests) {
            if (request != null) {
                contents.put("obiAdapterRequest" + counter, request.getMethod() + " " + request.getUrl());
                addBody(contents, "obiAdapterRequestBody" + counter, request.getBody());
                counter++;
            }
        }
    }

    private void addResponse(Map<String, Object> contents, List<ResponseEntity<?>> responses) {
        int counter = 1;
        for (ResponseEntity response : responses) {
            if (response != null) {
                contents.put("obiAdapterResponse" + counter, response.getStatusCode().toString());
                addBody(contents, "obiAdapterResponseBody" + counter, response.getBody());
                counter++;
            }
        }
    }

    private void addBody(Map<String, Object> contents, String name, Object value) {
        if (value != null) {
            if (value instanceof byte[]) {
                try {
                    contents.put(name, jsonMapper.fromJson((byte[]) value));
                } catch (Exception e) {
                    contents.put(name, new String((byte[]) value, StandardCharsets.UTF_8));
                }
            } else {
                contents.put(name, value);
            }
        }
    }

    @Transactional
    public void delete(Long id) {
        repository.findById(id).ifPresent(value -> repository.delete(value));
    }
}
