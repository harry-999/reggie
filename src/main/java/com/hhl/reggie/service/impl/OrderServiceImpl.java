package com.hhl.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hhl.reggie.common.BaseContext;
import com.hhl.reggie.common.CustomException;
import com.hhl.reggie.dto.OrdersDto;
import com.hhl.reggie.entity.*;
import com.hhl.reggie.mapper.OrderMapper;
import com.hhl.reggie.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Orders> implements OrderService {

    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private UserService userService;
    @Autowired
    private OrderService orderService;

    /**
     * 用户提交支付订单
     * @param orders
     */
    @Override
    public void submit(Orders orders) {
        //前端传输服务端的数据中没有设置userId，所以要先获取userid
        Long userId = BaseContext.getCurrentId();
        orders.setUserId(userId);

        //寻找是否订单中有套餐或者菜品的数据，通过userId操作shoppingcarts
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(userId!=null,ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(shoppingCartLambdaQueryWrapper);

        //如果没有数据，则需要抛出业务异常
        if (shoppingCarts==null||shoppingCarts.size()==0){
            throw new CustomException("购物车为空不能下单");
        }
        //根据前端传输的addressBookId查询addressbook表中的地址信息获取用户地址信息
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if(addressBook==null){
            //如果地址信息不存在则抛出业务异常
            throw new CustomException("地址信息有误，不能下单");
        }

        //以下为正常清空的下单
        //使用mybatis-plus工具类设置随机的id作为订单id orderid
        long orderId = IdWorker.getId();
        orders.setId(orderId);

        //进行购物车的金额数据计算 顺便把订单明细给计算出来
        AtomicInteger amount = new AtomicInteger(0);//使用原子类来保存计算的金额结果 作用和锁有类似之处，是为了保证并发情况下的线程安全。

        //新建一个orderDetails列表用于存放订单信息
        ArrayList<OrderDetail> orderDetails = new ArrayList<>();
        //遍历shoppingCarts中的每一个数据，将其orderId\Number数量 口味信息dish_flavour、dish_id、setmeal_id、Name、image、amount金额(单份价格)、设置给order-detail表中
        for (ShoppingCart shoppingCart : shoppingCarts) {
            OrderDetail orderDetail = new OrderDetail();
            //给orderDetail添加orderId
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(shoppingCart.getNumber());
            orderDetail.setDishFlavor(shoppingCart.getDishFlavor());
            orderDetail.setDishId(shoppingCart.getDishId());
            orderDetail.setSetmealId(shoppingCart.getSetmealId());
            orderDetail.setName(shoppingCart.getName());
            orderDetail.setImage(shoppingCart.getImage());
            orderDetail.setAmount(shoppingCart.getAmount());
            //将所有的订单计算amount订单的总价格 addAndGet进行累加 item.getAmount()单份的金额  multiply乘  item.getNumber()份数
            amount.addAndGet(shoppingCart.getAmount().multiply(new BigDecimal(shoppingCart.getNumber())).intValue());
            //将orderDetail存放到列表中
            orderDetails.add(orderDetail);
        }
        //将orderDetail写入到数据库中
        orderDetailService.saveBatch(orderDetails);

        //给orders表写入数据
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        //设置订单状态 订单状态 1待付款，2待派送，3已派送，4已完成，5已取消
        orders.setStatus(2);
        //设置订单的总金额
        orders.setAmount(new BigDecimal(amount.get()));
        orders.setUserId(userId);
        //设置订单号
        orders.setNumber(String.valueOf(orderId));
        //设置收货人
        orders.setConsignee(addressBook.getConsignee());
        //设置收货人手机
        orders.setPhone(addressBook.getPhone());
        //设置收货地址
        orders.setAddress(addressBook.getProvinceCode()==null? "" : addressBook.getProvinceName()
                +(addressBook.getCityCode()==null?"":addressBook.getCityName())
                +(addressBook.getDistrictCode()==null?"":addressBook.getDistrictName())
                +(addressBook.getDetail() == null ? "" : addressBook.getDetail())
        );


        //将orders入库
        this.save(orders);

        //设置用户姓名
        User user = userService.getById(userId);
        if(user.getName()!=null){
            orders.setUserName(user.getName());
        }

        //支付完成后需要清空购物车订单
        shoppingCartService.remove(shoppingCartLambdaQueryWrapper);
    }

    /**
     * 根据orderId查询orderDetail列表
     * @param orderId
     * @return
     */
    @Override
    public List<OrderDetail> getOrderDetailListByOrderId(Long orderId){
        LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
        orderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId,orderId);
        List<OrderDetail> list = orderDetailService.list(orderDetailLambdaQueryWrapper);
        return list;
    }

    /**
     * 用户端展示自己的订单分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public Page<OrdersDto> getOrdersPage(int page, int pageSize) {
        //分页构造器
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        Page<OrdersDto> pageDto = new Page<>(page,pageSize);

        //构造条件查询器 根据orderId查询orders中数据存放到pageInfo对象的recoreds中
        LambdaQueryWrapper<Orders> ordersLambdaQueryWrapper = new LambdaQueryWrapper<>();
        ordersLambdaQueryWrapper.eq(Orders::getUserId,BaseContext.getCurrentId());
        ordersLambdaQueryWrapper.orderByDesc(Orders::getOrderTime);
        orderService.page(pageInfo,ordersLambdaQueryWrapper);

        //将pageInfo的信息（除了records属性）复制到pageDto中
        BeanUtils.copyProperties(pageInfo,pageDto,"records");

        //给orderDto进行属性赋值
        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> ordersDtoList=records.stream().map((item)->{
            //新建一个ordersDto对象
            OrdersDto ordersDto = new OrdersDto();
            //复制orders信息给dto基本信息
            BeanUtils.copyProperties(item,ordersDto);
            //根据orderId查询orderDetail数据给ordersDto对象属性赋值
            Long orderId = item.getId();
            List<OrderDetail> orderDetailListByOrderId = this.getOrderDetailListByOrderId(orderId);
            ordersDto.setOrderDetails(orderDetailListByOrderId);
            return ordersDto;
        }).collect(Collectors.toList());


        pageDto.setRecords(ordersDtoList);

        return pageDto;
    }

    /**
     * 用户再来一单
     * @param orderId
     * 前端点击再来一单是直接跳转到购物车的，所以为了避免数据有问题，再跳转之前我们需要把购物车的数据给清除
     * ①通过orderId获取订单明细
     * ②把订单明细的数据的数据塞到购物车表中，不过在此之前要先把购物车表中的数据给清除(清除的是当前登录用户的购物车表中的数据)，
     * 不然就会导致再来一单的数据有问题；
     * (这样可能会影响用户体验，但是对于外卖来说，用户体验的影响不是很大，电商项目就不能这么干了)
     */
    @Override
    public void againSubmit(long orderId) {
        //清空购物车
        shoppingCartService.clean();

        //根据orderId查询得到orderDetail数据
        LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();
        orderDetailLambdaQueryWrapper.eq(OrderDetail::getOrderId,orderId);
        List<OrderDetail> orderDetailList = orderDetailService.list(orderDetailLambdaQueryWrapper);

        //将得到的orderDetail数据存放到shoppingCart购物车入库
        List<ShoppingCart> shoppingCartList=orderDetailList.stream().map((item)->{
            //新建一个shoppingCart对象
            ShoppingCart shoppingCart = new ShoppingCart();
            //将order和order_details表中的数据赋值给购物车对象
            shoppingCart.setName(item.getName());
            shoppingCart.setImage(item.getImage());
            shoppingCart.setDishId(item.getDishId());
            shoppingCart.setDishFlavor(item.getDishFlavor());
            shoppingCart.setNumber(item.getNumber());
            shoppingCart.setAmount(item.getAmount());
            shoppingCart.setSetmealId(item.getSetmealId());
            shoppingCart.setUserId(BaseContext.getCurrentId());
            return shoppingCart;
        }).collect(Collectors.toList());

        //将购物车信息入库
        shoppingCartService.saveBatch(shoppingCartList);
    }
}
