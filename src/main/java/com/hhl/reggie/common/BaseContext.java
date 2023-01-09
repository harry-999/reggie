package com.hhl.reggie.common;

/**
 * 基于ThreadLocal封装工具类，用于同一个线程中保存和获取当前登录用户id
 */
public class BaseContext  {
    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 在线程中设置保存值
     * @param id
     */
    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    /**
     * 在线程中获取保存值
     * @return
     */
    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
