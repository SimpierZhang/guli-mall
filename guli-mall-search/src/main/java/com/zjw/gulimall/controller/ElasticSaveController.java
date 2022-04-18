package com.zjw.gulimall.controller;

import com.zjw.common.to.SkuEsModel;
import com.zjw.common.utils.ErrorConstant;
import com.zjw.common.utils.R;
import com.zjw.gulimall.service.ElasticSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-15 17:27
 * @Modifier:
 */
@Slf4j
@RestController
@RequestMapping("/search/save")
public class ElasticSaveController
{
    @Resource
    private ElasticSaveService elasticSaveService;

    //上传数据到es中
    @PostMapping("/product")
    public R uploadProductInfoToEs(@RequestBody List<SkuEsModel> modelList){
        try {
            boolean uploadResult = elasticSaveService.uploadProductInfoToEs(modelList);
            if(uploadResult){
                return R.ok();
            }else
            {
                return R.error(ErrorConstant.ProductErrorEnum.PRODUCT_ERROR_UPLOAD_ENUM.getCode(), ErrorConstant.ProductErrorEnum.PRODUCT_ERROR_UPLOAD_ENUM.getMessage());
            }
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return R.error(ErrorConstant.ProductErrorEnum.PRODUCT_ERROR_UPLOAD_ENUM.getCode(), ErrorConstant.ProductErrorEnum.PRODUCT_ERROR_UPLOAD_ENUM.getMessage());
    }
}
