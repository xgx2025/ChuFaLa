package com.hope.chufala.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 用户资料更新参数。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateFormDTO {
    /** 用户 ID；序列化为字符串避免前端精度丢失 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /** 用户名 */
    private String username;
    /** 性别 */
    private String gender;
    /** 个人简介 */
    private String bio;
    /** 头像 URL */
    private String avatar;
    /** 生日 */
    private LocalDate birthday;
}
