package com.gacha.gachascheduler.repository;

import com.gacha.gachascheduler.entity.CommentEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    List<CommentEntity> findByPostIdOrderByCreatedAtAsc(Long postId);
    List<CommentEntity> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
}
