package com.share.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.share.common.Result;
import com.share.common.ResultCode;
import com.share.entity.History;
import com.share.entity.Note;
import com.share.mapper.HistoryMapper;
import com.share.service.IHistoryService;
import com.share.service.INoteService;
import com.share.vo.HistoryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class HistoryServiceImpl extends ServiceImpl<HistoryMapper, History> implements IHistoryService {

    @Autowired
    @Lazy
    private INoteService noteService;

    @Override
    public Result<?> deleteHistorys(Long userId) {
        LambdaQueryWrapper<History> historyWrapper = new LambdaQueryWrapper<>();
        historyWrapper.eq(History::getUserId, userId);
        List<History> list = this.list(historyWrapper);
        if (list.isEmpty()) {
            return Result.error(ResultCode.FORBIDDEN);
        }
        List<Long> Ids = list.stream().map(History::getId).toList();
        boolean remove = this.removeByIds(Ids);
        if (!remove) {
            return Result.error(ResultCode.FORBIDDEN);
        }
        return Result.success();
    }

    @Override
    public Result<?> deleteHistory(Long id, Long userId) {
        LambdaQueryWrapper<History> historyWrapper = new LambdaQueryWrapper<>();
        historyWrapper.eq(History::getNoteId, id);
        historyWrapper.eq(History::getUserId, userId);
        boolean remove = this.remove(historyWrapper);
        if (!remove) {
            return Result.error(ResultCode.FORBIDDEN);
        }
        return Result.success();
    }

    @Override
    public boolean saveHistory(Long userId, Long noteId) {
        boolean exists = this.exists(new LambdaQueryWrapper<History>().eq(History::getUserId, userId).eq(History::getNoteId, noteId));
        if (exists) {
            return true;
        }
        History history = new History();
        history.setUserId(userId);
        history.setNoteId(noteId);
        history.setBrowsedAt(LocalDateTime.now());
        return this.saveOrUpdate(history);
    }

    @Override
    public Result<Page<HistoryVO>> getMyHistory(Long userId, Integer page, Integer pageSize, String keyword) {
        Page<History> historyPage = this.page(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<History>()
                        .eq(History::getUserId, userId)
                        .orderByDesc(History::getBrowsedAt)
        );
        if (historyPage.getRecords().isEmpty()) {
            Page<HistoryVO> emptyPage = new Page<>(page, pageSize);
            emptyPage.setTotal(0);
            return Result.success(emptyPage);
        }
        List<History> validRecords = historyPage.getRecords().stream()
                .filter(h -> h.getNoteId() != null && h.getBrowsedAt() != null)
                .collect(Collectors.toList());
        if (validRecords.isEmpty()) {
            Page<HistoryVO> emptyPage = new Page<>(page, pageSize);
            emptyPage.setTotal(0);
            return Result.success(emptyPage);
        }
        Map<Long, LocalDateTime> browsedAtMap = validRecords.stream()
                .collect(Collectors.toMap(History::getNoteId, History::getBrowsedAt, (v1, v2) -> v2));
        List<Long> noteIds = validRecords.stream().map(History::getNoteId).collect(Collectors.toList());

        LambdaQueryWrapper<Note> noteWrapper = new LambdaQueryWrapper<>();
        noteWrapper.in(Note::getId, noteIds)
                .eq(Note::getVisibility, "public")
                .eq(Note::getStatus, "published");
        if (keyword != null && !keyword.isEmpty()) {
            noteWrapper.and(w -> w.like(Note::getTitle, keyword).or().like(Note::getBookName, keyword));
        }
        List<Note> notes = noteService.list(noteWrapper);
        Map<Long, Note> noteMap = notes.stream().collect(Collectors.toMap(Note::getId, Function.identity()));
        List<HistoryVO> voList = noteIds.stream()
                .map(noteId -> {
                    Note note = noteMap.get(noteId);
                    if (note == null) return null;
                    HistoryVO vo = new HistoryVO();
                    BeanUtils.copyProperties(note, vo);
                    vo.setBrowsedAt(browsedAtMap.get(noteId));
                    return vo;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Page<HistoryVO> resultPage = new Page<>(page, pageSize);
        resultPage.setTotal(historyPage.getTotal());
        resultPage.setRecords(voList);
        return Result.success(resultPage);
    }
}