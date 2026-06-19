package com.share.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("history")
public class History {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long noteId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime browsedAt;
}