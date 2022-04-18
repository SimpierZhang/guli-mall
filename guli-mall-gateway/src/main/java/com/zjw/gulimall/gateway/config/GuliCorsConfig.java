package com.zjw.gulimall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static io.netty.handler.codec.http.cookie.CookieHeaderNames.MAX_AGE;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-03 22:45
 * @Modifier:
 */
@Configuration
public class GuliCorsConfig
{
    //网关配置跨域信息
    @Bean
    public CorsWebFilter corsFilter() {
        UrlBasedCorsConfigurationSource corsConfigurationSource = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //允许任意请求头跨域
        corsConfiguration.addAllowedHeader("*");
        //允许任意方法跨域
        corsConfiguration.addAllowedMethod("*");
        //允许任意请求来源跨域
        corsConfiguration.addAllowedOrigin("*");
        //允许携带cookie
        corsConfiguration.setAllowCredentials(true);
        corsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);
        return new CorsWebFilter(corsConfigurationSource);
    }
}
