package com.kh.coupang.repo;

import com.kh.coupang.domain.ProductComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductCommentDAO extends JpaRepository<ProductComment, Integer> {

    // 상품 1개에 따른 댓글 전체 조회
    @Query(value = "SELECT * FROM product_comment WHERE prod_code = :code", nativeQuery = true) // nativeQuery=true : jpa방식 말고 내가 짠 코드로 가져오겠다
    List<ProductComment> findByProdCode(@Param("code") int code);
}
