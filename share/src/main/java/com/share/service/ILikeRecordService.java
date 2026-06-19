package com.share.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.share.common.Result;
import com.share.entity.LikeRecord;
import com.share.entity.Note;

public interface ILikeRecordService extends IService<LikeRecord> {
    boolean isLiked(Long userId, Long noteId);
    boolean like(Long userId, Long noteId);
    boolean unlike(Long userId, Long noteId);
    Result<Page<Note>> getMyLikes(Long userId, Integer page, Integer pageSize, String keyword);
}