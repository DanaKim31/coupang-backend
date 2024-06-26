package com.kh.coupang.controller;

import com.kh.coupang.domain.*;
import com.kh.coupang.service.CategoryService;
import com.kh.coupang.service.ProductCommentService;
import com.kh.coupang.service.ProductService;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j // console.log, syso와 같은 역할
@RestController
@RequestMapping("/api/*")
@CrossOrigin(origins ={"*"}, maxAge = 6000)
public class ProductController {

    @Autowired
    private ProductService product;

    @Autowired
    private ProductCommentService comment;

    @Autowired
    private CategoryService category;

    @Value("${spring.servlet.multipart.location}")
    private String uploadPath; // D:\\upload

    // 카테고리 가져오기
    @GetMapping("/public/category")
    // 하위 카테고리 리스트가 담겨져 있는 CategoryDTO (not Category)
    public ResponseEntity<List<CategoryDTO>> categoryView() {
        List<Category> topList = category.getTopLevelCategories();
        List<CategoryDTO> response = new ArrayList<>();

        for(Category item : topList) {
            CategoryDTO dto = CategoryDTO.builder()
                    .cateIcon(item.getCateIcon())
                    .cateCode(item.getCateCode())
                    .cateName(item.getCateName())
                    .cateUrl(item.getCateUrl())
                    .subCategories(category.getBottomLevelCategories(item.getCateCode()))
                    .build();
            response.add(dto);
        }
        return ResponseEntity.ok(response);
    }



    // 추가
    @PostMapping("/product")
    public ResponseEntity<Product> create(ProductDTO dto) throws IOException {
        log.info("dto : " + dto);
        log.info("file : " + dto.getFile());

        // 파일 업로드
        String fileName = dto.getFile().getOriginalFilename();
        log.info("fileName : " + fileName);
        // UUID (같은 값이 있을 수 있기 때문에 랜던값
        String uuid = UUID.randomUUID().toString();
        // 폴더 구분자 : /, \, \\로 다를 수 있기 때문에 File.separator로 구분
        String saveName = uploadPath + File.separator + "product" + File.separator + uuid + "_" + fileName;
        Path savePath = Paths.get(saveName);
        dto.getFile().transferTo(savePath); // 파일 업로드가 실제로 일어나고 있음!

        // Product vo 값들 담아서 요청!
        Product vo = new Product();
        vo.setProdName(dto.getProdName());
        vo.setPrice(dto.getPrice());
        vo.setProdPhoto(saveName);

        Category category = new Category();
        category.setCateCode(dto.getCateCode());
        vo.setCategory(category);


        Product result = product.create(vo);
        if (result != null) {
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping("/public/product")
    public ResponseEntity<List<Product>> viewAll(@RequestParam(name="category", required = false) Integer category, @RequestParam(name="page", defaultValue = "1") int page) {
        Sort sort = Sort.by("prodCode").descending();
        Pageable pageable = PageRequest.of(page-1, 10, sort);


        // QueryDSL
        // 1. 가장 먼저 동적 처리하기 위한 Q 도메인 클래스 얻어오기 (generated 폴더 내 Q 클래스들)
        // Q 도메인 클래스를 이용하면 Entity 클래스에 선언된 필드들을 변수로 활용할 수 있음
        QProduct qProduct = QProduct.product;

        // 2. BooleanBuilder : where 문에 들어가는 조건들을 넣어주는 컨테이너
        BooleanBuilder builder = new BooleanBuilder();

        if(category != null) {
            // 3. 원하는 조건은 필드값과 같이 결합해서 생성
            BooleanExpression expression = qProduct.category.cateCode.eq(category);

            // 4. 만들어진 조건은 where 문에 and 나 or 같은 키워드와 결합
            builder.and(expression);
        }

        // 5. BooleanBuilder는 QuerydslPredicateExecutor 인터페이스의 findAll() 사용 -> ProductDAO에 추가
        Page<Product> list = product.viewAll(pageable, builder);


        return //category==null ?
                ResponseEntity.status(HttpStatus.OK).body(list.getContent());
                //ResponseEntity.status(HttpStatus.OK).build();
                //ResponseEntity.status(HttpStatus.OK).body(product.viewCategory(category, pageable).getContent());
    }
    

    // 수정
    @PutMapping("/product")
    public ResponseEntity<Product> update(ProductDTO dto) throws IOException {

        Product vo = new Product();
        vo.setProdCode(dto.getProdCode());
        vo.setProdName(dto.getProdName());
        vo.setPrice(dto.getPrice());

        Category category = new Category();
        category.setCateCode(dto.getCateCode());
        vo.setCategory(category);

        // 기존 데이터를 가져와야 하는 상황!
        Product prev = product.view(dto.getProdCode());

        if(dto.getFile().isEmpty()) {
            // 수정할 사진이 없는 경우 : 기존 사진 경로 그대로 vo로 담아냄
            vo.setProdPhoto(prev.getProdPhoto());
        } else {
            // 사진을 수정할 경우 : 기존 사진은 삭제하고, 새로운 사진을 추가
            // 기존 사진은 삭제
            File file = new File(prev.getProdPhoto());
            file.delete();

            // 새로운 사진을 추가
            String fileName = dto.getFile().getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            String saveName = uploadPath + File.separator + "product" + File.separator + uuid + "_" + fileName;
            Path savePath = Paths.get(saveName);
            dto.getFile().transferTo(savePath); // 파일 업로드가 실제로 일어나고 있음!

            vo.setProdPhoto(saveName);
        }

        Product result = product.update(vo);
        // 삼항연산자 활용
        return (result != null) ?
                ResponseEntity.status(HttpStatus.ACCEPTED).body(result) :
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    // 삭제
    @DeleteMapping("/product/{code}")
    public ResponseEntity<Product> delete(@PathVariable(name="code") int code) {
        // 파일 삭제 로직
        Product prev = product.view(code); // 선택한 코드의 상품정보 불러오기
        File file = new File(prev.getProdPhoto()); // 불러온 상품의 사진(prodPhoto)을 file 변수로 담기
        file.delete(); // 변수로 담은 상품의 사진 삭제

        // 상품 삭제
        Product result = product.delete(code); // 상품 목록에서 선택한 상품 삭제 (위 코드 없이 해당 코드만 실행 시 업로드 폴더 내 사진 파일은 삭제되지 않음)
        return (result != null) ?
                ResponseEntity.status(HttpStatus.ACCEPTED).body(result) :
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    // 상품 1개 조회
    @GetMapping("/public/product/{code}")
    public ResponseEntity<Product> view(@PathVariable(name = "code") int code) {
        Product vo = product.view(code);
        return ResponseEntity.status(HttpStatus.OK).body(vo);
    }


    // 상품 댓글 추가
    @PostMapping("/product/comment")
    public ResponseEntity createComment(@RequestBody ProductComment vo) {

        // 시큐리티에 담은 로그인한 사용자의 정보 가져오기
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        Object principal = authentication.getPrincipal();

        if(principal instanceof User) {
            User user = (User) principal;
            vo.setUser(user);
            return ResponseEntity.ok(comment.create(vo));
        }

        return ResponseEntity.badRequest().build();
    }


    // 상품 1개에 따른 댓글 조회  ->  비회원에게도 공개 : /public/ 추가
    @GetMapping("/public/product/{code}/comment")
    public ResponseEntity<List<ProductCommentDTO>> viewComment(@PathVariable(name="code") int code) {
        List<ProductComment> topList = comment.getTopLevelComments(code);
        List<ProductCommentDTO> response = commentDetailList(topList, code);

        return ResponseEntity.ok(response);
    }


    public List<ProductCommentDTO> commentDetailList(List<ProductComment> comments, int code) {
        List<ProductCommentDTO> response = new ArrayList<>();

        for(ProductComment item : comments) {
            List<ProductComment> replies = comment.getRepliesComment(item.getProComCode(), code);
            List<ProductCommentDTO> repliesDTO = commentDetailList(replies, code);
            ProductCommentDTO dto = commentDetail(item);
            dto.setReplies(repliesDTO);
            response.add(dto);
        }

        return response;
    }


    public ProductCommentDTO commentDetail(ProductComment vo) {
        return ProductCommentDTO.builder()
                .prodCode(vo.getProdCode())
                .proComCode(vo.getProComCode())
                .proComDesc(vo.getProComDesc())
                .proComDate(vo.getProComDate())
                .user(UserDTO.builder()
                        .id(vo.getUser().getId())
                        .name(vo.getUser().getName())
                        .build())
                .build();
    }

}