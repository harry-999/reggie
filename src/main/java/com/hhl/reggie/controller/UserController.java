package com.hhl.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hhl.reggie.common.R;
import com.hhl.reggie.entity.User;
import com.hhl.reggie.service.UserService;
import com.hhl.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 发送手机短信验证码
     * @param user
     * @return
     */
    //前端分析请求分析 请求的参数是以json格式发送phone的值所以采用@RequestBody User接收，session是为了将user用户信息存放到session域对象中
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取手机号
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)){
            //随机生成的4为验证码
            Integer integerCode = ValidateCodeUtils.generateValidateCode(4);
            String code = integerCode.toString();

            log.info("code={}",code);
            //调用阿里云提供的短信服务api完成发送短信  这里个人用户申请不了阿里云短信服务的签名，所以这里在后台输出了
            //SMSUtils.sendMessage("","","","");

            //把验证码存起来  这里使用session来存放验证码，当然也可以存到redis
            session.setAttribute(phone,code);
            return R.success("手机验证码发送成功");
        }

        return R.error("手机验证码发送失败");
    }

    /**
     * 登录客户端用户
     * @param map
     * @param session
     * @return
     */
    //前端调试分析，传入的数据是以json格式的phone和code，由于User对象没有code属性，所以这里采取Map来接收数据
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
//        log.info("map phone {} ,mapcode {} ",map.get("phone"),map.get("code"));
        //获取手机号
        String phone = map.get("phone").toString();

        //获取验证码
        String code = map.get("code").toString();

        //从Session中获取保存的验证码
        Object codeInSession = session.getAttribute(phone);

        //进行验证码的校验（页面提交到服务端的map存放的code和session中存放的code对比）
        if(codeInSession!=null&&codeInSession.equals(code)){
            // 如果校验成功，说明登录成功 ，则根据手机号去用户表中查询用户 操作user表
            LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userLambdaQueryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(userLambdaQueryWrapper);
            if(user==null){
                //如果查询的数据为空，表示是新用户没有注册，则自动完成注册（在user表中新建一条数据）
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                //注册新用户
                userService.save(user);
            }
                //如果查询到数据，则保持用户登录状态（在session中保存用户的登录状态，这样才能不会被拦截器拦截）
            session.setAttribute("user",user.getId());
            return R.success(user);
        }
            // 如果校验失败，则返回登录失败信息
        return R.error("登录失败");
    }

    @PostMapping("/loginout")
    public R<String> loginout(HttpSession session){
        //清除session中的用户id
        session.removeAttribute("user");
        return R.success("退出成功");
    }



}
