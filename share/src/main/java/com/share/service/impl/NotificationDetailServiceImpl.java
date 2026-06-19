package com.share.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.share.entity.CollectRecord;
import com.share.entity.NotificationDetail;
import com.share.mapper.CollectRecordMapper;
import com.share.mapper.NotificationDetailMapper;
import com.share.service.INotificationDetailService;
import org.springframework.stereotype.Service;


@Service
public class NotificationDetailServiceImpl extends ServiceImpl<NotificationDetailMapper, NotificationDetail> implements INotificationDetailService {
}
