package com.hope.chufala.service.impl;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.hope.chufala.common.exception.user.*;
import com.hope.chufala.common.util.AliOSSUtils;
import com.hope.chufala.common.util.EmailVerificationCodeUtils;
import com.hope.chufala.common.util.SnowFlakeUtils;
import com.hope.chufala.model.dto.RegisterFormDTO;
import com.hope.chufala.model.dto.UserUpdateFormDTO;
import com.hope.chufala.model.entity.User;
import com.hope.chufala.mapper.UserMapper;
import com.hope.chufala.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.regex.Pattern;

@Slf4j
@Service
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private EmailVerificationCodeUtils emailVerificationCodeUtils;
    @Autowired
    private AliOSSUtils aliOSSUtils;

    /**
     * 历史遗留盐值。早期实现为 MD5("hope" + 明文)，此处仅用于兼容存量数据，
     * 新注册用户一律走 BCrypt，不再使用该盐。
     */
    private static final String LEGACY_SALT = "hope";

    /**
     * 历史 MD5 哈希特征：32 位十六进制。
     * BCrypt 哈希固定 60 位且以 $2a$ / $2b$ 开头，两者不会互相误判。
     */
    private static final Pattern LEGACY_MD5 = Pattern.compile("^[0-9a-fA-F]{32}$");

    @Override
    public User login(String email, String password) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("email", email));
        // 用户不存在与密码错误返回同一提示，避免暴露某个邮箱是否已注册
        if (user == null || !matches(password, user.getPassword())) {
            log.warn("用户名密码错误，用户邮箱（IP）:{}", email);
            throw new InvalidLoginException("用户名或密码错误");
        }
        // 登录是唯一能拿到明文的时机，顺手把历史 MD5 哈希升级为 BCrypt
        upgradeLegacyHash(user, password);
        return user;
    }

    /**
     * 校验明文密码与库中哈希是否匹配。
     * 兼容存量数据：早期用 MD5(固定盐 + 明文) 存储，此时按旧算法比对；
     * 其余情况按 BCrypt 校验，格式非法则统一按不匹配处理，避免穿透成 500。
     */
    private boolean matches(String rawPassword, String storedHash) {
        if (storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        if (LEGACY_MD5.matcher(storedHash).matches()) {
            return matchesLegacyMd5(rawPassword, storedHash);
        }
        try {
            return BCrypt.checkpw(rawPassword, storedHash);
        } catch (IllegalArgumentException e) {
            log.warn("库中密码哈希格式无法识别，已按不匹配处理");
            return false;
        }
    }

    /** 按历史算法 MD5(固定盐 + 明文) 比对；用常量时间比较，避免时序侧信道 */
    private boolean matchesLegacyMd5(String rawPassword, String storedHash) {
        byte[] expected = storedHash.toLowerCase().getBytes(StandardCharsets.UTF_8);
        byte[] actual = SecureUtil.md5(LEGACY_SALT + rawPassword).getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(expected, actual);
    }

    /**
     * 把库中的历史 MD5 哈希就地升级为 BCrypt；升级失败不影响本次登录。
     *
     * 注意：这里必须用 UpdateWrapper 只 set password 一个字段。
     * User 实体的 role / status / vip / isDelete 是原始类型 int，永远不为 null，
     * 若改用 updateById(user)，MyBatis-Plus 的 NOT_NULL 策略会把这些字段一并写回 0，
     * 导致用户 vip 等状态被清空。
     */
    private void upgradeLegacyHash(User user, String rawPassword) {
        String storedHash = user.getPassword();
        if (storedHash == null || !LEGACY_MD5.matcher(storedHash).matches()) {
            return;
        }
        try {
            String upgraded = BCrypt.hashpw(rawPassword);
            UpdateWrapper<User> wrapper = new UpdateWrapper<>();
            wrapper.eq("id", user.getId());
            wrapper.set("password", upgraded);
            userMapper.update(null, wrapper);
            user.setPassword(upgraded);
            log.info("用户 {} 的密码哈希已由历史 MD5 升级为 BCrypt", user.getId());
        } catch (Exception e) {
            log.error("用户 {} 密码哈希升级失败，本次登录不受影响", user.getId(), e);
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
    public boolean updateById(UserUpdateFormDTO userUpdateFrom) {
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
