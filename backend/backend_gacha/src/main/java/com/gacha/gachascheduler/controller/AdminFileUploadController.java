package com.gacha.gachascheduler.controller;

import com.gacha.gachascheduler.dto.FileUploadResponseDto;
import com.gacha.gachascheduler.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 관리자 화면에서 이미지를 올리고 공개 URL을 받기 위한 업로드 엔드포인트(NCP Object Storage). */
@RestController
@RequestMapping("/api/admin/files")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUB_ADMIN', 'MAIN_ADMIN')")
public class AdminFileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponseDto> upload(@RequestParam("file") MultipartFile file) {
        String url = fileStorageService.upload(file);
        return ResponseEntity.ok(new FileUploadResponseDto(url));
    }
}
