package com.gacha.gachascheduler.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

class FileStorageServiceTest {

    private final S3Client s3Client = Mockito.mock(S3Client.class);
    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(s3Client);
        ReflectionTestUtils.setField(fileStorageService, "endpoint", "https://kr.object.ncloudstorage.com");
        ReflectionTestUtils.setField(fileStorageService, "bucket", "test-bucket");
    }

    @Test
    void uploadsImageAndReturnsPublicUrl() {
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        MockMultipartFile file = new MockMultipartFile("file", "icon.png", "image/png", new byte[] {1, 2, 3});

        String url = fileStorageService.upload(file);

        assertThat(url).startsWith("https://kr.object.ncloudstorage.com/test-bucket/uploads/");
        assertThat(url).endsWith(".png");
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void rejectsNonImageContentType() {
        MockMultipartFile file = new MockMultipartFile("file", "a.txt", "text/plain", "hello".getBytes());

        assertThatThrownBy(() -> fileStorageService.upload(file))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void rejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

        assertThatThrownBy(() -> fileStorageService.upload(file))
                .isInstanceOf(ResponseStatusException.class);
    }
}
