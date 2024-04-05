package com.kh.coupang.service;

import com.kh.coupang.domain.QReviewComment;
import com.kh.coupang.domain.ReviewComment;
import com.kh.coupang.repo.ReviewCommentDAO;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewCommentService {

    @Autowired
    private ReviewCommentDAO dao;

    @Autowired
    private JPAQueryFactory queryFactory;

    private final QReviewComment qReviewComment = QReviewComment.reviewComment;


    // 댓글 추가
    public ReviewComment create(ReviewComment vo) {
        return dao.save(vo);
    }

    // 상위 댓글만 조회
    public List<ReviewComment> getTopComments(int code) {
        return queryFactory.selectFrom(qReviewComment)
                .where(qReviewComment.reviComParent.eq(0))
                .where(qReviewComment.reviCode.eq(code))
                .orderBy(qReviewComment.reviComDate.desc())
                .fetch();
    }


    // 하위 댓글만 조회
    public List<ReviewComment> getReplyComments(int parent, int code) {
        return queryFactory.selectFrom(qReviewComment)
                .where(qReviewComment.reviComParent.eq(parent))
                .where(qReviewComment.reviCode.eq(code))
                .orderBy(qReviewComment.reviComDate.asc())
                .fetch();
    }


}
