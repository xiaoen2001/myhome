package com.share.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.share.common.Result;
import com.share.entity.Comment;
import com.share.entity.Note;
import com.share.entity.User;
import com.share.service.ICommentService;
import com.share.service.INoteService;
import com.share.service.IStatsService;
import com.share.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements IStatsService {

    private final IUserService userService;
    private final INoteService noteService;
    private final ICommentService commentService;

    @Override
    public Result<Map<String, Object>> getDashboardStats() {
        long userCount = userService.count();
        long noteCount = noteService.count();
        long commentCount = commentService.count();
        long viewCount = noteService.list().stream().mapToLong(Note::getViewCount).sum();
        Map<String, Object> stats = new HashMap<>();
        stats.put("userCount", userCount);
        stats.put("noteCount", noteCount);
        stats.put("commentCount", commentCount);
        stats.put("viewCount", viewCount);
        return Result.success(stats);
    }

    @Override
    public Result<Map<String, Object>> getCommentTrend() {
        List<String> months = new ArrayList<>();
        List<Long> data = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 5; i >= 0; i--) {
            LocalDateTime start = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime end = start.plusMonths(1).minusSeconds(1);
            long count = commentService.count(new LambdaQueryWrapper<Comment>()
                    .between(Comment::getCreatedAt, start, end));
            months.add(start.format(DateTimeFormatter.ofPattern("M月")));
            data.add(count);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("months", months);
        result.put("data", data);
        return Result.success(result);
    }

    @Override
    public Result<Map<String, Object>> getViewTrend() {
        List<String> months = new ArrayList<>();
        List<Long> data = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 5; i >= 0; i--) {
            LocalDateTime start = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime end = start.plusMonths(1).minusSeconds(1);
            long views = noteService.list(new LambdaQueryWrapper<Note>()
                            .between(Note::getCreatedAt, start, end))
                    .stream().mapToLong(Note::getViewCount).sum();
            months.add(start.format(DateTimeFormatter.ofPattern("M月")));
            data.add(views);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("months", months);
        result.put("data", data);
        return Result.success(result);
    }

    @Override
    public Result<Map<String, Object>> getUserTrend() {
        List<String> months = new ArrayList<>();
        List<Long> data = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 11; i >= 0; i--) {
            LocalDateTime start = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime end = start.plusMonths(1).minusSeconds(1);
            long count = userService.count(new LambdaQueryWrapper<User>()
                    .between(User::getCreatedAt, start, end));
            months.add(start.getMonthValue() + "月");
            data.add(count);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("months", months);
        result.put("data", data);
        return Result.success(result);
    }

    @Override
    public Result<Map<String, Object>> getNoteTrend() {
        List<String> months = new ArrayList<>();
        List<Long> data = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 11; i >= 0; i--) {
            LocalDateTime start = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime end = start.plusMonths(1).minusSeconds(1);
            long count = noteService.count(new LambdaQueryWrapper<Note>()
                    .between(Note::getCreatedAt, start, end));
            months.add(start.getMonthValue() + "月");
            data.add(count);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("months", months);
        result.put("data", data);
        return Result.success(result);
    }
}