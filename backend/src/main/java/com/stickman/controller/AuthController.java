package com.stickman.controller;

import com.stickman.common.Result;
import com.stickman.common.UserContext;
import com.stickman.dto.LoginRequest;
import com.stickman.dto.RegisterRequest;
import com.stickman.entity.SysUser;
import com.stickman.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

/**
 * 认证接口
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest req, HttpServletRequest request) {
        return Result.success(authService.login(req, request.getRemoteAddr()));
    }

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody RegisterRequest req) {
        return Result.success(authService.register(req));
    }

    @GetMapping("/me")
    public Result<SysUser> me() {
        return Result.success(authService.currentUser());
    }
}
