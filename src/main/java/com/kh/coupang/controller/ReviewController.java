package com.kh.coupang.controller;

import com.kh.coupang.domain.*;
import com.kh.coupang.service.ReviewCommentService;
import com.kh.coupang.service.ReviewService;
import com.querydsl.core.BooleanBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
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
import org.springframework.stereotype.Controller;
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
public class ReviewController {

    @Autowired
    private ReviewService service;

    @Autowired
    private ReviewCommentService commentService;

    @Value("${spring.servlet.multipart.location}")
    private String uploadPath;

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

        // review_image에는 revi_code가 필요!
        for(MultipartFile file : dto.getFiles()){
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

        return result != null ?
            ResponseEntity.status(HttpStatus.CREATED).body(result) :
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }


    @GetMapping("/review")
    public ResponseEntity<List<Review>> viewAll(@RequestParam(name="page", defaultValue = "1")int page) {
        Sort sort = Sort.by("reviCode").descending();
        Pageable paging = PageRequest.of(page-1, 5, sort);

        Page<Review> list = service.viewAll(paging);

        return ResponseEntity.status(HttpStatus.OK).body(list.getContent());
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
