package com.share.controller.admin;

import com.share.common.BusinessException;
import com.share.common.Result;
import com.share.common.ResultCode;
import com.share.service.IStatsService;
import com.share.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminStatsController {

    private final IUserService userService;
    private final IStatsService statsService;

    private void checkAdmin(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) throw new BusinessException(ResultCode.UNAUTHORIZED);
        String role = userService.getById(userId).getRole();
        if (!"admin".equals(role)) throw new BusinessException(ResultCode.FORBIDDEN);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats(HttpServletRequest request) {
        checkAdmin(request);
        return statsService.getDashboardStats();
    }

    @GetMapping("/trend/comment")
    public Result<Map<String, Object>> getCommentTrend(HttpServletRequest request) {
        checkAdmin(request);
        return statsService.getCommentTrend();
    }

    @GetMapping("/trend/view")
    public Result<Map<String, Object>> getViewTrend(HttpServletRequest request) {
        checkAdmin(request);
        return statsService.getViewTrend();
    }

    @GetMapping("/trend/user")
    public Result<Map<String, Object>> getUserTrend(HttpServletRequest request) {
        checkAdmin(request);
        return statsService.getUserTrend();
    }

    @GetMapping("/trend/note")
    public Result<Map<String, Object>> getNoteTrend(HttpServletRequest request) {
        checkAdmin(request);
        return statsService.getNoteTrend();
    }
}