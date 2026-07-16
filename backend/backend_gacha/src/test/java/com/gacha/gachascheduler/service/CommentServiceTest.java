package com.gacha.gachascheduler.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gacha.gachascheduler.dto.CommentRequestDto;
import com.gacha.gachascheduler.dto.CommentResponseDto;
import com.gacha.gachascheduler.dto.PostRequestDto;
import com.gacha.gachascheduler.entity.ChannelEntity;
import com.gacha.gachascheduler.entity.GameEntity;
import com.gacha.gachascheduler.entity.UserEntity;
import com.gacha.gachascheduler.enums.PostTemplateType;
import com.gacha.gachascheduler.enums.Role;
import com.gacha.gachascheduler.repository.ChannelRepository;
import com.gacha.gachascheduler.repository.GameRepository;
import com.gacha.gachascheduler.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;

@DataJpaTest
@Import({CommentService.class, PostService.class})
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private PostService postService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createCommentAndReplyAreListedInOrder() {
        Long authorId = createUser();
        Long postId = createPost(authorId);

        CommentResponseDto parent = commentService.createComment(
                postId, authorId, requestFor("첫 댓글", null));
        commentService.createComment(postId, authorId, requestFor("대댓글", parent.getId()));

        List<CommentResponseDto> comments = commentService.getCommentsByPost(postId);

        assertThat(comments).hasSize(2);
        assertThat(comments.get(0).getContent()).isEqualTo("첫 댓글");
        assertThat(comments.get(1).getParentCommentId()).isEqualTo(parent.getId());
    }

    @Test
    void otherUserCannotDeleteSomeoneElsesComment() {
        Long authorId = createUser();
        Long otherUserId = createUser();
        Long postId = createPost(authorId);
        CommentResponseDto comment = commentService.createComment(postId, authorId, requestFor("댓글", null));

        assertThatThrownBy(() -> commentService.deleteComment(comment.getId(), otherUserId, Role.USER))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void authorCanDeleteOwnComment() {
        Long authorId = createUser();
        Long postId = createPost(authorId);
        CommentResponseDto comment = commentService.createComment(postId, authorId, requestFor("댓글", null));

        commentService.deleteComment(comment.getId(), authorId, Role.USER);

        assertThat(commentService.getCommentsByPost(postId)).isEmpty();
    }

    private Long createPost(Long authorId) {
        GameEntity game = new GameEntity();
        game.setTitle("Comment Test Game");
        game.setGameCode("comment-test-game-" + UUID.randomUUID());
        game = gameRepository.save(game);

        ChannelEntity channel = new ChannelEntity();
        channel.setGameId(game.getId());
        channel.setName("공략 채널");
        channel = channelRepository.save(channel);

        PostRequestDto request = new PostRequestDto();
        request.setChannelId(channel.getId());
        request.setTitle("제목");
        request.setContent("내용");
        request.setTemplateType(PostTemplateType.FREE);
        return postService.createPost(authorId, request).getId();
    }

    private Long createUser() {
        UserEntity user = new UserEntity();
        user.setEmail(UUID.randomUUID() + "@example.com");
        user.setName("Tester");
        user.setGoogleId(UUID.randomUUID().toString());
        user.setUserCode("01_" + UUID.randomUUID());
        return userRepository.save(user).getId();
    }

    private CommentRequestDto requestFor(String content, Long parentCommentId) {
        CommentRequestDto request = new CommentRequestDto();
        request.setContent(content);
        request.setParentCommentId(parentCommentId);
        return request;
    }
}
