package com.hope.chufala.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 注册请求参数。
 *
 * <p>需同时通过邮箱验证码（verifyCode）与图形验证码（captcha）校验。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterFormDTO {
    /** 用户名 */
    private String username;
    /** 注册邮箱 */
    private String email;
    /** 密码（明文传输，服务端哈希后存储） */
    private String password;
    /** 邮箱验证码 */
    private String verifyCode;
    /** 图形验证码 */
    private String captcha;
}
