package com.hhl.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hhl.reggie.entity.Employee;
import com.hhl.reggie.mapper.EmployeeMapper;
import com.hhl.reggie.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class   EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
