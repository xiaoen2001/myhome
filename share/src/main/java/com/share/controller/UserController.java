package com.share.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.share.common.Result;
import com.share.entity.Note;
import com.share.entity.User;
import com.share.service.INoteService;
import com.share.service.IUserService;
import com.share.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    private final INoteService noteService;

    @GetMapping("/profile/{userId}")
    public Result<UserVO> getProfile(@PathVariable Long userId) {
        return userService.getUserProfile(userId);
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestAttribute Long userId, @RequestBody Map<String, String> map) {
        return userService.updateProfile(userId, map);
    }

    @GetMapping("/{userId}/profile")
    public Result<User> getUserProfile(@PathVariable Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    @GetMapping("/notes/{userId}")
    public Result<List<Note>> getNotes(@PathVariable Long userId) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId);
        List<Note> notes = noteService.list(wrapper);
        return Result.success(notes);
    }
}