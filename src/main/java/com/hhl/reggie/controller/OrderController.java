package com.hhl.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhl.reggie.common.R;
import com.hhl.reggie.dto.OrdersDto;
import com.hhl.reggie.entity.Orders;
import com.hhl.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        orderService.submit(orders);
        log.info("orders： {}",orders);
        return R.success("下单成功");
    }

    /**
     * 用户订单分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> page(Integer page, Integer pageSize){
        Page<OrdersDto> pageDto = orderService.getOrdersPage(page, pageSize);
        return R.success(pageDto);
    }

    /**
     * 用户再来一单
     * @param map
     * @return
     */
    @PostMapping("/again")
    public R<String> again(@RequestBody Map<String,String> map){
        String ids = map.get("id");
        long orderId = Long.parseLong(ids);
        orderService.againSubmit(orderId);
        return R.success("操作成功");
    }

    /**
     * 后台订单分页查询
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    public R<Page> ServiceOrderpage(Integer page,Integer pageSize,Integer number,String beginTime,String endTime){
        //创建分页构造器对象，里面存放的是Orders类
        Page<Orders> pageInfo = new Page<>();
        //构造条件查询对象
        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //条件查询orders表 gt 大于 lt小于
        ordersLambdaQueryWrapper.like(number!=null,Orders::getNumber,number)
                .gt(StringUtils.isNotEmpty(beginTime),Orders::getOrderTime,beginTime)
                .lt(StringUtils.isNotEmpty(endTime),Orders::getOrderTime,endTime);

        orderService.page(pageInfo,ordersLambdaQueryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 更新订单派送指令
     * @param orders
     * @return
     */
    @PutMapping()
    public R<String> send(@RequestBody Orders orders){
        //根据orderId找到orders对应的数据，修改status 3已派送 4已完成
        Long orderId = orders.getId();
        Orders order = orderService.getById(orderId);
        order.setStatus(orders.getStatus());
        orderService.updateById(order);
        return R.success("订单状态成功修改");
    }
}
