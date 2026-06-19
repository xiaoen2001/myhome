package com.share.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.share.common.Result;
import com.share.common.ResultCode;
import com.share.config.NotificationWebSocketHandler;
import com.share.entity.CollectRecord;
import com.share.entity.Note;
import com.share.mapper.CollectRecordMapper;
import com.share.service.ICollectRecordService;
import com.share.service.INoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CollectRecordServiceImpl extends ServiceImpl<CollectRecordMapper, CollectRecord> implements ICollectRecordService {
    @Lazy
    @Autowired
    private INoteService noteService;
    @Autowired
    private NotificationWebSocketHandler notificationWebSocketHandler;

    @Override
    public boolean isCollected(Long userId, Long noteId) {
        return this.count(new LambdaQueryWrapper<CollectRecord>().eq(CollectRecord::getUserId, userId).eq(CollectRecord::getNoteId, noteId)) > 0;
    }

    @Override
    @Transactional
    public boolean collect(Long userId, Long noteId) {
        if (isCollected(userId, noteId)) return false;
        CollectRecord record = new CollectRecord();
        record.setUserId(userId);
        record.setNoteId(noteId);
        boolean saved = this.save(record);
        if (saved) {
            noteService.incrementCollectCount(noteId);
            Note note = noteService.getById(noteId);
            if (note != null && note.getUserId() != null && !note.getUserId().equals(userId)) {
                notificationWebSocketHandler.sendToUser(note.getUserId(), "系统", "有人收藏了您的笔记！");
            }
        }
        return saved;
    }

    @Override
    @Transactional
    public boolean uncollect(Long userId, Long noteId) {
        boolean removed = this.remove(new LambdaQueryWrapper<CollectRecord>().eq(CollectRecord::getUserId, userId).eq(CollectRecord::getNoteId, noteId));
        if (removed) {
            noteService.decrementCollectCount(noteId);
        }
        return removed;
    }

    @Override
    public Result<Page<Note>> getMyCollects(Long userId, Integer page, Integer pageSize, String keyword) {
        Page<CollectRecord> collectPage = this.page(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<CollectRecord>()
                        .eq(CollectRecord::getUserId, userId)
                        .orderByDesc(CollectRecord::getCreatedAt)
        );
        if (collectPage.getRecords().isEmpty()) {
            Page<Note> emptyPage = new Page<>(page, pageSize);
            emptyPage.setTotal(0);
            return Result.success(emptyPage);
        }
        List<Long> noteIds = collectPage.getRecords().stream()
                .map(CollectRecord::getNoteId)
                .collect(Collectors.toList());
        LambdaQueryWrapper<Note> noteWrapper = new LambdaQueryWrapper<>();
        noteWrapper.in(Note::getId, noteIds)
                .eq(Note::getVisibility, "public");
        if (keyword != null && !keyword.isEmpty()) {
            noteWrapper.and(w -> w.like(Note::getTitle, keyword).or().like(Note::getBookName, keyword));
        }
        List<Note> notes = noteService.list(noteWrapper);
        Map<Long, Note> noteMap = notes.stream().collect(Collectors.toMap(Note::getId, Function.identity()));
        List<Note> orderedNotes = noteIds.stream()
                .map(noteMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        Page<Note> resultPage = new Page<>(page, pageSize);
        resultPage.setTotal(collectPage.getTotal());
        resultPage.setRecords(orderedNotes);
        return Result.success(resultPage);
    }

    @Override
    public Result<?> deleteCollectRecord(Long noteId, Long userId) {
        LambdaQueryWrapper<CollectRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectRecord::getNoteId, noteId);
        wrapper.eq(CollectRecord::getUserId, userId);
        boolean remove = this.remove(wrapper);
        if (!remove) {
            return Result.error(ResultCode.FORBIDDEN);
        }
        return Result.success();
    }
}