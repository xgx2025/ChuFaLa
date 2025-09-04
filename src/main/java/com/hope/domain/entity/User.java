package com.hope.domain.entity;

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
public class User {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String username;
    private String email;
    @JsonIgnore
    private String password;
    private String gender;
    private String bio;
    private String avatar;
    private LocalDateTime createTime;
    private LocalDateTime loginTime;
    private int role;
    private int status;
    private int vip;
    private int isDelete;
}
