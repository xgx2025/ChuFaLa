package com.hope.chufala.service;

import com.hope.chufala.model.dto.RegisterFormDTO;
import com.hope.chufala.model.dto.UserUpdateFromDTO;
import com.hope.chufala.model.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface IUserService {

    /**
     * 用户登录
     * @param email
     * @param password
     * @return
     */
    User login(String email, String password);

    /**
     * 根据id查询用户
     * @param id
     * @return
     */
    User getUserById(Long id);

    void register(RegisterFormDTO registerFormDTO,String userIP);

    boolean isVipUser(Long userId);

    boolean updateById(UserUpdateFromDTO user);

    String uploadAvatar(MultipartFile avatar);
}
