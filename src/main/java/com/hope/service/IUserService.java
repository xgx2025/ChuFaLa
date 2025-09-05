package com.hope.service;

import com.hope.domain.dto.RegisterFormDTO;
import com.hope.domain.entity.User;
import com.hope.domain.vo.Result;

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

    Result register(RegisterFormDTO registerFormDTO);
}
