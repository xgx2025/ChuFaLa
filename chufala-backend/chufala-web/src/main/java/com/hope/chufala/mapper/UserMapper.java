package com.hope.chufala.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hope.chufala.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper。
 *
 * <p>除通用 CRUD 外，提供仅取邮箱的轻量查询（发通知邮件时避免整行加载）。
 *
 * @author 谢光湘
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    /**
     * 按用户 ID 查询邮箱。
     *
     * @param userId 用户 ID
     * @return 邮箱，不存在时为 null
     */
    String findEmailById(Long userId);
}
