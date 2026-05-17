package com.example.attendance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security configuration for authentication and authorization rules.
 */
@Configuration
public class SecurityConfig {

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
                        .requestMatchers(
                                "/login",
                                "/login/**",
                                "/register",
                                "/register/**",
                                "/error",
                                "/error/**",
                                "/favicon.ico",
                                "/auth/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**"
                        )
                        .permitAll()
                        .anyRequest()
                        .authenticated()
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
                .build();
    }

    /**
     * Password encoder for secure hashing during registration and login checks.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
