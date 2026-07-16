package com.gacha.gachascheduler.service;

import com.gacha.gachascheduler.dto.CommentMapper;
import com.gacha.gachascheduler.dto.PostMapper;
import com.gacha.gachascheduler.dto.UserHistoryResponseDto;
import com.gacha.gachascheduler.entity.ChannelEntity;
import com.gacha.gachascheduler.entity.CommentEntity;
import com.gacha.gachascheduler.entity.PostEntity;
import com.gacha.gachascheduler.entity.UserEntity;
import com.gacha.gachascheduler.repository.ChannelRepository;
import com.gacha.gachascheduler.repository.CommentRepository;
import com.gacha.gachascheduler.repository.PostRepository;
import com.gacha.gachascheduler.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 로그인 유저가 작성한 글/댓글을 모아서 보여주는 "내 활동 이력". */
@Service
@RequiredArgsConstructor
public class UserHistoryService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserHistoryResponseDto getHistory(Long userId) {
        UserEntity author = userRepository.findById(userId).orElse(null);

        List<PostEntity> posts = postRepository.findByAuthorIdOrderByCreatedAtDesc(userId);
        Map<Long, ChannelEntity> channelsById = channelRepository.findAllById(
                        posts.stream().map(PostEntity::getChannelId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(ChannelEntity::getId, Function.identity()));

        List<CommentEntity> comments = commentRepository.findByAuthorIdOrderByCreatedAtDesc(userId);

        UserHistoryResponseDto dto = new UserHistoryResponseDto();
        dto.setPosts(posts.stream()
                .map(post -> PostMapper.toDto(post, channelsById.get(post.getChannelId()), author))
                .toList());
        dto.setComments(comments.stream()
                .map(comment -> CommentMapper.toDto(comment, author))
                .toList());
        return dto;
    }
}
