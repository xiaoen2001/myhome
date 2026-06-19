package com.share.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.share.common.Result;
import com.share.entity.Notification;

import java.util.Map;

public interface INotificationService extends IService<Notification> {
    Result<Void> addNotification(Map<String, Object> params);
    Result<Void> deleteNotification(Long id);
}