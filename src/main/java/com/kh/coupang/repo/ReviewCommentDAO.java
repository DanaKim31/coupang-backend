package com.kh.coupang.repo;

import com.kh.coupang.domain.ReviewComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewCommentDAO extends JpaRepository<ReviewComment, Integer> {
    
    // 리뷰 1개에 따른 댓글 전체 조회
    @Query(value = "SELECT * FROM review_comment WHERE revi_code = :code", nativeQuery = true)
    List<ReviewComment> findByReviCode(@Param("code") int code);
}
