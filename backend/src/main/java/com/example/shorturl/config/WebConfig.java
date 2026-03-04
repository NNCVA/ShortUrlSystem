package com.example.shorturl.config;

import com.example.shorturl.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.CorsFilter;

/**
 * Web 配置（注册字符编码过滤器、CORS 过滤器和 JWT 过滤器）
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * CORS 过滤器 - 必须在最前面
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        FilterRegistrationBean<CorsFilter> registrationBean = new FilterRegistrationBean<>();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://localhost:5174");
        config.addAllowedOrigin("http://localhost:5175");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        registrationBean.setFilter(new CorsFilter(source));
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(0); // 最高优先级
        return registrationBean;
    }

    /**
     * 字符编码过滤器
     */
    @Bean
    public FilterRegistrationBean<CharacterEncodingFilter> encodingFilter() {
        FilterRegistrationBean<CharacterEncodingFilter> registrationBean = new FilterRegistrationBean<>();
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        registrationBean.setFilter(filter);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilter() {
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(jwtAuthenticationFilter);
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(2);
        return registrationBean;
    }
}
