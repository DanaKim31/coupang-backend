package com.kh.coupang.service;

import com.kh.coupang.domain.Review;
import com.kh.coupang.domain.ReviewImage;
import com.kh.coupang.repo.ReviewDAO;
import com.kh.coupang.repo.ReviewImageDAO;
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

    // 추가
    public Review create(Review rv) {
        return review.save(rv);
    }

    public ReviewImage createImg(ReviewImage img) {
        return image.save(img);
    }


    // 전체 리뷰
    public Page<Review> viewAll(Pageable paging) {
        return review.findAll(paging);
    }



}
