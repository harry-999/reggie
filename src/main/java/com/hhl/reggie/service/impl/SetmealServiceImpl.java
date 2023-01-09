package com.hhl.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hhl.reggie.common.CustomException;
import com.hhl.reggie.dto.SetmealDto;
import com.hhl.reggie.entity.Setmeal;
import com.hhl.reggie.entity.SetmealDish;
import com.hhl.reggie.mapper.SetmealMapper;
import com.hhl.reggie.service.SetmealDishService;
import com.hhl.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;
    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作setmeal表，执行insert操作
        this.save(setmealDto);

        //将得到的setmealDto中的setmealDishes遍历，添加套餐的id
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        for (SetmealDish setmealDish : setmealDishes) {
            setmealDish.setSetmealId(setmealDto.getId());
        }
        //保存套餐和菜品的关联信息，操作setmeal_dish表，执行insert操作
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 根据id删除套餐和套餐关联的菜品
     * @param ids
     */
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        //查询套餐状态，确定是否可以删除
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.in(Setmeal::getId,ids);
        setmealLambdaQueryWrapper.eq(Setmeal::getStatus,1);
        //查询用户选取批量删除的套餐中是否有不可删除选项
        int count = super.count(setmealLambdaQueryWrapper);

        //如果不能删除，则抛出一个业务异常
        if(count>0){
            throw new ClassCastException("套餐正在售卖中，不能删除");
        }

        //如果可以删除，先删除套餐表中的数据--setmeal
        this.removeByIds(ids);
        //删除关系表中的数据--setmeal_dish 为什么不能直接用setmealDishService中的removeByides，原因此时的ids不是关系表setmeal_dish中的主键
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(setmealDishLambdaQueryWrapper);
    }


    /**
     * 根据id修改status
     * @param status
     * @param ids
     */
    @Override
    public void updateSetmealStatusById(Integer status, List<Long> ids) {
        //新建条件构照器
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //根据id条件在setmeal表中寻找对应的setmeal，并且更新setmeal表
        setmealLambdaQueryWrapper.in(ids!=null,Setmeal::getId,ids);
        List<Setmeal> setmeals = this.list(setmealLambdaQueryWrapper);

        //遍历setmeals 将setmeal类中的status修改
        for (Setmeal setmeal : setmeals) {
            //需要判断是否存在该实体类，优化
            if(setmeal!=null){
                setmeal.setStatus(status);
                //更新setmeal中的实体
                this.updateById(setmeal);
            }
        }

    }

    /**
     * 根据套餐id获取套餐中的具体信息包括菜品
     * @param id
     */
    @Override
    public SetmealDto getDate(Long id) {
        //根据套餐id条件构造器查询setmeal中的基本信息 setmeal表
        Setmeal setmeal = this.getById(id);
//        //并且设置setmeal中的code为1，根据端代码可知，只有code为1才能显示在页面中
//        setmeal.setCode("1");
        //将查询到的信息复制给新的setmealdto对象中
        SetmealDto setmealDto = new SetmealDto();

        if (setmeal!=null){
            //复制setmeal对象基本信息给setmealdto
            BeanUtils.copyProperties(setmeal,setmealDto);
            //查询菜品 关联表setmead_dish
            LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
            setmealDishLambdaQueryWrapper.eq(id!=null,SetmealDish::getSetmealId,id);
            List<SetmealDish> dishes = setmealDishService.list(setmealDishLambdaQueryWrapper);
            setmealDto.setSetmealDishes(dishes);
            return setmealDto;
        }
        return null;
    }
}
