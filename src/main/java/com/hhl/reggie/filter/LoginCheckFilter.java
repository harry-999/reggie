package com.hhl.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hhl.reggie.common.BaseContext;
import com.hhl.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter
public class LoginCheckFilter implements Filter {
    //路径匹配器，支持通配符
    public static  final AntPathMatcher PATH_MATCHER = new AntPathMatcher();
    /*





     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request=(HttpServletRequest) servletRequest;
        HttpServletResponse response=(HttpServletResponse) servletResponse;

        //1、获取本次请求的URI 需要向下转型
        String requestURI = request.getRequestURI();
        log.info("拦截的请求： {}" ,requestURI);

        //2、判断本次请求是否需要处理
        //设置一些不需要拦截的请求
        String[] urls={
                "/employee/login" ,
                "employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login",
        };
        if(check(urls,requestURI)){
            //3、如果不需要处理，则直接放行
            chain.doFilter(request,response);
//            log.info("本次请求不需要处理直接放行");
            return;
        }

        //4-1、判断登录状态，如果已经登录，则直接放行
        //先获取request中的session对象,然后查看session中的employee值
        if(request.getSession().getAttribute("employee")!=null){

            Long eid = (Long)request.getSession().getAttribute("employee");
            //将id放入到线程中去，为了后面mybatis-plus中给公共字段赋值（MyMetaObjecthandler类）的时候获取id，注意一定要在doFilter前放入，否则和update的线程不一样
            BaseContext.setCurrentId(eid);
            chain.doFilter(request,response);
            log.info("用户已登录，用户id为 {}",eid);
            return;
        }
        //4-2判断移动端登录状态，如果已登录，则直接放行
        if(request.getSession().getAttribute("user") != null){
            //log.info("用户已登录，用户id为：{}",request.getSession().getAttribute("user"));
            //把用户id存储到本地的threadLocal
            Long userId = (Long) request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);
            chain.doFilter(request,response);
            return;
        }


        //5、如果未登录则返回未登录结果
        // 前端代码分析： 前端中有request.js代码中的响应拦截器，当我们响应给浏览器的数据msg为NOTLOGIN，则页面会自动跳转到登录页面
        /*
        service.interceptors.response.use(res => {
        if (res.data.code === 0 && res.data.msg === 'NOTLOGIN') {// 返回登录页面
        console.log('---/backend/page/login/login.html---')
        localStorage.removeItem('userInfo')
        window.top.location.href = '/backend/page/login/login.html'
      }
         */
//        log.info("用户未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;

    }

    /**
     * 用于判断请求是否在定义的请求中
     * @param urls
     * @param url
     * @return
     */
    public boolean check(String[] urls ,String url){
        for (String s : urls) {
            if(PATH_MATCHER.match(s,url)){
                return true;
            }
        }
        return false;
    }
}
