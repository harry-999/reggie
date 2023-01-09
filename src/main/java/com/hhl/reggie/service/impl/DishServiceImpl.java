package com.hhl.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hhl.reggie.common.CustomException;
import com.hhl.reggie.dto.DishDto;
import com.hhl.reggie.entity.Dish;
import com.hhl.reggie.entity.DishFlavor;
import com.hhl.reggie.mapper.DishMapper;
import com.hhl.reggie.service.DishFlavorService;
import com.hhl.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品同时保存对应的口味数据
     * @param dishDto
     */
    @Override
//    开启事务  因为需要操作两个表，所以开启事务管理,保证事务的一致性
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品的基本信息到菜品表dish
        this.save(dishDto);

        //获取dishDto中的口味flavors列表，并且给每一种口味设置菜品id
        //获取菜品id
        Long dishId = dishDto.getId();
        //获取口味列表
        List<DishFlavor> flavors = dishDto.getFlavors();
        //遍历每一个口味并且赋值菜品id
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishId);
        }

        //保存菜品口味数据到菜品口味表中dish_flavor
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 根据id查询菜品信息和口味信息
     * @param id
     * @return
     */
    public DishDto getByIdWithFlavor(Long id){
        //新建一个DishDto对象，用于返回dish基本信息和口味数据
        DishDto dishDto = new DishDto();

        //查询菜品基本信息，从dish表查询，可以直接继承父接口Iservice中的方法
        Dish dish = this.getById(id);

        //复制dish对象的基本信息到dishDto对象中
        BeanUtils.copyProperties(dish,dishDto);

        //根据菜品id查询对应的口味信息，从dish_flavor表中查询
        //创建条件构造器
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();

        //添加条件
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());

        //查询
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);

        //将口味信息赋值到dishDto中并返回
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    /**
     * 根据前端传输的dishDto对象修改菜品信息和口味信息
     * @param dishDto
     */
    @Transactional
    public void updateWithFlavor(DishDto dishDto){
        //用继承父类的方法更新dish基本信息 操作dish表 dishDto继承dish中，所以有dish的id
        this.updateById(dishDto);

        //根据菜品id清理当前菜品对应的口味数据 操作dish_flavor表delete操作,因为Iservice中范型不是DishFlavor，所以要自己写条件构造器
        LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(dishFlavorLambdaQueryWrapper);

        //添加当前菜品对应的口味数据 操作dish_flavor表insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();
        //这里如果原本菜品信息中没有添加口味设置，则表示没有经历过添加dish_id的操作，所以需要添加dish_id（细节）
        for (DishFlavor flavor : flavors) {
            //将disid添加到flavor集合中
            flavor.setDishId(dishDto.getId());
        }
        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 根据ids修改status售卖状态
     * @param status
     * @param ids
     */
    @Override
    public void updateDishStatusById(Integer status, List<Long> ids) {
        //新建条件构造器，根据id值找到dish表中的对应实体类集合
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(Dish::getId,ids);
        List<Dish> dishes = this.list(dishLambdaQueryWrapper);
        //遍历集合，修改实体类中的status状态
        for (Dish dish : dishes) {
            //判断是否找到实体类
            if(dish!=null){
                //修改实体类status
                dish.setStatus(status);
                //更新dish表
                this.updateById(dish);
            }
        }
    }

    /**
     * 根据ids删除菜品
     * @param ids
     */
    @Override
    @Transactional
    public void deleteDishByIds(List<Long> ids) {
        //判断当前菜品售卖状态status
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<DishFlavor> dishFlavorLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.in(Dish::getId,ids);
        List<Dish> dishes = this.list(dishLambdaQueryWrapper);
        for (Dish dish : dishes) {
            if(dish.getStatus()!=1){
                //如果status为0停止售卖，则可以进行删除
                //先删除菜品基本信息 ---dish表
                this.removeById(dish.getId());
                //删除关联信息 ---dish_flavor表
                dishFlavorLambdaQueryWrapper.eq(DishFlavor::getDishId,dish.getId());
                dishFlavorService.remove(dishFlavorLambdaQueryWrapper);
            }else {
                //如果status为1还在售卖，抛出业务异常
                throw new CustomException("菜品还在售卖中不能删除");
            }
        }







    }
}
