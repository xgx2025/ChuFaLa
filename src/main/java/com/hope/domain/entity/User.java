package com.hope.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user")
public class User {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String username;
    private String email;
    @JsonIgnore
    private String password;
    private String phone;
    private String gender;
    private String bio;
    private String avatar;
    @TableField(value = "create_time")
    private LocalDateTime createTime;
    @TableField(value = "login_time")
    private LocalDateTime loginTime;
    private int role;
    private int status;
    private int vip;
    @TableField(value = "is_delete")
    private int isDelete;
}
