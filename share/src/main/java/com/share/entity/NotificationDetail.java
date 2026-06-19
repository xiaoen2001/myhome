package com.share.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notification_detail")
public class NotificationDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String content;
    private String author;
    private String status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}