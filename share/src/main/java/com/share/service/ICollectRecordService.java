package com.share.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.share.common.Result;
import com.share.entity.CollectRecord;
import com.share.entity.Note;

public interface ICollectRecordService extends IService<CollectRecord> {
    boolean isCollected(Long userId, Long noteId);
    boolean collect(Long userId, Long noteId);
    boolean uncollect(Long userId, Long noteId);
    Result<Page<Note>> getMyCollects(Long userId, Integer page, Integer pageSize, String keyword);
    Result<?> deleteCollectRecord(Long noteId, Long userId);
}