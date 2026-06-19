package com.share.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "note", autoResultMap = true)
public class Note {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer categoryId;
    private String title;
    private String bookName;
    private String author;
    private String cover;
    private String content;
    private Integer viewCount;
    private Integer commentCount;
    private Integer likeCount;
    private Integer collectCount;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> tags;
    private String status; // draft, reviewing, published, rejected, offline, banned
    private String visibility; // public, private
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}