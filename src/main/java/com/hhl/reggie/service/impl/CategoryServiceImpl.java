package com.hhl.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hhl.reggie.common.CustomException;
import com.hhl.reggie.entity.Category;
import com.hhl.reggie.entity.Dish;
import com.hhl.reggie.entity.Setmeal;
import com.hhl.reggie.mapper.CategoryMapper;
import com.hhl.reggie.service.CategoryService;
import com.hhl.reggie.service.DishService;
import com.hhl.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private DishService dishService;

    /**
     * 根据id删除分类，删除之前需要进行判断分类没有关联菜品和套餐
     * @param id
     */
    @Override
    public void remove(Long id) {
        //------------------------查询是否关联菜品--------------------------
        //新建一个条件构照器
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        //执行sql语句
        int count1 = dishService.count(dishLambdaQueryWrapper);
        //查询当前分类是否关联菜品，如果关联，则抛出一个业务异常
        if(count1>0){
            //已经关联菜品，抛出一个业务异常
            throw new CustomException("已经关联菜品，无法删除分类");
        }

        //------------------------查询是否关联套餐--------------------------
        //新建一个条件构照器
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        //执行sql语句
        int count2 = setmealService.count(setmealLambdaQueryWrapper);
        //查询当前分类是否关联套餐，如果关联，则抛出一个业务异常
        if(count2>0){
            //已经关联套餐，抛出一个业务异常
            throw new CustomException("已经关联套餐，无法删除分类");
        }

        //正常删除分类
        super.removeById(id);
    }
}
