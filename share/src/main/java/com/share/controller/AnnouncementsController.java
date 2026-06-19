package com.share.controller;

import com.share.common.Result;
import com.share.entity.NotificationDetail;
import com.share.service.INotificationDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/announcements")
public class AnnouncementsController {

    private final INotificationDetailService notificationDetailService;

    @GetMapping("/{id}")
    public Result<?> findById(@PathVariable Long id) {
        NotificationDetail byId = notificationDetailService.getById(id);
        return Result.success(byId);
    }
}
