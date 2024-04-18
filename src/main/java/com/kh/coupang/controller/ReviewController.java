package com.kh.coupang.controller;

import com.kh.coupang.domain.*;
import com.kh.coupang.service.ReviewCommentService;
import com.kh.coupang.service.ReviewService;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/*")
@CrossOrigin(origins ={"*"}, maxAge = 6000)
public class ReviewController {

    @Autowired
    private ReviewService service;

    @Autowired
    private ReviewCommentService commentService;

    @Autowired
    private JPAQueryFactory queryFactory;
    final private QReviewImage qReviewImage = QReviewImage.reviewImage;

    @Value("${spring.servlet.multipart.location}")
    private String uploadPath;

    // 리뷰 추가
    @PostMapping("/review")
    public ResponseEntity<Review> create(ReviewDTO dto) throws IOException {

        // review부터 추가하여 revi_code가 담긴 review! - DynamaicInsert
        Review rv = new Review();
        rv.setId(dto.getId());
        rv.setProdCode(dto.getProdCode());
        rv.setReviTitle(dto.getReviTitle());
        rv.setReviDesc(dto.getReviDesc());
        rv.setRating(dto.getRating());

        Review result = service.create(rv);
        log.info("result : " + result);

        if(dto.getFiles() != null) { // 이미지 없이도 등록 가능
        // review_image에는 revi_code가 필요!
        for(MultipartFile file : dto.getFiles()) {
            ReviewImage imgVo = new ReviewImage();

            // 파일 업로드
            String fileName = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            String saveName = uploadPath + File.separator + "review" + File.separator + uuid + "_" + fileName;
            Path savePath = Paths.get(saveName);
            file.transferTo(savePath);

            imgVo.setReviUrl(saveName);
            imgVo.setReview(result);

            service.createImg(imgVo);
        }
        }

        return result != null ?
            ResponseEntity.status(HttpStatus.CREATED).body(result) :
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    // http://localhost:8080/api/public/product/47/review
    // 상품 1개에 따른 리뷰 전체 보기
    @GetMapping("/public/product/{code}/review")
    public ResponseEntity<List<Review>> viewAll(@RequestParam(name="page", defaultValue = "1")int page, @PathVariable(name="code") int code) {
        Sort sort = Sort.by("reviCode").descending();
        Pageable paging = PageRequest.of(page-1, 5, sort);

        QReview qReview = QReview.review;

        BooleanBuilder builder = new BooleanBuilder();
        BooleanExpression expression = qReview.prodCode.eq(code);
        builder.and(expression);

        Page<Review> list = service.viewAll(paging, builder);
        return ResponseEntity.status(HttpStatus.OK).body(list.getContent());
    }

    // 리뷰 삭제
    @DeleteMapping("review/{code}")
    public ResponseEntity delete(@PathVariable(name = "code") int code) {

        // 이미지들 삭제하면서 리뷰 이미지 테이블에서도 삭제
        // 1. 이미지 테이블에서 해당 reviCode에 대한 이미지들 가져오기 >> SELECT문 생각, DAO에 추가 후 Service에 반영해서 가져오기
            // SELECT * FROM review_image WHERE revi_code = 42
            // QueryDSL 방법으로도 가지고 올 수 있음!
        List<ReviewImage> uploadedImgs = service.getReviImages(code);

        for(ReviewImage image : uploadedImgs) {
            // 2. 반복문을 돌려서 각각의 image에 있는 URL로 File 객체로 file.delete() 사용 >> 로컬 폴더에 있는 이미지 파일 삭제
            File file = new File(image.getReviUrl());
            file.delete();
            // 3. 반복문 안에서 그와 동시에 이미지 테이블에서 이미지의 Code로 삭제 기능 진행(reviImgCode) >> DB에 저장된 이미지 삭제
            service.deleteImage(image.getReviImgCode());
        }

        // 리뷰 삭제 : reviCode로 삭제
        service.delete(code);
        
        return ResponseEntity.ok().build();
    }

    // 리뷰 수정
    @PutMapping("/review") // form-data도 RequestBody이지만 스프링부트에서는 생략해야 함
    public ResponseEntity update(ReviewDTO dto) throws IOException {
        log.info("dto : " + dto);

        // =========== 리뷰 코드 가져와서 기존 이미지 파일 관련 처리 ===========
            // dto.images에 있으면 삭제하지 않은 사진들 / 없으면 삭제한 기존 사진들
        // 1. 기존 리뷰에 있던 이미지들 정보 가져오기
        List<ReviewImage> uploadedImgs = service.getReviImages(dto.getReviCode());
        // 2. 반복문을 돌려서 dto.images에 해당 이미지가 포함되어 있는지 판단
        for(ReviewImage image : uploadedImgs) {
            // dto.getImages().contains(image.getReviUrl())
            // 3. 위 코드로 조건을 걸어서 로컬 폴더에 있는 파일 삭제
            if((dto.getImages()!=null && !dto.getImages().contains(image.getReviUrl())) || dto.getImages() == null) {
                File file = new File(image.getReviUrl());
                file.delete();

                // 4. 파일 삭제와 동시에 테이블에서도 해당 정보 삭제
                service.deleteImage(image.getReviImgCode());
            }
        }

        // =========== dto.files에 새로 추가된 사진들 추가 ===========
        if(dto.getFiles() != null) { // 이미지 없이도 등록 가능
            // review_image에는 revi_code가 필요!
            for(MultipartFile file : dto.getFiles()) {
                ReviewImage imgVo = new ReviewImage();

                // 파일 업로드
                String fileName = file.getOriginalFilename();
                String uuid = UUID.randomUUID().toString();
                String saveName = uploadPath + File.separator + "review" + File.separator + uuid + "_" + fileName;
                Path savePath = Paths.get(saveName);
                file.transferTo(savePath);

                imgVo.setReviUrl(saveName);
                imgVo.setReview(Review.builder().reviCode(dto.getReviCode()).build());

                service.createImg(imgVo);
            }
        }

        // 리뷰 수정
        Review vo = Review.builder()
                .reviCode(dto.getReviCode())
                .id(dto.getId())
                .prodCode(dto.getProdCode())
                .reviTitle(dto.getReviTitle())
                .reviDesc(dto.getReviDesc())
                .build();
        service.create(vo);
        return ResponseEntity.ok().build();
    }


    // 리뷰 댓글 추가
    @PostMapping("/review/comment")
    public ResponseEntity createComment(@RequestBody ReviewComment vo) {

        Object principal = authentication();

        if(principal instanceof User) {
            User user = (User) principal;
            vo.setUser(user);
            return ResponseEntity.ok(commentService.create(vo));
        }

        return ResponseEntity.badRequest().build();
    }


    // 리뷰 1개에 따른 댓글 조회
    @GetMapping("/public/review/{code}/comment")
    public ResponseEntity<List<ReviewCommentDTO>> viewComment(@PathVariable(name="code") int code) {
        List<ReviewComment> topList = commentService.getTopComments(code);
        List<ReviewCommentDTO> response = new ArrayList<>();

        for(ReviewComment top : topList) {
            List<ReviewComment> replies = commentService.getReplyComments(top.getReviComCode(), code);
            List<ReviewCommentDTO> repliesDTO = new ArrayList<>();

            for(ReviewComment reply : replies) {
                ReviewCommentDTO dto = ReviewCommentDTO.builder()
                        .reviCode(reply.getReviCode())
                        .reviComCode(reply.getReviComCode())
                        .reviComDesc(reply.getReviComDesc())
                        .reviComDate(reply.getReviComDate())
                        .user(UserDTO.builder()
                                .id(reply.getUser().getId())
                                .name(reply.getUser().getName())
                                .build())
                        .build();
                repliesDTO.add(dto);
            }

            ReviewCommentDTO dto = ReviewCommentDTO.builder()
                    .reviCode(top.getReviCode())
                    .reviComCode(top.getReviComCode())
                    .reviComDesc(top.getReviComDesc())
                    .reviComDate(top.getReviComDate())
                    .user(UserDTO.builder()
                            .id(top.getUser().getId())
                            .name(top.getUser().getName())
                            .build())
                    .replies(repliesDTO)
                    .build();
            response.add(dto);

        }

        return ResponseEntity.ok(response);
    }


    public Object authentication() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        return authentication.getPrincipal();
    }
}
