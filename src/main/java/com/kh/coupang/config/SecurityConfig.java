package com.kh.coupang.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    // 특정 HTTP 요청에 대한 웹 기반 보안 구성. 인증/인가 및 로그아웃 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 원하는 기능들을 .으로 이어서 작성
        return http
                // security에서 기본적으로 제공하는 기능 disable 처리
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session -> 
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // 요청들마다 권한 처리(인증된 사람만 허용), 슬래쉬 뒤에 오는 것만 권한 허용
                .authorizeHttpRequests(authorize ->
                    authorize
                        // 해당하는 경로 지정해서 허용
                        .requestMatchers("/signUp").permitAll()
                        // 지정한 경로 외 것들 처리
                        .anyRequest().authenticated()
                )
                .build();
    }

}
