package com.example.attendance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;

/**
 * Security configuration for authentication and authorization rules.
 */
@Configuration
public class SecurityConfig {

    /**
     * Configure global security rules. For now, keep all endpoints accessible while wiring auth.
     */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        AccessDeniedHandlerImpl handler = new AccessDeniedHandlerImpl();
        handler.setErrorPage("/403");
        return handler;
    }

    /**
     * Configure global security rules. For now, keep all endpoints accessible while wiring auth.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // API-first setup: disable CSRF for non-browser clients like Postman.
                .csrf(csrf -> csrf.disable())
                // 权限控制配置：放行登录/注册页与静态资源，其余需认证。
                .authorizeHttpRequests(auth -> auth
                        // ===== 公开路径 =====
                        .requestMatchers(
                                "/login", "/login/**",
                                "/register", "/register/**",
                                "/error", "/error/**",
                                "/favicon.ico",
                                "/auth/**",
                                "/css/**", "/js/**", "/images/**",
                                "/webjars/**"
                        ).permitAll()

                        // ===== 管理员专属 =====
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/user/**").hasRole("ADMIN")

                        // ===== 学生 + 管理员 =====
                        .requestMatchers("/student/dashboard").hasAnyRole("STUDENT", "ADMIN")
                        .requestMatchers("/attendance/checkIn/**").hasAnyRole("STUDENT", "ADMIN")
                        .requestMatchers("/leave/apply/**").hasAnyRole("STUDENT", "ADMIN")

                        // ===== 教师 + 管理员 =====
                        .requestMatchers("/teacher/**").hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers("/course/**").hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers("/attendance/export/**").hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers("/attendance/import/**").hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers("/leave/approve/**").hasAnyRole("TEACHER", "ADMIN")

                        // 学生管理页面（教师+管理员）— 必须在 /student/dashboard 之后
                        .requestMatchers("/student/**").hasAnyRole("TEACHER", "ADMIN")
                        // REST API：写操作限制（教师+管理员）
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/add", "/students").hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/students/**").hasAnyRole("TEACHER", "ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/students/**").hasAnyRole("TEACHER", "ADMIN")

                        // ===== 所有角色 =====
                        .requestMatchers("/attendance/list/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                        .requestMatchers("/leave/list/**").hasAnyRole("STUDENT", "TEACHER", "ADMIN")
                        .requestMatchers("/dashboard").authenticated()

                        // ===== 其余需登录 =====
                        .anyRequest().authenticated()
                )
                // 表单登录配置：使用自定义页面，并在登录成功后跳转到 dashboard。
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login") // 这里是处理登录请求的地址
                        .failureUrl("/login?error")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                // 保留 Basic 方便用 Postman 直接测试。
                .httpBasic(Customizer.withDefaults())
                .exceptionHandling(eh -> eh.accessDeniedHandler(accessDeniedHandler()))
                .build();
    }

    /**
     * Password encoder for secure hashing during registration and login checks.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new LegacyAwarePasswordEncoder();
    }

    /**
     * 兼容历史明文密码的数据迁移编码器：
     * - 新写入仍使用 BCrypt
     * - 旧数据若是明文，则允许直接登录
     */
    static class LegacyAwarePasswordEncoder implements PasswordEncoder {
        private final BCryptPasswordEncoder delegate = new BCryptPasswordEncoder();

        @Override
        public String encode(CharSequence rawPassword) {
            return delegate.encode(rawPassword);
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            if (encodedPassword == null || rawPassword == null) {
                return false;
            }

            if (isBcryptHash(encodedPassword)) {
                return delegate.matches(rawPassword, encodedPassword);
            }

            return encodedPassword.contentEquals(rawPassword);
        }

        private boolean isBcryptHash(String value) {
            return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
        }
    }
}
