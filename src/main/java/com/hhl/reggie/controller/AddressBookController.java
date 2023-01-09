package com.hhl.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hhl.reggie.common.BaseContext;
import com.hhl.reggie.common.R;
import com.hhl.reggie.entity.AddressBook;
import com.hhl.reggie.service.AddressBookService;
import jdk.nashorn.internal.ir.CallNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/addressBook")
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;

    /**
     * 查询用户的全部地址
     * @return
     */
    //根据前端代码分析，返回的数据应该是list列表存放的是addressbook信息
    @GetMapping("/list")
    public R<List<AddressBook>> list(){
        //根据在域对象中的获取用户信息，并且给传入的形参实体类赋值
        Long userId=BaseContext.getCurrentId();
        //条件构造器，根据用户id选对应的addressbook值 操作addressbook表
        LambdaQueryWrapper<AddressBook> addressBookLambdaQueryWrapper = new LambdaQueryWrapper<>();
        addressBookLambdaQueryWrapper.eq(userId!=null,AddressBook::getUserId,userId);
        List<AddressBook> addressBooks = addressBookService.list(addressBookLambdaQueryWrapper);
        return R.success(addressBooks);
    }

    /**
     * 根据id查询地址（回显）
     * @return
     */
    //根据前端代码分析 传入的形参应该是通过url 返回值设置为Addressbook实体类，因为此时需要回显功能，所以我用的是AddressBook实体类作为返回对象，
    @GetMapping("/{id}")
    public R<AddressBook> get(@PathVariable Long id){
        //根据id查询addressbook
        AddressBook addressBookServiceById = addressBookService.getById(id);
        if(addressBookServiceById==null){
            return R.error("没有找到该对象");
        }
        else{
            return R.success(addressBookServiceById);
        }
    }

    /**
     * 新增地址
     * @return
     */
    @PostMapping()
    public R<AddressBook> save(@RequestBody AddressBook addressBook){
        //操作addressbook表新增数据
        //给addressBook添加id
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookService.save(addressBook);
//        log.info("addressBook {}",addressBook);
        return R.success(addressBook);
    }
    /**
     * 修改更新地址
     * @return
     */
    //前端调试中的addressbook为修改的地址联系信息
    @PutMapping()
    public R<String> update(@RequestBody AddressBook addressBook){
        if(addressBook==null){
            return R.error("请求异常");
        }
        addressBookService.updateById(addressBook);
        return R.success("成功修改地址信息");
    }

    /**
     * 设置默认地址
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    public R<String> setDefault(@RequestBody AddressBook addressBook){
        //将全部该用户的地址设置is_default为0
        LambdaQueryWrapper<AddressBook> addressBookLambdaQueryWrapper2 = new LambdaQueryWrapper<>();
        addressBookLambdaQueryWrapper2.eq(AddressBook::getUserId,BaseContext.getCurrentId());
        List<AddressBook> list = addressBookService.list(addressBookLambdaQueryWrapper2);
        for (AddressBook book : list) {
            book.setIsDefault(0);
            addressBookService.updateById(book);
        }
        //根据点击地址id找到所需要设置默认地址的sql信息，并且设置is_default为1(默认地址为1)
        LambdaQueryWrapper<AddressBook> addressBookLambdaQueryWrapper1 = new LambdaQueryWrapper<>();
        addressBookLambdaQueryWrapper1.eq(addressBook.getId()!=null,AddressBook::getId,addressBook.getId());
        AddressBook addressBook1 = addressBookService.getOne(addressBookLambdaQueryWrapper1);
        addressBook1.setIsDefault(1);
        addressBookService.updateById(addressBook1);
        return R.success("成功修改默认地址");
    }

    /**
     * 获取地址栏中地址信息
     * @return
     */
    @GetMapping("/default")
    public R<AddressBook> getdefault(){
        LambdaQueryWrapper<AddressBook> addressBookLambdaQueryWrapper = new LambdaQueryWrapper<>();
        addressBookLambdaQueryWrapper.eq(AddressBook::getIsDefault,1);
        addressBookLambdaQueryWrapper.eq(AddressBook::getUserId,BaseContext.getCurrentId());
        AddressBook addressBookServiceOne = addressBookService.getOne(addressBookLambdaQueryWrapper);
        return R.success(addressBookServiceOne);
    }
}
