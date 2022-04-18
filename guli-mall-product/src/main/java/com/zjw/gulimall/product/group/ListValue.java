package com.zjw.gulimall.product.group;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @Author: Zjw
 * @Description: 自定义注解，用于判断传过来的值是否是0或1，其余的数字都属于不符合规范
 * @Create 2021-08-07 10:51
 * @Modifier:
 */
@Documented
@Constraint(validatedBy = {ListValueConstraintValidator.class})
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
public @interface ListValue
{
    //错误提示值一般为校验注解类的全类名.message    ListValue 的全类名
    String message() default "{com.zjw.gulimall.product.group.ListValue.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    //定义注解@ListValue(value={0,1},groups = {AddGroup.class})  value值默认为空数组
    int[] value() default { };
}
