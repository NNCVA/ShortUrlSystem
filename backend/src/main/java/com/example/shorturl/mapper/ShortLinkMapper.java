package com.example.shorturl.mapper;

import com.example.shorturl.entity.ShortLink;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 短链接 Mapper
 */
@Mapper
public interface ShortLinkMapper {

    /**
     * 根据ID查询短链接
     */
    ShortLink selectById(@Param("id") Long id);

    /**
     * 根据短码查询短链接
     */
    ShortLink selectByShortCode(@Param("shortCode") String shortCode);

    /**
     * 查询短链接列表（分页、搜索、状态筛选）
     */
    List<ShortLink> selectList(@Param("keyword") String keyword,
                                @Param("status") String status,
                                @Param("offset") Integer offset,
                                @Param("limit") Integer limit);

    /**
     * 统计短链接总数（搜索、状态筛选）
     */
    Long countList(@Param("keyword") String keyword,
                   @Param("status") String status);

    /**
     * 插入短链接
     */
    int insert(ShortLink shortLink);

    /**
     * 更新短链接
     */
    int updateById(ShortLink shortLink);

    /**
     * 删除短链接
     */
    int deleteById(@Param("id") Long id);

    /**
     * 增加点击次数
     */
    int incrementClickCount(@Param("id") Long id);
}
