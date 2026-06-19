package com.share.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.share.common.Result;
import com.share.entity.Notification;
import com.share.entity.NotificationDetail;
import com.share.service.INotificationDetailService;
import com.share.service.INotificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final INotificationService notificationService;
    private final INotificationDetailService detailService;

    @GetMapping
    public Result<Page<Notification>> getNotifications(@RequestParam(defaultValue = "1") Integer page,
                                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                                       HttpServletRequest request) {
        // 从 JWT 中获取当前用户ID
        Long userId = (Long) request.getAttribute("userId");

        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        // 查询条件：(type = 'system' AND user_id IS NULL) → 全局通知，所有人可见
        //          OR user_id = 当前用户ID → 个人通知
        wrapper.and(w -> w
                .and(sw -> sw.eq(Notification::getType, "system").isNull(Notification::getUserId))
                .or()
                .eq(Notification::getUserId, userId)
        );
        wrapper.orderByDesc(Notification::getCreateTime);
        Page<Notification> result = notificationService.page(new Page<>(page, pageSize), wrapper);
        return Result.success(result);
    }

    @GetMapping("/detail/{id}")
    public Result<NotificationDetail> getDetail(@PathVariable Long id) {
        Notification notification = notificationService.getById(id);
        if (notification == null || notification.getDetailId() == null) {
            return Result.error(404, "详情不存在");
        }
        NotificationDetail detail = detailService.getById(notification.getDetailId());
        return Result.success(detail);
    }
}