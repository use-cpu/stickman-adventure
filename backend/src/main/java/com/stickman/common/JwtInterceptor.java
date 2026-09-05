package com.stickman.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT 拦截器 - 校验登录状态
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeUnauthorized(response, "未登录或token缺失");
            return false;
        }

        String token = authHeader.substring(7);
        if (jwtUtil.isExpired(token)) {
            writeUnauthorized(response, "token已过期,请重新登录");
            return false;
        }

        Long userId = jwtUtil.getUserId(token);
        String username = jwtUtil.getUsername(token);
        String role = jwtUtil.getRole(token);
        if (userId == null) {
            writeUnauthorized(response, "token无效");
            return false;
        }

        UserContext.set(userId, username, role);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    private void writeUnauthorized(HttpServletResponse response, String msg) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"" + msg + "\"}");
    }
}
