package com.fourspicehotpot.backend.common.config;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NgrokConfig {

    @Bean
    public FilterRegistrationBean<Filter> ngrokBypassFilter() {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();

        registrationBean.setFilter((request, response, chain) -> {
            HttpServletResponse res = (HttpServletResponse) response;
            res.setHeader("ngrok-skip-browser-warning", "true");
            chain.doFilter(request, response);
        });

        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}
