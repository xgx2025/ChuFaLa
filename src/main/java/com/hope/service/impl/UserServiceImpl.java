package com.hope.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hope.domain.entity.User;
import com.hope.mapper.UserMapper;
import com.hope.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public User login(String email, String password) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        String passwordEncrypt = SecureUtil.md5(password);
        qw.eq("email", email).eq("password", passwordEncrypt);
        return userMapper.selectOne(qw);
    }

    @Override
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }
}
