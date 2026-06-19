package com.share.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.share.common.BusinessException;
import com.share.common.Result;
import com.share.common.ResultCode;
import com.share.entity.Notification;
import com.share.entity.NotificationDetail;
import com.share.service.INotificationDetailService;
import com.share.service.INotificationService;
import com.share.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final INotificationService notificationService;
    private final INotificationDetailService detailService;
    private final IUserService userService;

    private void checkAdmin(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) throw new BusinessException(ResultCode.UNAUTHORIZED);
        String role = userService.getById(userId).getRole();
        if (!"admin".equals(role)) throw new BusinessException(ResultCode.FORBIDDEN);
    }

    @GetMapping
    public Result<Page<Notification>> list(@RequestParam(defaultValue = "1") Integer page,
                                           @RequestParam(defaultValue = "10") Integer pageSize,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) String type,
                                           HttpServletRequest request) {
        checkAdmin(request);
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Notification::getTitle, keyword).or().like(Notification::getContent, keyword);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Notification::getType, type);
        }
        wrapper.orderByDesc(Notification::getCreateTime);
        Page<Notification> pageResult = notificationService.page(new Page<>(page, pageSize), wrapper);
        return Result.success(pageResult);
    }

    @GetMapping("/detail/{id}")
    public Result<NotificationDetail> getDetail(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        Notification notification = notificationService.getById(id);
        if (notification == null || notification.getDetailId() == null) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        NotificationDetail detail = detailService.getById(notification.getDetailId());
        return Result.success(detail);
    }

    @PostMapping
    public Result<Void> add(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        checkAdmin(request);
        return notificationService.addNotification(params);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        checkAdmin(request);
        return notificationService.deleteNotification(id);
    }
}