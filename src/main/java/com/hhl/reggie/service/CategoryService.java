package com.hhl.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hhl.reggie.entity.Category;

public interface CategoryService extends IService<Category> {
//    根据id删除分类，删除之前需要进行判断分类没有关联菜品和套餐
    public void remove(Long id);
}
