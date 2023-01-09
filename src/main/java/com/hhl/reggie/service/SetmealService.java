package com.hhl.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hhl.reggie.dto.SetmealDto;
import com.hhl.reggie.entity.Setmeal;

import java.util.List;


public interface SetmealService extends IService<Setmeal> {

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * 根据id删除套餐和套餐关联的菜品
     * @param ids
     */
    public void removeWithDish(List<Long> ids);

    /**
     * 根据id修改套餐的status
     * @param status
     * @param ids
     */
    public void updateSetmealStatusById(Integer status,List<Long>ids);

    /**
     * 根据套餐id获取套餐中的具体信息包括菜品
     * @param id
     */
    public SetmealDto getDate(Long id);
}
