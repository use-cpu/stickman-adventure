package com.stickman.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.stickman.common.BusinessException;
import com.stickman.common.Constants;
import com.stickman.common.JwtUtil;
import com.stickman.common.UserContext;
import com.stickman.dto.LoginRequest;
import com.stickman.dto.RegisterRequest;
import com.stickman.entity.SysUser;
import com.stickman.entity.UserLog;
import com.stickman.mapper.SysUserMapper;
import com.stickman.mapper.UserLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务: 登录、注册
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;
    private final UserLogMapper userLogMapper;
    private final JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 登录
     */
    public Map<String, Object> login(LoginRequest req, String ip) {
        SysUser user = userMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, req.getUsername()));
        if (user == null) {
            throw BusinessException.of("用户不存在");
        }
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw BusinessException.of("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw BusinessException.of("账号已被禁用,请联系管理员");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        recordLog(user.getId(), "LOGIN", "登录成功", ip);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("role", user.getRole());
        userInfo.put("avatar", user.getAvatar());
        result.put("user", userInfo);
        return result;
    }

    /**
     * 注册
     */
    public Map<String, Object> register(RegisterRequest req) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, req.getUsername()));
        if (count > 0) {
            throw BusinessException.of("用户名已被占用");
        }

        SysUser user = new SysUser();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setNickname(req.getNickname() == null ? req.getUsername() : req.getNickname());
        user.setRole(Constants.ROLE_PLAYER);
        user.setStatus(1);
        userMapper.insert(user);

        Map<String, Object> result = new HashMap<>();
        result.put("id", user.getId());
        result.put("username", user.getUsername());
        result.put("message", "注册成功");
        return result;
    }

    /**
     * 获取当前登录用户信息
     */
    public SysUser currentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw BusinessException.unauthorized("未登录");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.unauthorized("用户不存在");
        }
        // 不返回密码,清空敏感字段
        user.setPassword(null);
        return user;
    }

    private void recordLog(Long userId, String action, String detail, String ip) {
        UserLog userLog = new UserLog();
        userLog.setUserId(userId);
        userLog.setAction(action);
        userLog.setDetail(detail);
        userLog.setIp(ip);
        userLogMapper.insert(userLog);
    }
}
