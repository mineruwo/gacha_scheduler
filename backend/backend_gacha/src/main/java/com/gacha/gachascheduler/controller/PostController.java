package com.gacha.gachascheduler.controller;

import com.gacha.gachascheduler.dto.PostRequestDto;
import com.gacha.gachascheduler.dto.PostResponseDto;
import com.gacha.gachascheduler.security.AuthenticatedUser;
import com.gacha.gachascheduler.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 조회(GET)는 로그인 여부와 관계없이 누구나 가능하고, 작성/수정/삭제는 로그인이 필요하다.
 * 수정/삭제는 본인 글이거나 관리자(SUB_ADMIN/MAIN_ADMIN)여야 한다(PostService에서 검증, 위반 시 403).
 */
@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/api/channels/{channelId}/posts")
    public ResponseEntity<Page<PostResponseDto>> getPostsByChannel(
            @PathVariable Long channelId,
            @RequestParam(required = false) String query,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(postService.getPostsByChannel(channelId, query, pageable));
    }

    @PostMapping("/api/channels/{channelId}/posts")
    public ResponseEntity<PostResponseDto> createPost(
            @PathVariable Long channelId,
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestBody PostRequestDto request) {
        request.setChannelId(channelId);
        return new ResponseEntity<>(postService.createPost(principal.userId(), request), HttpStatus.CREATED);
    }

    @GetMapping("/api/posts/{postId}")
    public ResponseEntity<PostResponseDto> getPost(@PathVariable Long postId) {
        return ResponseEntity.ok(postService.getPostAndIncrementView(postId));
    }

    @PutMapping("/api/posts/{postId}")
    public ResponseEntity<PostResponseDto> updatePost(
            @PathVariable Long postId,
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestBody PostRequestDto request) {
        return ResponseEntity.ok(
                postService.updatePost(postId, principal.userId(), principal.role(), request));
    }

    @DeleteMapping("/api/posts/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId, @AuthenticationPrincipal AuthenticatedUser principal) {
        postService.deletePost(postId, principal.userId(), principal.role());
        return ResponseEntity.noContent().build();
    }
}
