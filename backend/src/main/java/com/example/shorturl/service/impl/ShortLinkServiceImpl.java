package com.example.shorturl.service.impl;

import com.example.shorturl.dto.request.CreateShortLinkRequest;
import com.example.shorturl.dto.request.UpdateShortLinkRequest;
import com.example.shorturl.entity.ShortLink;
import com.example.shorturl.exception.BusinessException;
import com.example.shorturl.exception.ErrorCode;
import com.example.shorturl.mapper.ShortLinkMapper;
import com.example.shorturl.service.ShortLinkService;
import com.example.shorturl.util.ShortCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短链接服务实现
 */
@Service
@RequiredArgsConstructor
public class ShortLinkServiceImpl implements ShortLinkService {

    private final ShortLinkMapper shortLinkMapper;
    private final ShortCodeGenerator shortCodeGenerator;

    @Override
    @Transactional
    public ShortLink create(CreateShortLinkRequest request) {
        // 生成短码，确保唯一性
        String shortCode = generateUniqueShortCode();

        // 创建短链接实体
        ShortLink shortLink = new ShortLink();
        shortLink.setName(request.getName());
        shortLink.setOriginalUrl(request.getOriginalUrl());
        shortLink.setShortCode(shortCode);
        shortLink.setStatus("ENABLED");
        shortLink.setClickCount(0);

        // 插入数据库
        shortLinkMapper.insert(shortLink);

        return shortLink;
    }

    @Override
    public Map<String, Object> getList(String keyword, String status, Integer page, Integer size) {
        // 计算偏移量
        int offset = (page - 1) * size;

        // 查询列表
        List<ShortLink> items = shortLinkMapper.selectList(keyword, status, offset, size);

        // 查询总数
        Long total = shortLinkMapper.countList(keyword, status);

        // 构建响应
        Map<String, Object> result = new HashMap<>();
        result.put("items", items);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);

        return result;
    }

    @Override
    public ShortLink getById(Long id) {
        ShortLink shortLink = shortLinkMapper.selectById(id);
        if (shortLink == null) {
            throw new BusinessException(ErrorCode.SHORT_LINK_NOT_FOUND);
        }
        return shortLink;
    }

    @Override
    @Transactional
    public ShortLink update(Long id, UpdateShortLinkRequest request) {
        // 检查短链接是否存在
        ShortLink shortLink = getById(id);

        // 更新字段
        if (request.getName() != null) {
            shortLink.setName(request.getName());
        }
        if (request.getOriginalUrl() != null) {
            shortLink.setOriginalUrl(request.getOriginalUrl());
        }
        if (request.getStatus() != null) {
            shortLink.setStatus(request.getStatus());
        }

        // 更新数据库
        shortLinkMapper.updateById(shortLink);

        // 重新查询返回最新数据
        return shortLinkMapper.selectById(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // 检查短链接是否存在
        getById(id);

        // 删除
        shortLinkMapper.deleteById(id);
    }

    @Override
    @Transactional
    public ShortLink toggleStatus(Long id) {
        // 检查短链接是否存在
        ShortLink shortLink = getById(id);

        // 切换状态
        String newStatus = "ENABLED".equals(shortLink.getStatus()) ? "DISABLED" : "ENABLED";
        shortLink.setStatus(newStatus);

        // 更新数据库
        shortLinkMapper.updateById(shortLink);

        // 重新查询返回最新数据
        return shortLinkMapper.selectById(id);
    }

    @Override
    @Transactional
    public String getOriginalUrlByShortCode(String shortCode) {
        // 查询短链接
        ShortLink shortLink = shortLinkMapper.selectByShortCode(shortCode);
        if (shortLink == null) {
            throw new BusinessException(ErrorCode.SHORT_LINK_NOT_FOUND);
        }

        // 检查状态
        if ("DISABLED".equals(shortLink.getStatus())) {
            throw new BusinessException(ErrorCode.SHORT_LINK_DISABLED);
        }

        // 增加点击次数
        shortLinkMapper.incrementClickCount(shortLink.getId());

        return shortLink.getOriginalUrl();
    }

    /**
     * 生成唯一短码（最多尝试10次）
     */
    private String generateUniqueShortCode() {
        for (int i = 0; i < 10; i++) {
            String shortCode = shortCodeGenerator.generate();
            ShortLink existing = shortLinkMapper.selectByShortCode(shortCode);
            if (existing == null) {
                return shortCode;
            }
        }
        throw new BusinessException(ErrorCode.SHORT_CODE_EXISTS);
    }
}
