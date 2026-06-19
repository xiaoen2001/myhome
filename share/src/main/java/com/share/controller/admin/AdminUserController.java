package com.share.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.share.common.BusinessException;
import com.share.common.Result;
import com.share.common.ResultCode;
import com.share.entity.User;
import com.share.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;

    private void checkAdmin(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) throw new BusinessException(ResultCode.UNAUTHORIZED);
        String role = userService.getById(userId).getRole();
        if (!"admin".equals(role)) throw new BusinessException(ResultCode.FORBIDDEN);
    }

    @GetMapping
    public Result<Page<User>> listUsers(@RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer pageSize,
                                        @RequestParam(required = false) String keyword,
                                        HttpServletRequest request) {
        checkAdmin(request);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getNickname, keyword)
                    .or()
                    .like(User::getEmail, keyword);
        }
        wrapper.orderByDesc(User::getCreatedAt);
        Page<User> pageResult = userService.page(new Page<>(page, pageSize), wrapper);
        // 隐藏密码
        pageResult.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(pageResult);
    }

    @PutMapping("/{id}")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> params, HttpServletRequest request) {
        checkAdmin(request);
        User user = userService.getById(id);
        if (user == null) throw new BusinessException(ResultCode.USER_NOT_FOUND);
        if (params.containsKey("nickname")) user.setNickname((String) params.get("nickname"));
        if (params.containsKey("email")) user.setEmail((String) params.get("email"));
        if (params.containsKey("role")) user.setRole((String) params.get("role"));
        userService.updateById(user);
        return Result.success();
    }

    @PutMapping("/{id}/reset-password")
    public Result<Void> resetPassword(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        User user = userService.getById(id);
        if (user == null) throw new BusinessException(ResultCode.USER_NOT_FOUND);
        user.setPassword(passwordEncoder.encode("123456"));
        userService.updateById(user);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestBody Map<String, String> params, HttpServletRequest request) {
        checkAdmin(request);
        User user = userService.getById(id);
        if (user == null) throw new BusinessException(ResultCode.USER_NOT_FOUND);
        user.setStatus(params.get("status"));
        userService.updateById(user);
        return Result.success();
    }
}