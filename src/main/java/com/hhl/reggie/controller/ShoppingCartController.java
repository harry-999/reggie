package com.hhl.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hhl.reggie.common.BaseContext;
import com.hhl.reggie.common.R;
import com.hhl.reggie.entity.Setmeal;
import com.hhl.reggie.entity.ShoppingCart;
import com.hhl.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加到购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        //先设置用户id,指定当前是哪个用户的购物车数据  因为前端没有传这个id给我们,但是这个id又非常重要（数据库这个字段不能为null）,
        // 所以要想办法获取到,我们在用户登录的时候就已经保存了用户的id
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        // 根据前端调试分析，如果加入购物车的是套餐，则前端请求中的参数dishId为null，如果加入购物车的是菜品，则dish不为null
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if(dishId!=null){
            //购物车为菜品,则直接根据dishId查询ShoppingCart表中的数据
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getDishId,dishId);
        }
        else {
            //购物车为套餐,则根据setmealId查询ShoppingCart中的数据
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());

        }
        //执行sql语句，得到数据
        ShoppingCart shoppingCart1 = shoppingCartService.getOne(shoppingCartLambdaQueryWrapper);

        //如果在shoppingCart表中找不到对应的数据，说明购物车中没有该数据，表示需要添加到购物车中
        if(shoppingCart1==null){
            //此时容易漏  设置数量为1
            shoppingCart.setNumber(1);
            //添加到数据库
            shoppingCartService.save(shoppingCart);
            //容易漏掉 ， 用于返回到R
            shoppingCart1=shoppingCart;
        }
        else{
            //反之如果找到数据，则在购物车中数量加一，数据库中对应的shoppingCart表的数据数量加一
            Integer number = shoppingCart1.getNumber();
            shoppingCart1.setNumber(number+1);
            shoppingCartService.updateById(shoppingCart1);
        }
        return R.success(shoppingCart1);
    }

    /**
     * 减少套餐或者菜品
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        //先设置用户id,指定当前是哪个用户的购物车数据  因为前端没有传这个id给我们,但是这个id又非常重要（数据库这个字段不能为null）,
        // 所以要想办法获取到,我们在用户登录的时候就已经保存了用户的id
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        // 根据前端调试分析，如果加入购物车的是套餐，则前端请求中的参数dishId为null，如果加入购物车的是菜品，则dish不为null
        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper = new LambdaQueryWrapper<>();
        if(dishId!=null){
            //购物车为菜品,则直接根据dishId查询ShoppingCart表中的数据
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getDishId,dishId);
        }
        else {
            //购物车为套餐,则根据setmealId查询ShoppingCart中的数据
            shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());

        }
        //执行sql语句，得到数据(可能是菜品或者是套餐)
        ShoppingCart shoppingCart1 = shoppingCartService.getOne(shoppingCartLambdaQueryWrapper);

        if(shoppingCart1!=null){
            //反之如果找到数据，则在购物车中数量减一，数据库中对应的shoppingCart表的数据数量减一
            Integer number = shoppingCart1.getNumber();
            shoppingCart1.setNumber(number-1);
            //获取减1后的购物车对应数量
            Integer lastNumber = shoppingCart1.getNumber();
            //进行数量合法性的判断
            if(lastNumber>0){
                //正常减1
                shoppingCartService.updateById(shoppingCart1);
            }
            else if (lastNumber==0){
                //表示数量删减后已经为0，则删除shoppongCart中的对应数据（菜品或者是套餐）
                shoppingCartService.removeById(shoppingCart1.getId());
            }
            else {
                return R.error("操作异常");
            }

        }
        return R.success(shoppingCart1);
    }

    /**
     * 获取购物车数据
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        List<ShoppingCart> list = shoppingCartService.list();
        return R.success(list);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        //根据现在用户的id来删除对应的购物车信息
        shoppingCartService.clean();
        return R.success("成功清空购物车");
    }
}
