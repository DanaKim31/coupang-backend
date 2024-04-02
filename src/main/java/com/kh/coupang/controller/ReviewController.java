package com.kh.coupang.controller;

import com.kh.coupang.domain.Product;
import com.kh.coupang.domain.Review;
import com.kh.coupang.domain.ReviewDTO;
import com.kh.coupang.domain.ReviewImage;
import com.kh.coupang.service.ReviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/*")
public class ReviewController {

    @Autowired
    private ReviewService service;

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
    public ResponseEntity<List<Review>> viewAll() {
        return ResponseEntity.status(HttpStatus.OK).body(service.viewAll());
    }

}
