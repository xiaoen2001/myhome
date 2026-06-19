package com.share.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.share.common.BusinessException;
import com.share.common.Result;
import com.share.common.ResultCode;
import com.share.config.NotificationWebSocketHandler;
import com.share.entity.Notification;
import com.share.entity.NotificationDetail;
import com.share.mapper.NotificationMapper;
import com.share.service.INotificationDetailService;
import com.share.service.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements INotificationService {

    private final INotificationDetailService detailService;
    private final NotificationWebSocketHandler webSocketHandler;

    @Override
    public Result<Void> addNotification(Map<String, Object> params) {
        String type = (String) params.get("type");
        String title = (String) params.get("title");
        String content = (String) params.get("content");
        @SuppressWarnings("unchecked")
        Map<String, String> detailMap = (Map<String, String>) params.get("detail");
        String detailTitle = detailMap != null ? detailMap.get("title") : null;
        String detailContent = detailMap != null ? detailMap.get("content") : null;
        String author = detailMap != null ? detailMap.get("author") : "管理员";

        if (type == null || title == null || content == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST);
        }

        // 获取目标用户ID：个人通知传 userId，全局通知不传
        Object userIdObj = params.get("userId");
        Long targetUserId = null;
        if (userIdObj instanceof Integer) {
            targetUserId = ((Integer) userIdObj).longValue();
        } else if (userIdObj instanceof Long) {
            targetUserId = (Long) userIdObj;
        }

        NotificationDetail detail = new NotificationDetail();
        detail.setTitle(detailTitle != null && !detailTitle.isEmpty() ? detailTitle : title);
        detail.setContent(detailContent != null ? detailContent : "");
        detail.setAuthor(author);
        detail.setStatus("published");
        detail.setCreateTime(LocalDateTime.now());
        detailService.save(detail);

        Notification notification = new Notification();
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setDetailId(detail.getId());
        notification.setUserId(targetUserId);
        notification.setCreateTime(LocalDateTime.now());
        this.save(notification);

        // 全局系统通知 → 广播给所有在线用户
        if ("system".equals(type) && targetUserId == null) {
            webSocketHandler.broadcast(notification);
        }
        // 个人通知 → 只发给目标用户
        else if (targetUserId != null) {
            webSocketHandler.sendToUser(targetUserId, title, content);
        }
        return Result.success();
    }

    @Override
    public Result<Void> deleteNotification(Long id) {
        Notification notification = this.getById(id);
        if (notification == null) throw new BusinessException(ResultCode.NOT_FOUND);
        if (notification.getDetailId() != null) {
            detailService.removeById(notification.getDetailId());
        }
        this.removeById(id);
        return Result.success();
    }
}