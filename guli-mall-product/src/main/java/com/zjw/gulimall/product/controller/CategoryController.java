package com.zjw.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zjw.gulimall.product.entity.CategoryEntity;
import com.zjw.gulimall.product.service.CategoryService;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.R;



/**
 * 商品三级分类
 *
 * @author simpier
 * @email simpier@gmail.com
 * @date 2021-07-31 17:10:36
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;


    @RequestMapping("/list/tree")
    public R treeList(){
        List<CategoryEntity> categoryEntities = categoryService.queryCategoryForTree();
        return new R().put("data", categoryEntities);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("product:category:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = categoryService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    //@RequiresPermissions("product:category:info")
    public R info(@PathVariable("catId") Long catId){
		CategoryEntity category = categoryService.getById(catId);
		//数据统一命名为data,这个和前端要保持一致
        return R.ok().put("data", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("product:category:save")
    public R save(@RequestBody CategoryEntity category){
        boolean saveSuccess = categoryService.save(category);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("product:category:update")
    public R update(@RequestBody CategoryEntity category){
		categoryService.updateById(category);

        return R.ok();
    }

    //批量更新
    @RequestMapping("/update/sort")
    public R updateSort(@RequestBody CategoryEntity[] categoryEntities){
        categoryService.updateBatchById(Arrays.asList(categoryEntities));
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] catIds){
        //删除之前需要判断待删除的菜单是否被别的地方所引用
		categoryService.removeByIds(Arrays.asList(catIds));
        return R.ok();
    }

}
