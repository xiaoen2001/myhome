package com.share.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.share.entity.NotificationDetail;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.transaction.annotation.Transactional;


@Transactional
@Mapper
public interface NotificationDetailMapper extends BaseMapper<NotificationDetail> {
}
