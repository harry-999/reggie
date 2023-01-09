package com.hhl.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhl.reggie.common.R;
import com.hhl.reggie.entity.Employee;
import com.hhl.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 登录用户
     * @param request
     * @param employee
     * @return
     */
//    形参设计目的：HttpServletRequest request 为了将登录成功后的用户储存到浏览器中以后获取方便
//    @RequestBody Employee employee为了获取用户输入的账户和密码 @RequestBody注解会将请求体数据赋值给形参
//    前端通过数据loginForm loginForm:{
//            username: 'admin',
//            password: '123456'
//          } 用post请求发送给employee/login @PostMapping接受请求，并且@RequestBody自动接受json数据并且给employee对应的属性值赋值
    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //1、将页面提交的密码password进行md5加密处理
        String password = employee.getPassword();
        password = DigestUtils.md5DigestAsHex(password.getBytes());

        //2、根据页面提交的用户名username查询数据库(采用mybatis-plus)
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        // 根据Wrapper条件在数据库中查询是否存在对应的数据，并且转化为对应的实体类对象
        Employee emp = employeeService.getOne(queryWrapper);

        //3、如果没有查询到则返回登录失败结果
        if(emp==null){
            return R.error("没有此用户请重新注册");
        }

        //4、密码比对，如果不一致则返回登录失败结果
        if(!emp.getPassword().equals(password)){
            return R.error("对不起输入的账号或者密码有误，请重新输入");
        }
        //5、查看员工状态，如果为已禁用状态，则返回员工已禁用结果
        if(emp.getStatus()==0){
            return R.error("对不起该用户已经被锁定");
        }
        //6、登录成功，将员工id存入Session并返回登录成功结果
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    /**
     * 退出登录
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //1、清除Session中的用户信息
        HttpSession session = request.getSession();
        session.removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工信息
     * @param employee
     * @return
     */
    @PostMapping()
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){

        //设置初始密码为123456，注意要用MD5加密
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));

        /*
        * 这里采用mybatis-plus中的自动填充数据给所有表中的公共字段填充数据
        * */
//        //设置创建日期和修改日期
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//
//        //创建创建和修改的用户
//        //获取session中的用户id
//        HttpSession session = request.getSession();
//        Long eid = (Long) session.getAttribute("employee");
//        employee.setCreateUser(eid);
//        employee.setUpdateUser(eid);

        //利用Mybatis-plus中的api接口来添加数据到表中
        employeeService.save(employee);
        return R.success("添加员工成功");
    }

    /**
     * 显示员工列表
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    // data() {
    //          return {
    //             input: '',
    //             counts: 0,
    //             page: 1,
    //             pageSize: 10,
    //             tableData : [],
    //             id : '',
    //             status : '',
    //          }
    //        } 前端中传入服务端的数据是直接的数据名，所以不需要用@RequestBpdy，只需要对应名字即可
    //根据前端需要的数据来设置形参 page表示当前页面 pageSize表示页面总共数据 name表示可以根据员工的name来查询员工
    //注意返回类型是Page对象（根据前端代码可知展示员工列表数据是根据res.data.recoreds 后端中的R响应给前端中的res R.data响应给前端的res.data 此时采用Page对象的原因是Mybatis-plus中的Page有recoreds属性和total属性），
    // 在Mybatis-Plus中封装好，不需要我们封装，
    public R<Page> page(int page,int pageSize,String name){

        //构造分页构造器（limit）
        Page pageinfo = new Page(page,pageSize);

        //构造条件构造器（where）
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();

        //添加过滤条件（like，名字模糊查询最好用like）
        //第一个形参表示如果为true则才会判断
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);

        //添加排序条件（order）
        queryWrapper.orderByDesc(Employee::getUpdateTime);

        //执行查询
        employeeService.page(pageinfo,queryWrapper);
        return R.success(pageinfo);
    }

    /**
     * 根据员工id修改员工信息
     * @return
     */
        @PutMapping()
        public R<String>update(HttpServletRequest request,@RequestBody Employee employee){



            /*
             * 这里采用mybatis-plus中的自动填充数据给所有表中的公共字段填充数据
             * */
//            HttpSession session = request.getSession();
//            Long eid = (Long) session.getAttribute("employee");
//            employee.setUpdateTime(LocalDateTime.now());
//            employee.setUpdateUser(eid);
            employeeService.updateById(employee);
            return R.success("员工修改完成");
        }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
//    根据前端调试页面分析，该请求方式为get同时在URL中直接以id号传输，没有设置键名，所以用@PathVariable来接收
    @GetMapping("/{id}")
        public R<Employee> getById(@PathVariable Long id){
            log.info("根据id查询员工信息");
            Employee employee = employeeService.getById(id);
            if(employee!=null){
                return R.success(employee);
            }
            return R.error("没有查询到对应的员工信息");

        }




}
