package com.hope.chufala.common.model.vo;

import com.hope.chufala.common.constant.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应包装。
 *
 * <p>约定 {@code code == 0} 表示成功（不是 1）；前端拦截器据此判断业务成败，
 * 分页数据放在 {@code data} 中的 PageResult 里。
 *
 * @author 谢光湘
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    /** 业务状态码，0 表示成功 */
    private Integer code;
    /** 提示文案 */
    private String message;
    /** 业务数据载荷 */
    private Object data;

    /**
     * 成功响应（无提示文案）。
     *
     * @param data 业务数据
     * @return 成功响应
     */
    public static Result ok(Object data) {
        return new Result(0, null, data);
    }

    /**
     * 成功响应（自定义提示文案）。
     *
     * @param data    业务数据
     * @param message 提示文案
     * @return 成功响应
     */
    public static Result ok(Object data,String message){
        return new Result(0,message,data);
    }
    /**
     * 失败响应（使用枚举自带的提示文案）。
     *
     * @param resultCode 状态码枚举
     * @return 失败响应
     */
    public static  Result fail(ResultCode resultCode){
        Integer code = resultCode.getCode();
        String message = resultCode.getMessage();
        return new Result(code,message,null);
    }

    /**
     * 失败响应（覆盖提示文案，状态码取自枚举）。
     *
     * @param resultCode 状态码枚举
     * @param message    自定义提示文案
     * @return 失败响应
     */
    public static Result fail(ResultCode resultCode,String message){
        Integer code = resultCode.getCode();
        return new Result(code,message,null);
    }
}
