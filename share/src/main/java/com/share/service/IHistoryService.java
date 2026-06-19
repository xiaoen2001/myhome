package com.share.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.share.common.Result;
import com.share.entity.History;
import com.share.vo.HistoryVO;

public interface IHistoryService extends IService<History> {
    boolean saveHistory(Long userId, Long noteId);
    Result<?> deleteHistory(Long id, Long userId);
    Result<?> deleteHistorys(Long userId);
    Result<Page<HistoryVO>> getMyHistory(Long userId, Integer page, Integer pageSize, String keyword);
}