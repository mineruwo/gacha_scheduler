package com.gacha.gachascheduler.service;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/** 관리자 화면(공지/팝업 배너, 가챠 캐릭터 아이콘 등)에서 쓰는 이미지를 NCP Object Storage에 업로드. */
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/png", "image/jpeg", "image/webp", "image/gif");

    private final S3Client s3Client;

    @Value("${ncp.object-storage.endpoint}")
    private String endpoint;

    @Value("${ncp.object-storage.bucket}")
    private String bucket;

    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "업로드할 파일이 없습니다.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미지 파일(png/jpeg/webp/gif)만 업로드할 수 있습니다.");
        }

        String key = "uploads/" + UUID.randomUUID() + extensionFor(contentType);

        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .acl(ObjectCannedACL.PUBLIC_READ)
                            .build(),
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다.", e);
        }

        return endpoint + "/" + bucket + "/" + key;
    }

    private String extensionFor(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> "";
        };
    }
}
