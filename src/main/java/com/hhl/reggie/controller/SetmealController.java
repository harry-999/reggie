package com.hhl.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhl.reggie.common.R;
import com.hhl.reggie.dto.DishDto;
import com.hhl.reggie.dto.SetmealDto;
import com.hhl.reggie.entity.Category;
import com.hhl.reggie.entity.Dish;
import com.hhl.reggie.entity.Setmeal;
import com.hhl.reggie.entity.SetmealDish;
import com.hhl.reggie.service.CategoryService;
import com.hhl.reggie.service.DishService;
import com.hhl.reggie.service.SetmealDishService;
import com.hhl.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/setmeal")
/**
 * 套餐管理
 */
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private DishService dishService;

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info(setmealDto.toString());
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    /**
     * 套餐分页管理
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //构建分页构照器对象
        Page<Setmeal> setmealPage = new Page<>(page,pageSize);
        Page<SetmealDto> setmealDtoPage = new Page<>(page,pageSize);

        //构建条件查询出setmealPage对象
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.like(name!=null,Setmeal::getName,name);
        setmealLambdaQueryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(setmealPage,setmealLambdaQueryWrapper);

        //将查询得到的setmealPage对象复制给setmealDtoPage对象，除了recoreds属性，因为recoreds属性要改成setmealDto范型
        BeanUtils.copyProperties(setmealPage,setmealDtoPage,"recoreds");

        //修改setmealPage中的recoreds属性，并且复制给setmealDtoPage对象的recoreds属性
        //获取setmealPage中recoreds属性值
        List<Setmeal> records = setmealPage.getRecords();
        //新建一个list集合用来作为给setmealDtoPage的recoreds赋值
        ArrayList<SetmealDto> setmealDtoPageRecords = new ArrayList<>();
        for (Setmeal record : records) {
            //新键一个setmealDto对象，将record的基本属性复制给它
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(record,setmealDto);
            //根据record中的分类id查询查询套餐名称，并将其设置给setmealDto对象中
            //获取record中的id
            Long categoryId = record.getCategoryId();
            //根据菜品id查询菜品名称
            if(categoryId!=null){
                Category category = categoryService.getById(categoryId);
                //获取菜品名称
                String categoryName = category.getName();
                //赋值给setmealDto对象中
                setmealDto.setCategoryName(categoryName);
            }
            //添加到setmealDtoPageRecords中
            setmealDtoPageRecords.add(setmealDto);
        }
        setmealDtoPage.setRecords(setmealDtoPageRecords);

        return R.success(setmealDtoPage);
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    //根据前端调试分析，前端发送的批量删除数据是ids，多个id值，所以要用List接收，同时要用@RequestParam批注映射参数
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        setmealService.removeWithDish(ids);
        return R.success("套餐删除成功");
    }

    /**
     * 根据ids和status修改套餐状态
     * @param status
     * @param ids
     * @return
     */
    //根据前端调试分析，传给服务端的请求 http://localhost:8080/setmeal/status/0?ids=1610235412063592450,1610234488956002305
    @PostMapping("/status/{status}")
    public R<String> status( @PathVariable("status") Integer status,@RequestParam List<Long> ids){
        setmealService.updateSetmealStatusById(status,ids);
        return R.success("售卖状态修改成功");
    }

    /**
     * 根据菜品id和售卖状态返回套餐数据
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        //新建条件构造器
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        //寻找菜品id和status为售卖状态的套餐
        setmealLambdaQueryWrapper.eq(setmeal.getCategoryId()!=null,Setmeal::getCategoryId,setmeal.getCategoryId());
        setmealLambdaQueryWrapper.eq(setmeal.getStatus()!=null,Setmeal::getStatus,setmeal.getStatus());
        List<Setmeal> list = setmealService.list(setmealLambdaQueryWrapper);
        return R.success(list);
    }

    /**
     * 根据套餐id展示套餐内容
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getSetmealDto(@PathVariable Long id){
        SetmealDto setmealDto = setmealService.getDate(id);
        return R.success(setmealDto);
    }

    /**
     * 移动端点击套餐图片查看套餐具体内容
     * 这里返回的是dto 对象，因为前端需要copies这个属性 回显出菜品数量
     * 前端主要要展示的信息是:套餐中菜品的基本信息，图片，菜品描述，以及菜品的份数
     * @param setmealId
     * @return
     */
    @GetMapping("/dish/{setmealId}")
    public R<List<DishDto>> list(@PathVariable Long setmealId){

        //获取套餐中的所有的菜品 操作setmealdish表
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.eq(setmealId!=null,SetmealDish::getSetmealId,setmealId);
        List<SetmealDish> setmealDishes = setmealDishService.list(setmealDishLambdaQueryWrapper);

        //新建一个dishDtos列表存放菜品信息
        ArrayList<DishDto> dishDtos = new ArrayList<>();

        //将所有的菜品信息复制到新建的DishDto对象中
        for (SetmealDish setmealDish : setmealDishes) {
            DishDto dishDto = new DishDto();
            //拷贝套餐中菜品的信息
            BeanUtils.copyProperties(setmealDish,dishDto);
            //拷贝菜品具体信息 比如菜品描述，菜品图片等菜品的基本信息
            //先获取菜品的id
            Long dishId = setmealDish.getDishId();
            //根据dishId查询dish表中的菜品
            Dish dish = dishService.getById(dishId);
            //将dish复制到新建的dto对象
            BeanUtils.copyProperties(dish,dishDto);

            //存放到dishDtos列表
            dishDtos.add(dishDto);
        }
        return R.success(dishDtos);
    }

    /**
     * 修改套餐
     * @param setmealDto
     * @return
     */
    @PutMapping()
    public R<String> update(@RequestBody SetmealDto setmealDto){
        if(setmealDto==null){
            return R.error("请求异常");
        }
        if(setmealDto.getSetmealDishes()==null){
            return R.error("套餐中没有菜品，请添加套餐");
        }
        //将原本的setmealDish数据删掉，再重新添加
        //先获取前端中参数setmealDto中的setmealdish对象列表
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //找到setmeal_dish中的setmealid（通过前端传的setmealDto对象中的id作为setmealid，根据此删除setmealdish表中的dish）
        LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealDishLambdaQueryWrapper.eq(SetmealDish::getSetmealId,setmealDto.getId());
        setmealDishService.remove(setmealDishLambdaQueryWrapper);

        //为setmealDish添加修改的菜品数据
        for (SetmealDish setmealDish : setmealDishes) {
            //记得给修改的数据添加setmealid，因为前端数据中setmealDishes中没有设置setmealid
            setmealDish.setSetmealId(setmealDto.getId());
        }
        setmealDishService.saveBatch(setmealDishes);
        //为setmeal表更新基本数据
        setmealService.updateById(setmealDto);
        return R.success("套餐修改成功");
    }
}
