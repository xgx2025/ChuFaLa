package com.hope.chufala.common.exception;


import com.hope.chufala.common.constant.ResultCode;
import com.hope.chufala.common.model.vo.Result;
import com.hope.chufala.common.exception.biz.BizException;
import com.hope.chufala.common.exception.user.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 全局异常处理器
 *
 * <p>统一把各类异常转换为 {@link Result} 响应，避免异常细节直接暴露给前端；
 * 具体异常类型决定错误码，未匹配到的一律走兜底分支。
 *
 * @author 谢光湘
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 权限不足 → HTTP 403。
     *
     * @param e 异常
     * @return 统一失败响应
     */
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result handleForbiddenException(ForbiddenException e) {
        return Result.fail(ResultCode.USER_NOT_PERMITTED, e.getMessage());
    }

    /**
     * 资源不存在 → HTTP 404。
     *
     * @param e 异常
     * @return 统一失败响应
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result handleResourceNotFoundException(ResourceNotFoundException e) {
        return Result.fail(ResultCode.NOT_FOUND, e.getMessage());
    }


    /**
     * 数据库唯一键冲突：从异常信息中解析冲突字段，转成友好提示。
     *
     * @param e 异常
     * @return 统一失败响应
     */
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

    /**
     * 登录失败（用户名或密码错误）。
     *
     * @param e 异常
     * @return 统一失败响应
     */
    @ExceptionHandler(value= InvalidLoginException.class)
    public Result handleException(InvalidLoginException e){
        return Result.fail(ResultCode.USERNAME_OR_PASSWORD_ERROR, e.getMessage());
    }

    /**
     * 邮箱验证码错误。
     *
     * @param e 异常
     * @return 统一失败响应
     */
    @ExceptionHandler(value= InvalidEmailCodeException.class)
    public Result handleException(InvalidEmailCodeException e){
        return Result.fail(ResultCode.EMAIL_VERIFY_CODE_ERROR, e.getMessage());
    }

    /**
     * 邮箱已被注册。
     *
     * @param e 异常
     * @return 统一失败响应
     */
    @ExceptionHandler(value= EmailAlreadyExistsException.class)
    public Result handleException(EmailAlreadyExistsException e){
        return Result.fail(ResultCode.EMAIL_ALREADY_EXISTS, e.getMessage());
    }

    /**
     * 注册失败。
     *
     * @param e 异常
     * @return 统一失败响应
     */
    @ExceptionHandler(value= RegistrationFailedException.class)
    public Result handleException(RegistrationFailedException e){
        return Result.fail(ResultCode.UNKNOWN_ERROR, e.getMessage());
    }

    /**
     * 验证码发送过于频繁。
     *
     * @param e 异常
     * @return 统一失败响应
     */
    @ExceptionHandler(value= VerifyCodeTooFrequentException.class)
    public Result handleException(VerifyCodeTooFrequentException e){
        return Result.fail(ResultCode.CAPTCHA_SEND_TOO_MANY_TIMES, e.getMessage());
    }

    /**
     * 邮件发送相关异常（不向客户端暴露细节）。
     *
     * @param e 异常
     * @return 统一失败响应
     */
    @ExceptionHandler(value= {MessagingException.class, UnsupportedEncodingException.class})
    public Result handleException(Exception e){
       return Result.fail(ResultCode.UNKNOWN_ERROR, "邮箱验证码发送失败");
    }

    /**
     * 业务异常（记录请求路径与堆栈）。
     *
     * @param e       异常
     * @param request 当前请求
     * @return 统一失败响应
     */
    @ExceptionHandler(value= BizException.class)
    public Result handleException(BizException e, HttpServletRequest request){
        log.error("[业务异常] 请求路径：{}，异常信息：{}",request.getRequestURI(), e.getMessage(),e);
        return Result.fail(ResultCode.SYSTEM_BUSY, e.getMessage());
    }

    /**
     * 位置信息不可用。
     *
     * @param e 异常
     * @return 统一失败响应
     */
    @ExceptionHandler(value = LocationUnavailableException.class)
    public Result handleLocationUnavailableException(LocationUnavailableException e){
        log.error("[位置信息不可用] 错误信息：{}",e.getMessage(),e);
        return Result.fail(ResultCode.LOCATION_UNAVAILABLE, e.getMessage());
    }

    /**
     * 签名校验失败（不向客户端暴露具体原因）。
     *
     * @param e 异常
     * @return 统一失败响应
     */
    @ExceptionHandler(value = InvalidSignatureException.class)
    public Result handleInvalidSignatureException(InvalidSignatureException e){
        log.error("[无效签名] 错误信息：{}",e.getMessage(),e);
        return Result.fail(ResultCode.DATA_NOT_SAFE, "数据不安全，操作被拒绝");
    }


    /**
     * 参数非法。
     *
     * @param e 异常
     * @return 统一失败响应
     */
    @ExceptionHandler(value = IllegalArgumentException.class)
    public Result handleIllegalArgumentException(IllegalArgumentException e){
        log.error("[参数非法异常] 错误信息：{}",e.getMessage(),e);
        return Result.fail(ResultCode.ILLEGAL_ARGUMENT, e.getMessage());
    }
    /**
     * 未归类的运行时异常。
     *
     * @param e 异常
     * @return 统一失败响应
     */
    @ExceptionHandler(value = RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e){
        log.error("运行时异常",e);
        return Result.fail(ResultCode.SYSTEM_BUSY, e.getMessage());
    }

    //兜底方案
    /**
     * 兜底：其余所有异常统一返回未知错误。
     *
     * @param e 异常
     * @return 统一失败响应
     */
    @ExceptionHandler(value=Exception.class)
    public Result handleOtherException(Exception e){
        log.info(e.getMessage(),e);
        return Result.fail(ResultCode.UNKNOWN_ERROR, "未知错误");
    }
}
