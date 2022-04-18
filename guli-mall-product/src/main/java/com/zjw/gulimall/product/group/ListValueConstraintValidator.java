package com.zjw.gulimall.product.group;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-07 10:56
 * @Modifier:
 */
public class ListValueConstraintValidator implements ConstraintValidator<ListValue, Integer>
{
    private Set<Integer> valueSet = new HashSet<>();

    //初始化方法
    @Override
    public void initialize(ListValue constraintAnnotation) {
        //此处的constraintAnnotation.value()便是在使用注解@ListValue(value={0,1})中传入的value
        int[] value = constraintAnnotation.value();
        for(int item : value){
            valueSet.add(item);
        }
    }

    // 判断是否校验成功
    // value:提交过来需要校验的值（传的参），前端传来的值
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return valueSet.contains(value);
    }
}
