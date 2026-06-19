package com.share.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.share.common.Result;
import com.share.config.NotificationWebSocketHandler;
import com.share.entity.LikeRecord;
import com.share.entity.Note;
import com.share.mapper.LikeRecordMapper;
import com.share.service.ILikeRecordService;
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
public class LikeRecordServiceImpl extends ServiceImpl<LikeRecordMapper, LikeRecord> implements ILikeRecordService {
    @Lazy
    @Autowired
    private INoteService noteService;
    @Autowired
    private NotificationWebSocketHandler notificationWebSocketHandler;

    @Override
    public boolean isLiked(Long userId, Long noteId) {
        return this.count(new LambdaQueryWrapper<LikeRecord>().eq(LikeRecord::getUserId, userId).eq(LikeRecord::getNoteId, noteId)) > 0;
    }

    @Override
    @Transactional
    public boolean like(Long userId, Long noteId) {
        if (isLiked(userId, noteId)) return false;
        LikeRecord record = new LikeRecord();
        record.setUserId(userId);
        record.setNoteId(noteId);
        boolean saved = this.save(record);
        if (saved) {
            noteService.incrementLikeCount(noteId);
            Note note = noteService.getById(noteId);
            if (note != null && note.getUserId() != null && !note.getUserId().equals(userId)) {
                notificationWebSocketHandler.sendToUser(note.getUserId(), "系统", "有人点赞了您的笔记！");
            }
        }
        return saved;
    }

    @Override
    @Transactional
    public boolean unlike(Long userId, Long noteId) {
        boolean removed = this.remove(new LambdaQueryWrapper<LikeRecord>().eq(LikeRecord::getUserId, userId).eq(LikeRecord::getNoteId, noteId));
        if (removed) {
            noteService.decrementLikeCount(noteId);
        }
        return removed;
    }

    @Override
    public Result<Page<Note>> getMyLikes(Long userId, Integer page, Integer pageSize, String keyword) {
        Page<LikeRecord> likePage = this.page(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<LikeRecord>()
                        .eq(LikeRecord::getUserId, userId)
                        .orderByDesc(LikeRecord::getCreatedAt)
        );
        if (likePage.getRecords().isEmpty()) {
            Page<Note> emptyPage = new Page<>(page, pageSize);
            emptyPage.setTotal(0);
            return Result.success(emptyPage);
        }
        List<Long> noteIds = likePage.getRecords().stream()
                .map(LikeRecord::getNoteId)
                .collect(Collectors.toList());
        LambdaQueryWrapper<Note> noteWrapper = new LambdaQueryWrapper<>();
        noteWrapper.in(Note::getId, noteIds)
                .eq(Note::getVisibility, "public")
                .eq(Note::getStatus, "published");
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
        resultPage.setTotal(likePage.getTotal());
        resultPage.setRecords(orderedNotes);
        return Result.success(resultPage);
    }
}