package com.zjw.gulimall.product.controller.controllerBK;

import com.zjw.common.utils.R;
import com.zjw.gulimall.product.entity.CategoryEntity;
import com.zjw.gulimall.product.service.CategoryService;
import org.omg.PortableInterceptor.ObjectReferenceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: Zjw
 * @Description: 商品控制器的备份页面
 * @Create 2022-04-02 19:10
 * @Modifier:
 */
@RestController
@RequestMapping("product/category/bk")
public class CategoryControllerBK
{
    @Autowired
    @Qualifier("categoryServiceBK")
    private CategoryService categoryService;

    @GetMapping("/list/tree")
    public R getCategoryTree(){
        List<CategoryEntity> list = categoryService.queryCategoryForTree();
        return R.ok().put("data", list);
    }
}
