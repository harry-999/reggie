package com.hhl.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhl.reggie.common.R;
import com.hhl.reggie.dto.DishDto;
import com.hhl.reggie.entity.Category;
import com.hhl.reggie.entity.Dish;
import com.hhl.reggie.entity.DishFlavor;
import com.hhl.reggie.service.CategoryService;
import com.hhl.reggie.service.DishFlavorService;
import com.hhl.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

//    根据调试窗口封装自定义的dto对象来接受传输过来的参数
    @PostMapping()
    public R<String> save(@RequestBody DishDto dishDto){
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /**
     * 菜品信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
//    前端分析：菜品信息中的菜品分类显示的是菜品名称不是id，但是dish表中只有菜品id，
//    所以要构建一个dto对象继承dish对象，同时还有菜品名称属性，并且要dto拷贝dish对象的所有属性并且添加菜品名称
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){

        //构造分页构造器对象
        Page<Dish> dishPage = new Page<>(page,pageSize);
        //构建一个dishDto的范型的分页构造器对象
        Page<DishDto> dishDtoPage = new Page<>(page,pageSize);

        //构造条件构造器对象
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();

        //添加过滤条件
        queryWrapper.eq(name!=null,Dish::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getSort);
        //执行分页查询
        dishService.page(dishPage,queryWrapper);

        //将得到的Dish范型中的page对象dishPage拷贝到DishDto范型的page对象dishDtoPage中，不用拷贝records属性，因为还需要处理
        BeanUtils.copyProperties(dishPage,dishDtoPage,"records");
        //因为dishPage中的records属性是一个Dish范型的集合，要处理成DishDto范型的集合
        //将dishPage中的records属性中的dishList集合拷贝到dishDtoPage中recores属性中的dishDtoList集合中
        ArrayList<DishDto> dishDtoList = new ArrayList<>();
//        List<DishDto> dishDtoList = null;
        //获取dishPage中的records属性Dish范型集合
        List<Dish> dishList = dishPage.getRecords();
        //遍历dishList集合并且把每一条集合修改成dishDto对象，设置对应的菜品名称
        for (Dish dish : dishList) {
            //新建一个dishDto对象用来储存新的集合
            DishDto dishDto = new DishDto();
            //将集合中的每一个元素拷贝到dishDto中
            BeanUtils.copyProperties(dish,dishDto);
            //查询每一个集合中的id
            Long categoryId = dish.getCategoryId();
            //根据id查询对应的菜品名，要查询的是category表
            Category categoryById = categoryService.getById(categoryId);
            if(categoryById!=null){
                String categoryName = categoryById.getName();
                //将菜品名设置给新的dishDto对象中
                dishDto.setCategoryName(categoryName);
            }
            //将dishDto每一条数据存放到集合dishDtoList中
            dishDtoList.add(dishDto);
        }

        //将修改后的recoreds属性中的值放入到新的page对象中
        dishDtoPage.setRecords(dishDtoList);

        return R.success(dishDtoPage);
    }

    /**
     * 根据id获取菜品基本信息和口味数据
     * @param id
     * @return
     */
    //根据前端调试窗口分析，该请求中的参数直接写在URl中，没有键接收，所以用@PathVariable接收
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 更新菜品信息和口味信息
     * @param dishDto
     * @return
     */
    @PutMapping()
    public R<String> update(@RequestBody DishDto dishDto){
        //修改菜品基本信息和口味信息
        dishService.updateWithFlavor(dishDto);
        return R.success("修改菜品信息成功");
    }

    /**
     * 根据条件查询相应的菜品数据
     * @param dish
     * @return
     */
    //根据前端调试中可知，传给服务端的参数数据是菜品id，但是考虑代码的稳定性，建议使用dish对象接收，这样，只要传dish属性值都能接收
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        //新建条件构造器
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //添加菜品id查询条件
        dishLambdaQueryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        //添加条件查询状态为1（起售状态）的菜品
        dishLambdaQueryWrapper.eq(Dish::getStatus,1);
        //添加排序条件
        dishLambdaQueryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        //执行查询
        List<Dish> list = dishService.list(dishLambdaQueryWrapper);

        //进行集合的泛型转化
        List<DishDto> dishDtoList = list.stream().map((item) ->{
            DishDto dishDto = new DishDto();
            //为一个新的对象赋值，一定要考虑你为它赋过几个值，否则你自己都不知道就返回了null的数据
            //为dishDto对象的基本属性拷贝
            BeanUtils.copyProperties(item,dishDto);
            //给dishDto设置菜品名称
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if (category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            //为dishdto赋值flavors属性
            //当前菜品的id
            Long dishId = item.getId();
            //创建条件查询对象
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            //select * from dish_flavor where dish_id = ?
            //这里之所以使用list来条件查询那是因为同一个dish_id 可以查出不同的口味出来,就是查询的结果不止一个
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);

            return dishDto;
        }).collect(Collectors.toList());

        return R.success(dishDtoList);

    }

    /**
     * 根据ids和status修改菜品状态
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("status/{status}")
    public R<String> status(@PathVariable("status") Integer status,@RequestParam List<Long>ids){
        dishService.updateDishStatusById(status,ids);
        return R.success("成功修改菜品状态");
    }

    /**
     * 根据ids删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping()
    public R<String> delete(@RequestParam List<Long>ids){

        dishService.deleteDishByIds(ids);
        return R.success("成功删除菜品");
    }


}
