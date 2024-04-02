package com.kh.coupang.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert // 기본키
@Table(name="review_image") // 테이블명에 언더 스코어 있을 경우 추가!
public class ReviewImage {

    @Id
    @Column(name="revi_img_code")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int reviImgCode;

    @Column(name="revi_url")
    private String reviUrl;

    @ManyToOne
    @JoinColumn(name="revi_code") // 조인
    @JsonIgnore
    private Review review;

}
