package com.gacha.gachascheduler.controller;

import com.gacha.gachascheduler.dto.CommentRequestDto;
import com.gacha.gachascheduler.dto.CommentResponseDto;
import com.gacha.gachascheduler.security.AuthenticatedUser;
import com.gacha.gachascheduler.service.CommentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 댓글 조회는 누구나 가능하고, 작성은 로그인이 필요하다. 삭제는 본인 댓글이거나 관리자여야 한다
 * (CommentService에서 검증, 위반 시 403).
 */
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/api/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }

    @PostMapping("/api/posts/{postId}/comments")
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable Long postId,
            @AuthenticationPrincipal AuthenticatedUser principal,
            @RequestBody CommentRequestDto request) {
        CommentResponseDto created = commentService.createComment(postId, principal.userId(), request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @DeleteMapping("/api/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId, @AuthenticationPrincipal AuthenticatedUser principal) {
        commentService.deleteComment(commentId, principal.userId(), principal.role());
        return ResponseEntity.noContent().build();
    }
}
