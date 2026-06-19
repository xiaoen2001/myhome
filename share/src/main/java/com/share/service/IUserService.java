package com.share.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.share.common.Result;
import com.share.dto.LoginDTO;
import com.share.dto.RegisterDTO;
import com.share.entity.User;
import com.share.vo.UserVO;

import java.util.List;
import java.util.Map;

public interface IUserService extends IService<User> {
    User findByUsername(String username);
    User findByEmail(String email);
    Result<List<User>> searchUsers(String keyword);

    Result<UserVO> getUserProfile(Long userId);
    Result<Void> updateProfile(Long userId, Map<String, String> params);
    Result<Map<String, Object>> login(LoginDTO loginDTO);
    Result<Void> register(RegisterDTO registerDTO);
    Result<Void> updatePassword(Long userId, String oldPassword, String newPassword);
    Result<Void> sendResetCode(String email);
    Result<Void> resetPassword(String email, String code, String newPassword);
    Result<List<User>> recommendFriends(Long userId);
}