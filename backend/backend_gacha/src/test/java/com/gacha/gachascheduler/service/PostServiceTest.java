package com.gacha.gachascheduler.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gacha.gachascheduler.dto.PostRequestDto;
import com.gacha.gachascheduler.dto.PostResponseDto;
import com.gacha.gachascheduler.entity.ChannelEntity;
import com.gacha.gachascheduler.entity.GameEntity;
import com.gacha.gachascheduler.entity.UserEntity;
import com.gacha.gachascheduler.enums.PostTemplateType;
import com.gacha.gachascheduler.enums.Role;
import com.gacha.gachascheduler.repository.ChannelRepository;
import com.gacha.gachascheduler.repository.GameRepository;
import com.gacha.gachascheduler.repository.UserRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;

@DataJpaTest
@Import(PostService.class)
class PostServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void createPostAndFetchIncrementsViewCount() {
        Long channelId = createChannel();
        Long authorId = createUser();

        PostResponseDto created = postService.createPost(authorId, requestFor(channelId, "제목", "내용"));
        assertThat(created.getViewCount()).isEqualTo(0);
        assertThat(created.getChannelName()).isNotBlank();
        assertThat(created.getAuthorName()).isNotBlank();

        PostResponseDto firstView = postService.getPostAndIncrementView(created.getId());
        PostResponseDto secondView = postService.getPostAndIncrementView(created.getId());

        assertThat(firstView.getViewCount()).isEqualTo(1);
        assertThat(secondView.getViewCount()).isEqualTo(2);
    }

    @Test
    void authorCanUpdateOwnPost() {
        Long channelId = createChannel();
        Long authorId = createUser();
        PostResponseDto created = postService.createPost(authorId, requestFor(channelId, "제목", "내용"));

        PostResponseDto updated = postService.updatePost(
                created.getId(), authorId, Role.USER, requestFor(channelId, "수정된 제목", "수정된 내용"));

        assertThat(updated.getTitle()).isEqualTo("수정된 제목");
    }

    @Test
    void otherUserCannotUpdateSomeoneElsesPost() {
        Long channelId = createChannel();
        Long authorId = createUser();
        Long otherUserId = createUser();
        PostResponseDto created = postService.createPost(authorId, requestFor(channelId, "제목", "내용"));

        assertThatThrownBy(() -> postService.updatePost(
                created.getId(), otherUserId, Role.USER, requestFor(channelId, "해킹시도", "내용")))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getPostsByChannelReturnsOnlyThatChannelsPostsNewestFirst() {
        Long channelId = createChannel();
        Long otherChannelId = createChannel();
        Long authorId = createUser();

        postService.createPost(authorId, requestFor(channelId, "첫 글", "내용"));
        PostResponseDto second = postService.createPost(authorId, requestFor(channelId, "둘째 글", "내용"));
        postService.createPost(authorId, requestFor(otherChannelId, "다른 채널 글", "내용"));

        var page = postService.getPostsByChannel(channelId, null, PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent().get(0).getId()).isEqualTo(second.getId());
    }

    @Test
    void getPostsByChannelWithQueryMatchesTitleOrContentCaseInsensitively() {
        Long channelId = createChannel();
        Long authorId = createUser();
        postService.createPost(authorId, requestFor(channelId, "Purina 공략", "내용"));
        postService.createPost(authorId, requestFor(channelId, "다른 글", "여기 PURINA 언급"));
        postService.createPost(authorId, requestFor(channelId, "무관한 글", "무관한 내용"));

        var page = postService.getPostsByChannel(channelId, "purina", PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
    }

    @Test
    void adminCanDeleteSomeoneElsesPost() {
        Long channelId = createChannel();
        Long authorId = createUser();
        Long adminId = createUser();
        PostResponseDto created = postService.createPost(authorId, requestFor(channelId, "제목", "내용"));

        postService.deletePost(created.getId(), adminId, Role.MAIN_ADMIN);

        assertThatThrownBy(() -> postService.getPostAndIncrementView(created.getId()))
                .isInstanceOf(RuntimeException.class);
    }

    private Long createChannel() {
        GameEntity game = new GameEntity();
        game.setTitle("Board Test Game");
        game.setGameCode("board-test-game-" + UUID.randomUUID());
        game = gameRepository.save(game);

        ChannelEntity channel = new ChannelEntity();
        channel.setGameId(game.getId());
        channel.setName("공략 채널");
        channel = channelRepository.save(channel);
        return channel.getId();
    }

    private Long createUser() {
        UserEntity user = new UserEntity();
        user.setEmail(UUID.randomUUID() + "@example.com");
        user.setName("Tester");
        user.setGoogleId(UUID.randomUUID().toString());
        user.setUserCode("01_" + UUID.randomUUID());
        return userRepository.save(user).getId();
    }

    private PostRequestDto requestFor(Long channelId, String title, String content) {
        PostRequestDto request = new PostRequestDto();
        request.setChannelId(channelId);
        request.setTitle(title);
        request.setContent(content);
        request.setTemplateType(PostTemplateType.GUIDE);
        return request;
    }
}
