package com.hope.chufala.common.exception;


import com.hope.chufala.common.constant.ResultCode;
import com.hope.chufala.common.model.vo.Result;
import com.hope.chufala.common.exception.biz.BizException;
import com.hope.chufala.common.exception.user.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateKeyException.class)
    public Result handleDuplicateKeyException(DuplicateKeyException e){
        log.error("数据库唯一冲突异常",e);
        String errorMessage = e.getMessage();
        String friendlyMsg = "数据已存在";
        //正则匹配：从异常信息中提取 值、字段名
        Pattern pattern = Pattern.compile("Duplicate entry '(.*?)' for key '(.*?)'");
        Matcher matcher = pattern.matcher(errorMessage);
        if (matcher.find()){
            String duplicateValue = matcher.group(1);   //冲突的值
            String keyName = matcher.group(2);  //冲突的字段名

            if(keyName.contains("username")){
                log.info("用户名冲突");
                friendlyMsg = String.format("用户名 %s 已被使用", duplicateValue);
            }else if (keyName.contains("email")){
                log.info("邮箱冲突");
                friendlyMsg = String.format("邮箱 %s 已注册", duplicateValue);
            }else if (keyName.contains("phone")){
                log.info("手机号冲突");
                friendlyMsg = String.format("手机号 %s 已绑定账号", duplicateValue);

            }
        }

        return Result.fail(ResultCode.DUPLICATE_KEY,friendlyMsg);

    }

    @ExceptionHandler(value= InvalidLoginException.class)
    public Result handleException(InvalidLoginException e){
        return Result.fail(ResultCode.USERNAME_OR_PASSWORD_ERROR, e.getMessage());
    }

    @ExceptionHandler(value= InvalidEmailCodeException.class)
    public Result handleException(InvalidEmailCodeException e){
        return Result.fail(ResultCode.EMAIL_VERIFY_CODE_ERROR, e.getMessage());
    }

    @ExceptionHandler(value= EmailAlreadyExistsException.class)
    public Result handleException(EmailAlreadyExistsException e){
        return Result.fail(ResultCode.EMAIL_ALREADY_EXISTS, e.getMessage());
    }

    @ExceptionHandler(value= RegistrationFailedException.class)
    public Result handleException(RegistrationFailedException e){
        return Result.fail(ResultCode.UNKNOWN_ERROR, e.getMessage());
    }

    @ExceptionHandler(value= VerifyCodeTooFrequentException.class)
    public Result handleException(VerifyCodeTooFrequentException e){
        return Result.fail(ResultCode.CAPTCHA_SEND_TOO_MANY_TIMES, e.getMessage());
    }

    @ExceptionHandler(value= {MessagingException.class, UnsupportedEncodingException.class})
    public Result handleException(Exception e){
       return Result.fail(ResultCode.UNKNOWN_ERROR, "邮箱验证码发送失败");
    }

    @ExceptionHandler(value= BizException.class)
    public Result handleException(BizException e, HttpServletRequest request){
        log.error("[业务异常] 请求路径：{}，异常信息：{}",request.getRequestURI(), e.getMessage(),e);
        return Result.fail(ResultCode.SYSTEM_BUSY, e.getMessage());
    }

    @ExceptionHandler(value = LocationUnavailableException.class)
    public Result handleLocationUnavailableException(LocationUnavailableException e){
        log.error("[位置信息不可用] 错误信息：{}",e.getMessage(),e);
        return Result.fail(ResultCode.LOCATION_UNAVAILABLE, e.getMessage());
    }

    @ExceptionHandler(value = InvalidSignatureException.class)
    public Result handleInvalidSignatureException(InvalidSignatureException e){
        log.error("[无效签名] 错误信息：{}",e.getMessage(),e);
        return Result.fail(ResultCode.DATA_NOT_SAFE, "数据不安全，操作被拒绝");
    }


    @ExceptionHandler(value = IllegalArgumentException.class)
    public Result handleIllegalArgumentException(IllegalArgumentException e){
        log.error("[参数非法异常] 错误信息：{}",e.getMessage(),e);
        return Result.fail(ResultCode.ILLEGAL_ARGUMENT, e.getMessage());
    }
    @ExceptionHandler(value = RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e){
        log.error("运行时异常",e);
        return Result.fail(ResultCode.SYSTEM_BUSY, e.getMessage());
    }

    //兜底方案
    @ExceptionHandler(value=Exception.class)
    public Result handleOtherException(Exception e){
        log.info(e.getMessage(),e);
        return Result.fail(ResultCode.UNKNOWN_ERROR, "未知错误");
    }
}
