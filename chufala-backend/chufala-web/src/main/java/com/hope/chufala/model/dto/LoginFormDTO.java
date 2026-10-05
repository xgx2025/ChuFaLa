package com.hope.chufala.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录请求参数。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginFormDTO {
    /** 登录邮箱 */
    private String email;
    /** 登录密码（明文传输，由服务端比对哈希） */
    private String password;
}
