package com.hope.chufala.service;

import com.hope.chufala.model.dto.RegisterFormDTO;
import com.hope.chufala.model.dto.UserUpdateFormDTO;
import com.hope.chufala.model.entity.User;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户服务。
 *
 * @author 谢光湘
 */
public interface IUserService {

    /**
     * 用户登录（校验邮箱与密码）。
     *
     * @param email    邮箱
     * @param password 明文密码
     * @return 登录成功的用户
     */
    User login(String email, String password);

    /**
     * 按 ID 查询用户。
     *
     * @param id 用户 ID
     * @return 用户
     */
    User getUserById(Long id);

    /**
     * 用户注册（校验邮箱验证码与图形验证码）。
     *
     * @param registerFormDTO 注册表单
     * @param userIP          注册来源 IP，用于频控
     */
    void register(RegisterFormDTO registerFormDTO,String userIP);

    /**
     * 判断用户是否为 VIP。
     *
     * @param userId 用户 ID
     * @return 是 VIP 返回 true
     */
    boolean isVipUser(Long userId);

    /**
     * 更新用户资料。
     *
     * @param user 待更新的资料
     * @return 是否成功
     */
    boolean updateById(UserUpdateFormDTO user);

    /**
     * 上传用户头像。
     *
     * @param avatar 头像文件
     * @return 头像访问 URL
     */
    String uploadAvatar(MultipartFile avatar);
}
