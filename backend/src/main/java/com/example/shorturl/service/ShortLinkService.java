package com.example.shorturl.service;

import com.example.shorturl.dto.request.CreateShortLinkRequest;
import com.example.shorturl.dto.request.UpdateShortLinkRequest;
import com.example.shorturl.entity.ShortLink;

import java.util.List;
import java.util.Map;

/**
 * 短链接服务接口
 */
public interface ShortLinkService {

    /**
     * 创建短链接
     */
    ShortLink create(CreateShortLinkRequest request);

    /**
     * 获取短链接列表（分页、搜索、状态筛选）
     */
    Map<String, Object> getList(String keyword, String status, Integer page, Integer size);

    /**
     * 根据ID获取短链接
     */
    ShortLink getById(Long id);

    /**
     * 更新短链接
     */
    ShortLink update(Long id, UpdateShortLinkRequest request);

    /**
     * 删除短链接
     */
    void delete(Long id);

    /**
     * 切换短链接状态
     */
    ShortLink toggleStatus(Long id);

    /**
     * 根据短码获取原始URL并增加点击数
     */
    String getOriginalUrlByShortCode(String shortCode);

    /**
     * 异步增加点击数
     */
    void incrementClickCountAsync(Long id);
}
