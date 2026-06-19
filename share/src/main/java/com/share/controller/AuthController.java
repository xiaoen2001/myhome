package com.share.controller;

import com.share.common.Result;
import com.share.dto.LoginDTO;
import com.share.dto.RegisterDTO;
import com.share.service.IUserService;
import com.share.util.VerifyCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IUserService userService;
    private final VerifyCodeService verifyCodeService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) {
        return userService.login(loginDTO);
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        return userService.register(registerDTO);
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestAttribute Long userId, @RequestBody Map<String, String> params) {
        return userService.updatePassword(userId, params.get("oldPassword"), params.get("newPassword"));
    }

    @PostMapping("/confirmCode")
    public Result<Boolean> confirmCode(@RequestBody Map<String, String> map) {
        String code = map.get("code");
        String email = map.get("email");
        if (!verifyCodeService.verifyCode(email, code)) {
            return Result.error(400, "验证码错误或已过期");
        }
        return Result.success(true);
    }

    @PostMapping("/reset-code")
    public Result<Void> sendResetCode(@RequestBody Map<String, String> param) {
        return userService.sendResetCode(param.get("email"));
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody Map<String, String> param) {
        return userService.resetPassword(param.get("email"), param.get("code"), param.get("newPassword"));
    }
}