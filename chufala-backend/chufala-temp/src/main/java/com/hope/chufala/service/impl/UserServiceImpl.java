package com.hope.chufala.service.impl;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hope.chufala.common.exception.user.*;
import com.hope.chufala.common.util.AliOSSUtils;
import com.hope.chufala.common.util.EmailVerificationCodeUtils;
import com.hope.chufala.common.util.SnowFlakeUtils;
import com.hope.chufala.domain.dto.RegisterFormDTO;
import com.hope.chufala.domain.dto.UserUpdateFromDTO;
import com.hope.chufala.domain.entity.User;
import com.hope.chufala.mapper.UserMapper;
import com.hope.chufala.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private EmailVerificationCodeUtils emailVerificationCodeUtils;
    @Autowired
    private AliOSSUtils aliOSSUtils;

    @Override
    public User login(String email, String password) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("email", email));
        // 用户不存在与密码错误返回同一提示，避免暴露某个邮箱是否已注册
        if (user == null || !matches(password, user.getPassword())) {
            log.warn("用户名密码错误，用户邮箱（IP）:{}", email);
            throw new InvalidLoginException("用户名或密码错误");
        }
        return user;
    }

    /**
     * 校验明文密码与库中哈希是否匹配。
     * 兼容历史数据：早期用 MD5(固定盐 + 密码) 存储，格式不是 BCrypt，
     * 此时直接判定为不匹配，避免解析异常穿透成 500。
     */
    private boolean matches(String rawPassword, String storedHash) {
        if (storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        try {
            return BCrypt.checkpw(rawPassword, storedHash);
        } catch (IllegalArgumentException e) {
            log.warn("库中密码哈希格式无法识别（可能是历史 MD5 数据），已按不匹配处理");
            return false;
        }
    }

    @Override
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }


    @Override
    public void register(RegisterFormDTO registerFormDTO,String userIP) {
        User user = new User();
        user.setUsername(registerFormDTO.getUsername());
        user.setEmail(registerFormDTO.getEmail());
        user.setPassword(registerFormDTO.getPassword());
        boolean flag = emailVerificationCodeUtils.verifyCode(user.getEmail(), registerFormDTO.getVerifyCode());
        if (!flag) {
            log.warn("邮箱验证码错误，用户ip:{}",userIP);
            throw new InvalidEmailCodeException("邮箱验证码错误");
        }
        if (userMapper.selectOne(new QueryWrapper<User>().eq("email", user.getEmail())) != null) {
            log.warn("邮箱已存在，用户ip:{}",userIP);
            throw new EmailAlreadyExistsException("该邮箱已存在");
        }
        Long id = SnowFlakeUtils.nextId();
        user.setId(id);
        // BCrypt 自带随机盐，盐随哈希一起存储，无需再用全局固定盐
        user.setPassword(BCrypt.hashpw(user.getPassword()));
        user.setAvatar("https://chufala.oss-cn-shenzhen.aliyuncs.com/43e4927a-4a9c-4dbc-82cc-82a51f85f6bf.jpg");    //默认头像
        try {
            userMapper.insert(user);
        } catch (Exception e) {
            log.error("用户注册失败",e);
            throw new RegistrationFailedException("用户注册失败,请稍后重试");
        }
    }

    @Override
    public boolean isVipUser(Long userId) {
        return userMapper.selectOne(new QueryWrapper<User>().eq("id", userId).eq("vip", 1)) != null;
    }

    @Override
    public String uploadAvatar(MultipartFile avatar) {
        String url = null;
        try {
            if(avatar == null){
                throw new UploadFailException("头像不能为空");
            }
            url = aliOSSUtils.upload(avatar);
            if(url == null){
                throw new UploadFailException("头像上传失败");
            }
        } catch (IOException e) {
            throw new UploadFailException("头像上传失败"+e.getMessage());
        }
        return url;
    }

    @Override
    public boolean updateById(UserUpdateFromDTO userUpdateFrom) {
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", userUpdateFrom.getId()); 

        if (userUpdateFrom.getUsername() != null) {
            updateWrapper.set("username", userUpdateFrom.getUsername());
        }
        if (userUpdateFrom.getGender() != null) {
            updateWrapper.set("gender", userUpdateFrom.getGender());
        }
        if (userUpdateFrom.getBio() != null) {
            updateWrapper.set("bio", userUpdateFrom.getBio());
        }
        if (userUpdateFrom.getAvatar() != null) {
            updateWrapper.set("avatar", userUpdateFrom.getAvatar());
        }
        if (userUpdateFrom.getBirthday() != null) {
            updateWrapper.set("birthday", userUpdateFrom.getBirthday());
        }

        return userMapper.update(null, updateWrapper) > 0;
    }
}
