package com.gacha.gachascheduler.service;

import com.gacha.gachascheduler.dto.PostMapper;
import com.gacha.gachascheduler.dto.PostRequestDto;
import com.gacha.gachascheduler.dto.PostResponseDto;
import com.gacha.gachascheduler.entity.ChannelEntity;
import com.gacha.gachascheduler.entity.PostEntity;
import com.gacha.gachascheduler.entity.UserEntity;
import com.gacha.gachascheduler.enums.Role;
import com.gacha.gachascheduler.repository.ChannelRepository;
import com.gacha.gachascheduler.repository.PostRepository;
import com.gacha.gachascheduler.repository.UserRepository;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    @Transactional
    public PostResponseDto createPost(Long authorId, PostRequestDto request) {
        ChannelEntity channel = requireExistingChannel(request.getChannelId());

        PostEntity post = new PostEntity();
        post.setChannelId(request.getChannelId());
        post.setAuthorId(authorId);
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setTemplateType(request.getTemplateType());
        post.setViewCount(0);
        PostEntity created = postRepository.save(post);

        UserEntity author = userRepository.findById(authorId).orElse(null);
        return PostMapper.toDto(created, channel, author);
    }

    @Transactional
    public PostResponseDto updatePost(Long postId, Long requesterId, Role requesterRole, PostRequestDto request) {
        PostEntity post = getPostOrThrow(postId);
        requireOwnerOrAdmin(post.getAuthorId(), requesterId, requesterRole);

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setTemplateType(request.getTemplateType());
        PostEntity saved = postRepository.save(post);

        ChannelEntity channel = channelRepository.findById(saved.getChannelId()).orElse(null);
        UserEntity author = userRepository.findById(saved.getAuthorId()).orElse(null);
        return PostMapper.toDto(saved, channel, author);
    }

    @Transactional
    public void deletePost(Long postId, Long requesterId, Role requesterRole) {
        PostEntity post = getPostOrThrow(postId);
        requireOwnerOrAdmin(post.getAuthorId(), requesterId, requesterRole);
        postRepository.deleteById(postId);
    }

    @Transactional
    public PostResponseDto getPostAndIncrementView(Long postId) {
        PostEntity post = getPostOrThrow(postId);
        post.setViewCount(post.getViewCount() + 1);
        PostEntity saved = postRepository.save(post);

        ChannelEntity channel = channelRepository.findById(saved.getChannelId()).orElse(null);
        UserEntity author = userRepository.findById(saved.getAuthorId()).orElse(null);
        return PostMapper.toDto(saved, channel, author);
    }

    @Transactional(readOnly = true)
    public Page<PostResponseDto> getPostsByChannel(Long channelId, Pageable pageable) {
        Page<PostEntity> posts = postRepository.findByChannelIdOrderByCreatedAtDesc(channelId, pageable);

        ChannelEntity channel = channelRepository.findById(channelId).orElse(null);
        Map<Long, UserEntity> authorsById = userRepository.findAllById(
                        posts.stream().map(PostEntity::getAuthorId).distinct().toList())
                .stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity()));

        return posts.map(post -> PostMapper.toDto(post, channel, authorsById.get(post.getAuthorId())));
    }

    private PostEntity getPostOrThrow(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id " + postId));
    }

    private ChannelEntity requireExistingChannel(Long channelId) {
        return channelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found with id " + channelId));
    }

    private void requireOwnerOrAdmin(Long authorId, Long requesterId, Role requesterRole) {
        boolean isAdmin = requesterRole == Role.SUB_ADMIN || requesterRole == Role.MAIN_ADMIN;
        if (!isAdmin && !authorId.equals(requesterId)) {
            throw new AccessDeniedException("본인 글만 수정/삭제할 수 있습니다.");
        }
    }
}
