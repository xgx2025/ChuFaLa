package com.hope.domain.vo;


import com.hope.constant.ResultCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private Integer code;
    private String msg;
    private Object data;

    public static Result ok(Object data) {
        return new Result(0, null, data);
    }
    public static  Result fail(ResultCode resultCode){
        Integer code = resultCode.getCode();
        String msg = resultCode.getMessage();
        return new Result(code,msg,null);
    }
}
