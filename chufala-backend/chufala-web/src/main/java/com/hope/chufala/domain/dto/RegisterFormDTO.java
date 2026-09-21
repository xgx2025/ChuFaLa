package com.hope.chufala.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterFormDTO {
    private String username;
    private String email;
    private String password;
    private String verifyCode;
    private String captcha;
}
