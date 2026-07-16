package com.gacha.gachascheduler.service;

import com.gacha.gachascheduler.dto.CommentMapper;
import com.gacha.gachascheduler.dto.CommentRequestDto;
import com.gacha.gachascheduler.dto.CommentResponseDto;
import com.gacha.gachascheduler.entity.CommentEntity;
import com.gacha.gachascheduler.entity.UserEntity;
import com.gacha.gachascheduler.enums.Role;
import com.gacha.gachascheduler.repository.CommentRepository;
import com.gacha.gachascheduler.repository.PostRepository;
import com.gacha.gachascheduler.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentResponseDto createComment(Long postId, Long authorId, CommentRequestDto request) {
        requireExistingPost(postId);
        if (request.getParentCommentId() != null) {
            requireExistingComment(request.getParentCommentId());
        }

        CommentEntity comment = new CommentEntity();
        comment.setPostId(postId);
        comment.setAuthorId(authorId);
        comment.setContent(request.getContent());
        comment.setParentCommentId(request.getParentCommentId());
        CommentEntity created = commentRepository.save(comment);

        UserEntity author = userRepository.findById(authorId).orElse(null);
        return CommentMapper.toDto(created, author);
    }

    @Transactional
    public void deleteComment(Long commentId, Long requesterId, Role requesterRole) {
        CommentEntity comment = requireExistingComment(commentId);
        boolean isAdmin = requesterRole == Role.SUB_ADMIN || requesterRole == Role.MAIN_ADMIN;
        if (!isAdmin && !comment.getAuthorId().equals(requesterId)) {
            throw new AccessDeniedException("본인 댓글만 삭제할 수 있습니다.");
        }
        commentRepository.deleteById(commentId);
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByPost(Long postId) {
        List<CommentEntity> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);

        Map<Long, UserEntity> authorsById = userRepository.findAllById(
                        comments.stream().map(CommentEntity::getAuthorId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity()));

        return comments.stream()
                .map(comment -> CommentMapper.toDto(comment, authorsById.get(comment.getAuthorId())))
                .toList();
    }

    private void requireExistingPost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new RuntimeException("Post not found with id " + postId);
        }
    }

    private CommentEntity requireExistingComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id " + commentId));
    }
}
