package com.zjw.gulimall.product.exception;

import com.zjw.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ValidationException;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-07 10:10
 * @Modifier:
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.zjw.gulimall.product.controller")
public class GlobalProductExceptionHandler
{
    //统一处理参数校验异常
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R validatedExceptionHandler(MethodArgumentNotValidException e){
        e.printStackTrace();
        Map<String, String> errorMap = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(err -> {
            log.error("err>>{}>>{}", err.getField(), err.getDefaultMessage());
            errorMap.put(err.getField(), err.getDefaultMessage());
        });
        return R.error("10100").put("data", errorMap);
    }

    @ExceptionHandler(value = Throwable.class)
    public R defaultExceptionHandler(Exception e){
        e.printStackTrace();
        log.error("err>>{}", e.getMessage());
        return R.error("10000");
    }
}
