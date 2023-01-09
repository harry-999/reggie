package com.hhl.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

@ControllerAdvice(annotations = {RestController.class, Controller.class})//表示拦截哪些类型的controller注解
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 异常SQLIntegrityConstraintViolationException处理
     * @param ex
     * @return
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)//定义SQLIntegrityConstraintViolationException的异常处理
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error(ex.getMessage());
        // 用户名重复报错 ，因为表中的username字段定义的约束是unique
        String[] split = ex.getMessage().split(" ");
        if(ex.getMessage().contains("Duplicate entry")){
            String msg=split[2]+"已存在";
            return R.error(msg);
        }

        return R.error("未知错误");
    }

    /**
     * 异常ClassCastException处理
     * @param ex
     * @return
     */
    @ExceptionHandler(ClassCastException.class)//定义SQLIntegrityConstraintViolationException的异常处理
    public R<String> exceptionHandler(ClassCastException ex){
//        自定义的分类中如果绑定了菜品或者套餐，如果删除分类会报错ClassCastException
        return R.error(ex.getMessage());
    }

    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex){
            return R.error(ex.getMessage());
    }

}
