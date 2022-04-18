package com.zjw.gulimall.product.web;

import com.zjw.gulimall.product.entity.CategoryEntity;
import com.zjw.gulimall.product.service.CategoryService;
import com.zjw.gulimall.product.vo.Catalog2Vo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-16 22:35
 * @Modifier:
 */
@Controller
public class IndexController
{
    @Resource
    private CategoryService categoryService;

    @GetMapping(value = {"/", "/index.html"})
    public String getIndex(Model model){
        //1.查出商品一级分类
        List<CategoryEntity> categoryEntityList = categoryService.getLevel1CategoryList();
        Long end = System.currentTimeMillis();
        model.addAttribute("categorys", categoryEntityList);
        return "index";
    }

    //返回一级菜单下的二级菜单以及三级菜单
    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catalog2Vo>> getCatalogJson(){
        //每次查询时都要进行数据库的查询，容易损耗性能，考虑将其加入redis缓存
        Map<String, List<Catalog2Vo>> catalogJsonMap = categoryService.getCatalogJson();
        return catalogJsonMap;
    }

    @ResponseBody
    @GetMapping("/hello")
    public String hello(){
        return "hello";
    }
}
