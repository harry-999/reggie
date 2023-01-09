package com.hhl.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hhl.reggie.dto.DishDto;
import com.hhl.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {
//    新增菜品，同时插入菜品对应的口味数据，需要操作两张表：dish、dish_flavor
    public void saveWithFlavor(DishDto dishDto);

//    根据菜品id，查询菜品基本信息和口味数据，需要操作两张表：dish、dish_flavor
    public DishDto getByIdWithFlavor(Long id);
//    根据前端传输的dishDto对象，修改菜品基本信息和口味信息
    public void updateWithFlavor(DishDto dishDto);
//    根据前端传输的ids和status修改售卖状态
    public void updateDishStatusById(Integer status, List<Long>ids);
//    根据ids来删除对应的菜品
    public void deleteDishByIds(List<Long>ids);
}
