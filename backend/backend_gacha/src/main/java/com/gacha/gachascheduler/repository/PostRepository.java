package com.gacha.gachascheduler.repository;

import com.gacha.gachascheduler.entity.PostEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {
    Page<PostEntity> findByChannelIdOrderByCreatedAtDesc(Long channelId, Pageable pageable);
    List<PostEntity> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

    @Query("SELECT p FROM PostEntity p WHERE p.channelId = :channelId "
            + "AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) "
            + "OR LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))) "
            + "ORDER BY p.createdAt DESC")
    Page<PostEntity> searchByChannelId(@Param("channelId") Long channelId, @Param("query") String query, Pageable pageable);
}
