package com.hhl.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hhl.reggie.common.R;
import com.hhl.reggie.dto.OrdersDto;
import com.hhl.reggie.entity.OrderDetail;
import com.hhl.reggie.entity.Orders;

import java.util.List;

public interface OrderService extends IService<Orders> {
    //用户提交订单支付
    public void submit(Orders orders);

    //通过订单id查询订单明细，得到一个订单明细的集合
    //这里抽离出来是为了避免在stream中遍历的时候直接使用构造条件来查询导致eq叠加，从而导致后面查询的数据都是null
    public List<OrderDetail> getOrderDetailListByOrderId(Long orderId);

    //用户端展示自己的订单分页查询
    public Page<OrdersDto> getOrdersPage(int page,int pageSize);

    //用户再来一单
    public void againSubmit(long orderId);

}
