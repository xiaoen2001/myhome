package com.share.service;

import com.share.common.Result;

import java.util.Map;

public interface IStatsService {
    Result<Map<String, Object>> getDashboardStats();
    Result<Map<String, Object>> getCommentTrend();
    Result<Map<String, Object>> getViewTrend();
    Result<Map<String, Object>> getUserTrend();
    Result<Map<String, Object>> getNoteTrend();
}