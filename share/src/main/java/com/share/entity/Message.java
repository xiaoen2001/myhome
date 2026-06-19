package com.share.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String type; // text, image
    private String content;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}