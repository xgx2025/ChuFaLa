package com.hope.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hope.constant.ResultCode;
import com.hope.domain.dto.RegisterFormDTO;
import com.hope.domain.entity.User;
import com.hope.domain.vo.Result;
import com.hope.mapper.UserMapper;
import com.hope.service.IUserService;
import com.hope.utils.EmailVerificationCodeUtil;
import com.hope.utils.SnowFlakeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private EmailVerificationCodeUtil emailVerificationCodeUtil;

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

    @Override
    public Result register(RegisterFormDTO registerFormDTO) {
        User user = new User();
        user.setUsername(registerFormDTO.getUsername());
        user.setEmail(registerFormDTO.getEmail());
        user.setPassword(registerFormDTO.getPassword());
        boolean flag = emailVerificationCodeUtil.verifyCode(user.getEmail(), registerFormDTO.getVerifyCode());
        if (!flag) {
            return Result.fail(ResultCode.EMAIL_VERIFY_CODE_ERROR);
        }
        if (userMapper.selectOne(new QueryWrapper<User>().eq("email", user.getEmail())) != null) {
            return Result.fail(ResultCode.EMAIL_ALREADY_EXISTS);
        }
        Long id = SnowFlakeUtil.nextId();
        user.setId(id);
        String passwordEncrypt = SecureUtil.md5(user.getPassword());
        user.setPassword(passwordEncrypt);
        if (userMapper.insert(user) > 0){
            return Result.ok(null);
        }
        return Result.fail(ResultCode.UNKNOWN_ERROR);
    }
}
