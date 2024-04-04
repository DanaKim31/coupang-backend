package com.kh.coupang.controller;

import com.kh.coupang.domain.User;
import com.kh.coupang.domain.UserDTO;
import com.kh.coupang.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @PostMapping("/signUp")
    public ResponseEntity create(@RequestBody User vo) {

        User user = User.builder()
                .id(vo.getId())
                .password(passwordEncoder.encode(vo.getPassword()))
                .name(vo.getName())
                .phone(vo.getPhone())
                .email(vo.getEmail())
                .address(vo.getAddress())
                .role("ROLE_USER") // default 값이 지정된 컬럼이라도 'null'로 들어가기 때문에 직접 입력해서 지정
                .build();

        User result = userService.create(user);
        UserDTO responseDTO = UserDTO.builder()
                                        .id(result.getId())
                                        .name(result.getName())
                                        .build();
        return ResponseEntity.ok().body(responseDTO);
    }

    @PostMapping("/login")
    // 비밀번호 노출되지 않게 하기 위해서 GetMapping이 아닌 PostMappgin
    public ResponseEntity login(@RequestBody User vo) {
        return 
    }

}
