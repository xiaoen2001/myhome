package com.share.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("friend")
public class Friend {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long friendId;
    private String friendNickname;
    private String friendAvatar;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}