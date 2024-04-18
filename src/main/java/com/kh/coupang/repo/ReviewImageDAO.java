package com.kh.coupang.repo;

import com.kh.coupang.domain.ReviewComment;
import com.kh.coupang.domain.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewImageDAO extends JpaRepository<ReviewImage, Integer> { // 해당하는 리뷰 전체, primary key로 가져오기만 제공 >> @Query 추가
    // 각 리뷰에 등록된 이미지 전체 조회
    @Query(value = "SELECT * FROM review_image WHERE revi_code = :code", nativeQuery = true)
    List<ReviewImage> findByReviCode(@Param("code") Integer code); // DB에서 null인 경우가 있기 때문에 Integer

}
