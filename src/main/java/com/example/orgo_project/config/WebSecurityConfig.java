package com.example.orgo_project.config;

import com.example.orgo_project.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(CustomUserDetailsService customUserDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                // Phân quyền cho dashboard của riêng từng Role
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/seller/**").hasRole("SELLER")
                .requestMatchers("/expert/**").hasRole("EXPERT")
                .requestMatchers("/buyer/**").hasRole("BUYER")
                // FIX: Cho phép USER, SELLER, EXPERT vào /user/** (để xem lại form đăng ký)
                .requestMatchers("/user/**").hasAnyRole("ADMIN", "USER", "SELLER", "EXPERT")
                // Các đường dẫn cho phép public
                .requestMatchers("/login", "/register", "/forgot-password", "/verify-otp", "/reset-password", "/guest-login").permitAll()
                .requestMatchers("/", "/welcome", "/search", "/products", "/products/**", "/blog", "/blog/**", "/css/**", "/js/**", "/images/**", "/uploads/**", "/webjars/**").permitAll()
                .requestMatchers("/reviews/add").authenticated()
                // Bất kỳ request nào khác đều bắt buộc đăng nhập
                .anyRequest().authenticated()
        );

        // Cấu hình form login
        http.formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler((request, response, authentication) -> {
                    boolean isAdmin = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_ADMIN"));
                    boolean isSeller = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_SELLER"));
                    boolean isExpert = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_EXPERT"));
                    boolean isBuyer = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_BUYER"));

                    if (isAdmin) {
                        response.sendRedirect("/admin/dashboard");
                    } else if (isSeller) {
                        response.sendRedirect("/seller/dashboard");
                    } else if (isExpert) {
                        response.sendRedirect("/expert/dashboard");
                    } else if (isBuyer) {
                        response.sendRedirect("/buyer/dashboard");
                    } else {
                        response.sendRedirect("/");
                    }
                })
                .permitAll()
        );

        // Cấu hình logout
        http.logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
        );

        // Cấu hình trả về trang 403 khi không có quyền (role) truy cập
        http.exceptionHandling(exception -> exception
                .accessDeniedPage("/403")
        );

        return http.build();
    }

    // SpringSecurityDialect là một thành phần tích hợp giữa Spring Security và Thymeleaf
    @Bean
    public SpringSecurityDialect springSecurityDialect() {
        return new SpringSecurityDialect();
    }
}
