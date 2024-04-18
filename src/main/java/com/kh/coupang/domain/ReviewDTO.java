package com.kh.coupang.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDTO {

    private int reviCode; // 리뷰 수정 시 필요
    private String id;
    private int prodCode;
    private String reviTitle;
    private String reviDesc;
    private int rating;
    private List<MultipartFile> files; // 새로운 리뷰 등록 시
    private List<String> images; // 리뷰 수정 시

}
