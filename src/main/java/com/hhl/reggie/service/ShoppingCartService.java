package com.hhl.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hhl.reggie.entity.ShoppingCart;

public interface ShoppingCartService extends IService<ShoppingCart> {
    //清空购物车
    public void clean();
}
