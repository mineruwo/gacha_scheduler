package com.gacha.gachascheduler.repository;

import com.gacha.gachascheduler.entity.PostEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {
    Page<PostEntity> findByChannelIdOrderByCreatedAtDesc(Long channelId, Pageable pageable);
    List<PostEntity> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
}
