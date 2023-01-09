package com.hhl.reggie.dto;

import com.hhl.reggie.entity.Setmeal;
import com.hhl.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
