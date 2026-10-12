package com.fourspicehotpot.backend.common.security.filter;


import com.fourspicehotpot.backend.common.security.annotation.RequirePermission;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
public class PermissionInterceptor implements HandlerInterceptor {
    private static final Logger logger =
            LoggerFactory.getLogger(PermissionInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequirePermission requirePermission =
                handlerMethod.getMethodAnnotation(RequirePermission.class);

        if (requirePermission == null) {
            return true;
        }
        String requiredPermission = requirePermission.value();
        List<String> permissions =
                (List<String>) request.getAttribute("permissions");
        if (permissions == null || !permissions.contains(requiredPermission)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }


        return true;
    }

}
