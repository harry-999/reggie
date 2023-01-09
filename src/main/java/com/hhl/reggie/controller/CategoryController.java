package com.hhl.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhl.reggie.common.R;
import com.hhl.reggie.entity.Category;
import com.hhl.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     * @param category
     * @return
     */
//    @RequestBody的原因是请求中的参数是从请求报文中的json数据得到的
    @PostMapping()
    public R<String> save(@RequestBody Category category){
        log.info("category: {}",category);
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    /**
     * 显示分类分页请求信息
     * @param page
     * @param pageSize
     * @return
     */
//    这里没有@RequestBody的原因是前端的请求中的参数数据是在url中获取没有从请求报文中获取
    @GetMapping("/page")
    public R<Page> page (int page,int pageSize){

        //构造分页构造器（limit）
        Page pageinfo = new Page(page,pageSize);

        //构建条件构造器(用于排序显示)
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Category::getSort);

        //进行分页查询
        categoryService.page(pageinfo,queryWrapper);
//        这里需要返回Page对象是因为前端中需要Page中的record和total属性
        return R.success(pageinfo);
    }

    /**
     * 根据id删除分类信息
     * @param ids
     * @return
     */
    //根据前端代码分析 前端通过携带ids数据 通过delete请求方式
    @DeleteMapping
    public R<String> delete(Long ids){
        log.info("删除的分类， id为：{}",ids);


        //categoryService.removeById(ids);
        //利用自定义删除方法
        categoryService.remove(ids);

        return R.success("分类信息删除成功");
    }

    /**
     * 根据id修改分类信息
     * @param category
     * @return
     */
//    前端中按分类中修改的按钮，会出现回显的效果是因为采用了Vue中的双向绑定
//    <el-input
//    v-model="classData.name"
//    placeholder="请输入分类名称"
//    maxlength="14"
//            />
    @PutMapping()
    public R<String> update(@RequestBody Category category){
        log.info("修改分类信息： {}",category);
        categoryService.updateById(category);
        return R.success("修改分类信息成功");
    }

//    分析前端代码可知，前端vue中需要遍历一个dishList数组，里面存放的是菜品分类的数据，数据中传输的是菜品中的type值
    @GetMapping("list")
    public R<List<Category>> list(Category category){
        //新建一个条件构照器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();

        //添加条件
        queryWrapper.eq(category.getType()!=null,Category::getType,category.getType());
        //添加排序条件
        queryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);

        //查询并且封装成集合
        List<Category> list = categoryService.list(queryWrapper);

        return R.success(list);
    }
}
