package com.asset.asset_backend.common.config;

import com.asset.asset_backend.domains.auth.jwt.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

/**
 * 앱 전체 Spring Security 설정
 * - JWT 기반 stateless 인증
 * - HttpOnly 쿠키로 토큰 관리
 * - CORS 설정 (WebMvcConfigurer 대신 여기서 관리 — Security 필터가 먼저 실행되기 때문)
 */
@Configuration
@EnableWebSecurity // Spring Security 필터 체인 활성화
@RequiredArgsConstructor
public class SecurityConfig {

    // 모든 요청에서 JWT 토큰을 검증하는 커스텀 필터
    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // JWT 방식에서는 CSRF 토큰 불필요 (stateless이므로)
                .csrf(AbstractHttpConfigurer::disable)

                // Spring 기본 로그인 폼 사용 안 함 (자체 /api/auth/login 사용)
                .formLogin(AbstractHttpConfigurer::disable)

                // HTTP Basic 인증 사용 안 함 (Authorization 헤더 방식)
                .httpBasic(AbstractHttpConfigurer::disable)

                // CORS 설정을 아래 corsConfigurationSource() Bean에서 읽어옴
                // WebMvcConfigurer 방식은 Security 필터에서 무시되므로 여기서 직접 등록
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 세션 사용 안 함 — JWT로 인증하므로 서버에 세션 저장 불필요
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        // 회원가입, 로그인, 토큰 갱신은 인증 없이 접근 허용
                        .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/refresh").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // 나머지 모든 요청은 JWT 인증 필요
                        .anyRequest().authenticated()
                )

                // 인증 실패(401) 시 기본 Spring Security 응답 대신 JSON으로 반환
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, e) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write(
                                    "{\"success\":false,\"status\":401,\"message\":\"인증이 필요합니다\"}"
                            );
                        })
                )

                // JwtAuthFilter를 UsernamePasswordAuthenticationFilter 앞에 등록
                // 요청마다 쿠키에서 access_token 꺼내서 검증 후 SecurityContext에 저장
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS 설정
     * credentials: 'include' 사용 시 allowedOrigins에 와일드카드(*) 사용 불가
     * → 허용할 도메인 명시적으로 등록
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 허용할 프론트엔드 도메인 (로컬 + 운영)
        config.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "https://www.yoojoo-asset-management.xyz",
                "https://yoojoo-asset-management.xyz",
                "https://asset-frontend-dusky.vercel.app"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));

        // HttpOnly 쿠키 전송을 위해 필수 (credentials: 'include'와 대응)
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * 비밀번호 암호화 인코더
     * BCrypt 알고리즘 사용 — 단방향 해시, 복호화 불가
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}