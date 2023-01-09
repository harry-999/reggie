package com.hhl.reggie.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

/**
 * 自定义业务异常
 */
@Slf4j
public class CustomException extends RuntimeException {

    public  CustomException(String message){
       super(message);
    }
}
