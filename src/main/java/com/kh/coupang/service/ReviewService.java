package com.kh.coupang.service;

import com.kh.coupang.domain.QReview;
import com.kh.coupang.domain.QReviewImage;
import com.kh.coupang.domain.Review;
import com.kh.coupang.domain.ReviewImage;
import com.kh.coupang.repo.ReviewDAO;
import com.kh.coupang.repo.ReviewImageDAO;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewDAO review;

    @Autowired
    private ReviewImageDAO image;

    @Autowired
    private JPAQueryFactory queryFactory;
    private final QReviewImage qReviewImage = QReviewImage.reviewImage;


    // 추가
    public Review create(Review rv) {
        return review.save(rv);
    }
    public ReviewImage createImg(ReviewImage img) {
        return image.save(img);
    }


    // 전체 리뷰
    public Page<Review> viewAll(Pageable paging, BooleanBuilder builder) {
        return review.findAll(builder, paging); // 순서 중요!!
    }


    // 각 리뷰에 등록된 이미지 전체 조회
    public List<ReviewImage> getReviImages(int code) {
//        queryFactory 사용 시 DAO에 @Query 작성 안해도 됨!
//        return queryFactory.selectFrom(qReviewImage)
//                .where(qReviewImage.review.reviCode.eq(code))
//                .fetch();
        return image.findByReviCode(code);
    }

    // 이미지 삭제
    public void deleteImage(int code) {
        if (image.existsById(code))
         image.deleteById(code);
    }

    // 리뷰 삭제
    public void delete(int code) {
        if(review.existsById(code)) {
            review.deleteById(code);
        }
    }


}
