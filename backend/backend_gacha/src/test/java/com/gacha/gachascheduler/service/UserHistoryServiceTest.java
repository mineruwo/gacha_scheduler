package com.gacha.gachascheduler.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.gacha.gachascheduler.dto.CommentRequestDto;
import com.gacha.gachascheduler.dto.PostRequestDto;
import com.gacha.gachascheduler.dto.UserHistoryResponseDto;
import com.gacha.gachascheduler.entity.ChannelEntity;
import com.gacha.gachascheduler.entity.GameEntity;
import com.gacha.gachascheduler.entity.UserEntity;
import com.gacha.gachascheduler.enums.PostTemplateType;
import com.gacha.gachascheduler.repository.ChannelRepository;
import com.gacha.gachascheduler.repository.GameRepository;
import com.gacha.gachascheduler.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({UserHistoryService.class, PostService.class, CommentService.class})
class UserHistoryServiceTest {

    @Autowired
    private UserHistoryService userHistoryService;

    @Autowired
    private PostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void historyIncludesOwnPostsAndCommentsOnly() {
        Long me = createUser("me@example.com", "Me");
        Long someoneElse = createUser("other@example.com", "Other");
        Long channelId = createChannel();

        PostRequestDto myPostRequest = new PostRequestDto();
        myPostRequest.setChannelId(channelId);
        myPostRequest.setTitle("내 글");
        myPostRequest.setContent("내용");
        myPostRequest.setTemplateType(PostTemplateType.FREE);
        var myPost = postService.createPost(me, myPostRequest);

        PostRequestDto otherPostRequest = new PostRequestDto();
        otherPostRequest.setChannelId(channelId);
        otherPostRequest.setTitle("남의 글");
        otherPostRequest.setContent("내용");
        otherPostRequest.setTemplateType(PostTemplateType.FREE);
        postService.createPost(someoneElse, otherPostRequest);

        CommentRequestDto myCommentRequest = new CommentRequestDto();
        myCommentRequest.setContent("내 댓글");
        commentService.createComment(myPost.getId(), me, myCommentRequest);

        UserHistoryResponseDto history = userHistoryService.getHistory(me);

        assertThat(history.getPosts()).hasSize(1);
        assertThat(history.getPosts().get(0).getTitle()).isEqualTo("내 글");
        assertThat(history.getComments()).hasSize(1);
        assertThat(history.getComments().get(0).getContent()).isEqualTo("내 댓글");
    }

    private Long createUser(String email, String name) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setName(name);
        user.setGoogleId(UUID.randomUUID().toString());
        user.setUserCode("01_" + UUID.randomUUID());
        return userRepository.save(user).getId();
    }

    private Long createChannel() {
        GameEntity game = new GameEntity();
        game.setTitle("History Test Game");
        game.setGameCode("history-test-game-" + UUID.randomUUID());
        game = gameRepository.save(game);

        ChannelEntity channel = new ChannelEntity();
        channel.setGameId(game.getId());
        channel.setName("공략 채널");
        return channelRepository.save(channel).getId();
    }
}
