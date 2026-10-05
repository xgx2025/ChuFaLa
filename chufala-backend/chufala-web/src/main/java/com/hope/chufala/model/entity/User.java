package com.hope.chufala.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体。
 *
 * <p>未显式标注 {@code @TableName}，表名由类名推导为 {@code user}。
 * 该实体是登录态与权限判定的数据载体：登录成功后关键字段写入 JWT，
 * 请求期由 LoginInterceptor 解析并存入 ThreadLocal，供 AccessControl 做越权校验。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
public class User {
    /** 主键；序列化为字符串，避免前端 JS 大整数精度丢失 */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    /** 登录用户名 */
    private String username;
    /** 邮箱，注册与登录凭证，全局唯一 */
    private String email;
    /** 密码（BCrypt 哈希存储），响应体不输出 */
    @JsonIgnore
    private String password;
    /** 手机号 */
    private String phone;
    /** 性别 */
    private String gender;
    /** 个人简介 */
    private String bio;
    /** 头像 URL */
    private String avatar;
    /** 注册时间，对应列 create_time */
    @TableField(value = "create_time")
    private LocalDateTime registerDate;
    /** 角色：2-管理员（AccessControl.ADMIN_ROLE），其余为普通用户 */
    private int role;
    /** 生日 */
    private LocalDate birthday;
    /** 账号状态，随登录态写入 JWT claims */
    private int status;
    /** VIP 标记，非 0 视为会员；为原始 int 永不为 null，更新须用 UpdateWrapper 显式 set，否则会被 NOT_NULL 策略写回 0 */
    private int vip;
    /** 逻辑删除标记，0-未删除，非 0-已删除 */
    private int isDelete;
}
