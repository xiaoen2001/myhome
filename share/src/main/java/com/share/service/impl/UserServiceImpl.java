package com.share.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.share.common.BusinessException;
import com.share.common.Result;
import com.share.common.ResultCode;
import com.share.config.NotificationWebSocketHandler;
import com.share.dto.LoginDTO;
import com.share.dto.RegisterDTO;
import com.share.entity.Friend;
import com.share.entity.Note;
import com.share.entity.User;
import com.share.mapper.UserMapper;
import com.share.service.IFriendService;
import com.share.service.INoteService;
import com.share.service.IUserService;
import com.share.util.*;
import com.share.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private EmailService emailService;
    @Autowired
    private VerifyCodeService verifyCodeService;
    @Autowired
    @Lazy
    private IFriendService friendService;
    @Autowired
    @Lazy
    private INoteService noteService;
    @Autowired
    private NotificationWebSocketHandler notificationWebSocketHandler;
    @Autowired
    private FileUpload fileUpload;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;
    @Value("${server.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public User findByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username).eq(User::getStatus, "active"));
    }

    @Override
    public User findByEmail(String email) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
    }

    @Override
    public Result<List<User>> searchUsers(String keyword) {
        List<User> list = this.list(new LambdaQueryWrapper<User>()
                .like(User::getUsername, keyword)
                .or()
                .like(User::getNickname, keyword)
                .last("limit 10"));
        list.forEach(u -> u.setPassword(null));
        return Result.success(list);
    }

    @Override
    public Result<UserVO> getUserProfile(Long userId) {
        User user = this.getById(userId);
        user.setPassword(null);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        List<Note> list = noteService.list(new LambdaQueryWrapper<Note>().eq(Note::getUserId, user.getId()));
        userVO.setNoteCount(String.valueOf(list.size()));
        int collectionCount = 0;
        int likeCount = 0;
        for (Note note : list) {
            collectionCount += note.getCollectCount();
            likeCount += note.getLikeCount();
        }
        userVO.setCollectionCount(String.valueOf(collectionCount));
        userVO.setLikeCount(String.valueOf(likeCount));
        return Result.success(userVO);
    }

    @Override
    public Result<Void> updateProfile(Long userId, Map<String, String> map) {
        User user = this.getById(userId);
        user.setBio(map.get("brief"));
        user.setEmail(map.get("email"));
        user.setNickname(map.get("nickname"));
        String photo = map.get("photo");
        if (photo != null && photo.startsWith("data:image/")) {
            String old = user.getAvatar();
            if (old != null) {
                String relativePath = old.replace(baseUrl + "/uploads/", "");
                File file = new File(uploadDir, relativePath);
                if (file.exists()) {
                    boolean deleted = file.delete();
                    if (!deleted) {
                        return Result.error(500, "文件删除失败");
                    }
                }
            }
            String image = fileUpload.saveBase64Image(photo, "photos");
            user.setAvatar(image);
        }
        this.updateById(user);
        return Result.success();
    }

    @Override
    public Result<Map<String, Object>> login(LoginDTO loginDTO) {
        User user = this.findByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("nickname", user.getNickname());
        data.put("avatar", user.getAvatar());
        data.put("role", user.getRole());
        data.put("createdAt", user.getCreatedAt());

        List<Friend> list = friendService.list(new LambdaQueryWrapper<Friend>().eq(Friend::getUserId, user.getId()).select(Friend::getFriendId));
        for (Friend friend : list) {
            notificationWebSocketHandler.sendToUser(friend.getFriendId(), "系统", "你的好友 " + user.getNickname() + " 上线了!");
        }
        return Result.success(data);
    }

    @Override
    public Result<Void> register(RegisterDTO registerDTO) {
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setEmail(registerDTO.getEmail());
        String nickname = registerDTO.getNickname();
        user.setNickname(nickname != null && !nickname.isEmpty() ? nickname : registerDTO.getUsername());
        user.setRole("user");
        user.setStatus("active");
        this.save(user);
        return Result.success();
    }

    @Override
    public Result<Void> updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
        return Result.success();
    }

    @Override
    public Result<Void> sendResetCode(String email) {
        if (email == null || email.isEmpty()) {
            return Result.error(400, "邮箱不能为空");
        }
        User user = this.findByEmail(email);
        if (user == null) {
            return Result.error(404, "该邮箱未注册");
        }
        String code = CodeUtil.generateCode(6);
        verifyCodeService.saveCode(email, code);
        String subject = "密码重置验证码";
        String text = "您好，您正在重置读书笔记分享平台的密码。\n验证码：" + code + "\n有效期5分钟，请勿泄露。";
        emailService.sendSimpleMail(email, subject, text);
        return Result.success();
    }

    @Override
    public Result<Void> resetPassword(String email, String code, String newPassword) {
        if (email == null || code == null || newPassword == null) {
            return Result.error(400, "参数不完整");
        }
        if (!verifyCodeService.verifyCode(email, code)) {
            return Result.error(400, "验证码错误或已过期");
        }
        User user = this.findByEmail(email);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        this.updateById(user);
        verifyCodeService.removeCode(email);
        return Result.success();
    }

    @Override
    public Result<List<User>> recommendFriends(Long userId) {
        List<Long> friendIds = friendService.list(new LambdaQueryWrapper<Friend>().eq(Friend::getUserId, userId))
                .stream().map(Friend::getFriendId).toList();
        List<User> list = this.list(new LambdaQueryWrapper<User>()
                .ne(User::getId, userId)
                .notIn(friendIds.size() > 0, User::getId, friendIds)
                .last("limit 10"));
        list.forEach(u -> u.setPassword(null));
        return Result.success(list);
    }
}