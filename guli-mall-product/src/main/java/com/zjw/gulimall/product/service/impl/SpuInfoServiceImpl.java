package com.zjw.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.zjw.common.constant.ProductConstant;
import com.zjw.common.to.*;
import com.zjw.common.utils.R;
import com.zjw.gulimall.product.entity.*;
import com.zjw.gulimall.product.feign.CouponFeignService;
import com.zjw.gulimall.product.feign.SearchFeignService;
import com.zjw.gulimall.product.feign.WareFeignService;
import com.zjw.gulimall.product.service.*;
import com.zjw.gulimall.product.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.Query;

import com.zjw.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;


@Slf4j
@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService
{

    @Resource
    private SpuInfoDescService spuInfoDescService;
    @Resource
    private SpuImagesService spuImagesService;
    @Resource
    private AttrService attrService;
    @Resource
    private ProductAttrValueService productAttrValueService;
    @Resource
    private SkuInfoService skuInfoService;
    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Resource
    private SkuImagesService skuImagesService;
    @Resource
    private CouponFeignService couponFeignService;
    @Resource
    private WareFeignService wareFeignService;
    @Resource
    private BrandService brandService;
    @Resource
    private CategoryService categoryService;
    @Resource
    private SearchFeignService searchFeignService;


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> queryWrapper = new QueryWrapper<>();
        //根据条件查找
        String cond = (String) params.get("key");
        if (StringUtils.isNotBlank(cond)) {
            queryWrapper.eq("id", cond).or().like("spu_name", cond);
        }
        String catalogId = (String) params.get("catalogId");
        if (StringUtils.isNotBlank(catalogId) && !Long.valueOf(catalogId).equals(0L)) {
            queryWrapper.eq("catalog_id", catalogId);
        }
        String brandId = (String) params.get("brandId");
        if (StringUtils.isNotBlank(brandId) && !Long.valueOf(brandId).equals(0L)) {
            queryWrapper.eq("brand_id", brandId);
        }
        String status = (String) params.get("status");
        if (StringUtils.isNotBlank(status)) {
            queryWrapper.eq("publish_status", status);
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public boolean saveProduct(ProductInfoVo productInfoVo) {
        //1.保存SpuInfo>>SpuInfoEntity>>pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(productInfoVo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        spuInfoEntity.setPublishStatus(ProductConstant.PublishStatusEnum.PUBLISH_STATUS_NEW.getCode());
        save(spuInfoEntity);
        //因为保存之后会自动填充主键，所以可以得到spuId
        Long spuId = spuInfoEntity.getId();
        Long catalogId = spuInfoEntity.getCatalogId();
        Long brandId = spuInfoEntity.getBrandId();
        //2.保存SpuInfoDesc>>SpuInfoDescEntity>>pms_spu_info_desc
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuId);
        StringBuilder sb = new StringBuilder();
        //将描述信息封装成一个String,多个之间用逗号隔开
        productInfoVo.getDecript().forEach(descStr -> {
            sb.append(descStr).append(";");
        });
        spuInfoDescEntity.setDecript(sb.toString());
        spuInfoDescService.save(spuInfoDescEntity);


        //3.保存SpuImagesEntity>>pms_spu_images
        List<String> images = productInfoVo.getImages();
        if (images != null && images.size() > 0) {
            List<SpuImagesEntity> spuImageEntityList = images.stream().filter(StringUtils::isNotBlank).map(img -> {
                SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
                spuImagesEntity.setSpuId(spuId);
                spuImagesEntity.setImgUrl(img);
                spuImagesEntity.setImgSort(0);
                return spuImagesEntity;
            }).collect(Collectors.toList());
            if (spuImageEntityList.size() > 0)
                spuImagesService.saveBatch(spuImageEntityList);
        }


        //4.远程调用coupon服务>>保存SpuBoundsEntity>>gulimall_sms.sms_spu_bounds
        SpuBoundsEntityTo spuBoundsEntityTo = new SpuBoundsEntityTo();
        BeanUtils.copyProperties(productInfoVo.getBounds(), spuBoundsEntityTo);
        spuBoundsEntityTo.setSpuId(spuId);
        spuBoundsEntityTo.setWork(1);
        R r1 = couponFeignService.saveBounds(spuBoundsEntityTo);

        //5.保存ProductAttrValueEntity>>pms_product_attr_value
        List<BaseAttrs> baseAttrs = productInfoVo.getBaseAttrs();
        //避免循环中查数据
        List<Long> attrIdList = baseAttrs.stream().map(BaseAttrs::getAttrId).collect(Collectors.toList());
        List<AttrEntity> attrEntityList = attrService.listByIds(attrIdList);

        List<ProductAttrValueEntity> productAttrValueEntityList = baseAttrs.stream().map(attr -> {
            ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
            //首先根据attrId查出一些相应的信息将其复制到productAttrValueEntity，此处未查到
            List<AttrEntity> attrEntities
                    = attrEntityList.stream().filter(a -> a.getAttrId().equals(attr.getAttrId())).collect(Collectors.toList());
            if (attrEntities.size() > 0) {
                BeanUtils.copyProperties(attrEntities.get(0), productAttrValueEntity);
            }
            //然后再将传过来的属性复制到productAttrValueEntity
            productAttrValueEntity.setSpuId(spuId);
            if (attr != null)
                BeanUtils.copyProperties(attr, productAttrValueEntity);
            return productAttrValueEntity;
        }).collect(Collectors.toList());
        if (productAttrValueEntityList.size() > 0) {
            productAttrValueService.saveBatch(productAttrValueEntityList);
        }


        //6.保存skus
        List<Skus> skusList = productInfoVo.getSkus();
        skusList.forEach(item -> {
            //6.1 保存SkuInfoEntity>>pms_sku_info>>然后可以获取skuId，从而传递给后续
            SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
            //设置默认图片
            item.getImages().forEach(img -> {
                if (img.getDefaultImg() == 1) {
                    skuInfoEntity.setSkuDefaultImg(img.getImgUrl());
                }
            });
            BeanUtils.copyProperties(item, skuInfoEntity);
            skuInfoEntity.setSpuId(spuId);
            skuInfoEntity.setCatalogId(catalogId);
            skuInfoEntity.setBrandId(brandId); //price未加进来
            skuInfoService.save(skuInfoEntity);
            Long skuId = skuInfoEntity.getSkuId();

            //6.2 保存SkuSaleAttrValueEntity>>pms_sku_sale_attr_value
            List<Attr> attrList = item.getAttr();
            List<SkuSaleAttrValueEntity> skuSaleAttrValueEntityList = attrList.stream().map(attr -> {
                SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                skuSaleAttrValueEntity.setSkuId(skuId);
                return skuSaleAttrValueEntity;
            }).collect(Collectors.toList());
            skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntityList);


            //6.3 保存SkuImagesEntity>>pms_sku_images
            List<Images> skuImageList = item.getImages();
            List<SkuImagesEntity> skuImagesEntityList = skuImageList.stream().filter(img -> StringUtils.isNotBlank(img.getImgUrl())).map(skuImage -> {
                SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                BeanUtils.copyProperties(skuImage, skuImagesEntity);
                skuImagesEntity.setSkuId(skuId);
                return skuImagesEntity;
            }).collect(Collectors.toList());
            skuImagesService.saveBatch(skuImagesEntityList);


            //6.4 远程调用coupon服务>>保存SkuLadderEntity>>gulimall_sms.sms_sku_ladder
            if (item.getFullCount() != 0) {
                SkuLadderEntityTo skuLadderEntityTo = new SkuLadderEntityTo();
                skuLadderEntityTo.setFullCount(item.getFullCount());
                skuLadderEntityTo.setDiscount(item.getDiscount());
                skuLadderEntityTo.setSkuId(skuId);
                skuLadderEntityTo.setAddOther(item.getCountStatus());
                R r2 = couponFeignService.saveLadder(skuLadderEntityTo);
            }


            if (!item.getFullPrice().equals(new BigDecimal(0))) {
                //6.5 远程调用coupon服务>>保存SkuFullReductionEntity>>gulimall_sms.sms_sku_full_reduction
                SkuFullReductionEntityTo skuFullReductionEntityTo = new SkuFullReductionEntityTo();
                skuFullReductionEntityTo.setFullPrice(item.getFullPrice());
                skuFullReductionEntityTo.setReducePrice(item.getReducePrice());
                skuFullReductionEntityTo.setAddOther(item.getPriceStatus());
                skuFullReductionEntityTo.setSkuId(skuId);
                couponFeignService.saveFullReduction(skuFullReductionEntityTo);
            }


            //6.6 远程调用coupon服务>>保存MemberPriceEntity>>gulimall_sms.sms_member_price
            List<MemberPrice> memberPriceList = item.getMemberPrice();
            List<MemberPriceEntityTo> memberPriceEntityToList = memberPriceList.stream().filter(m -> !m.getPrice().equals(new BigDecimal(0))).map(memberPrice -> {
                MemberPriceEntityTo memberPriceEntityTo = new MemberPriceEntityTo();
                memberPriceEntityTo.setSkuId(skuId);
                memberPriceEntityTo.setMemberLevelId(memberPrice.getId());
                memberPriceEntityTo.setMemberLevelName(memberPrice.getName());
                memberPriceEntityTo.setMemberPrice(memberPrice.getPrice());
                return memberPriceEntityTo;
            }).collect(Collectors.toList());
            R r4 = couponFeignService.saveBatchMemberPrice(memberPriceEntityToList);

        });

        return true;
    }

    @Override
    @Transactional
    public boolean upProduct(Long spuId) {
        //1.根据spuId查出对应的skuInfo
        List<SkuInfoEntity> skuInfoList = skuInfoService.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", spuId));
        if (skuInfoList == null) return false;

        //2 远程调用库存服务进行对应商品的库存查询
        List<Long> skuIdList = skuInfoList.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());
        Map<Long, Boolean> wareSkuVoMap = null;
        try {
            R stockResult = wareFeignService.listSkuStock(skuIdList);
            List<WareSkuVo> wareSkuVoList = stockResult.getData(new TypeReference<List<WareSkuVo>>()
            {
            });
            //这样封装的好处可以不用遍历
            wareSkuVoMap = wareSkuVoList.stream().collect(Collectors.toMap(WareSkuVo::getSkuId, WareSkuVo::isHasStock));
        }
        catch (Exception e) {
            log.error("远程调用仓库服务失败>>{}", e);
        }

        //3.封装skuInfo以及库存信息到esModel中
        //将skuInfo中没有而esModel中需要的数据进行处理
        //需要特殊处理的数据
        /**
         *     private boolean hasStock;
         *     private Long hotScore;
         *     private String brandName;
         *     private String brandImg;
         *     private String catalogName;
         *      @Data
         *     public static class Attr{
         *         private Long attrId;
         *         private String attrName;
         *         private String attrValue;
         *     }
         */
        //封装属性，由于属性是通用的，所以不用放到循环中查询数据库
        List<ProductAttrValueEntity> attrValueEntityList = productAttrValueService.listQueryAttr(spuId);
        List<SkuEsModel.Attr> attrList = attrValueEntityList.stream().map(pa -> {
            SkuEsModel.Attr attr = new SkuEsModel.Attr();
            attr.setAttrId(pa.getAttrId());
            attr.setAttrName(pa.getAttrName());
            attr.setAttrValue(pa.getAttrValue());
            return attr;
        }).collect(Collectors.toList());
        Map<Long, Boolean> finalWareSkuVoMap = wareSkuVoMap;
        List<SkuEsModel> esModelList = skuInfoList.stream().map(sku -> {
            SkuEsModel esModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, esModel);
            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());
            if (finalWareSkuVoMap != null && finalWareSkuVoMap.containsKey(esModel.getSkuId())) {
                esModel.setHasStock(finalWareSkuVoMap.get(esModel.getSkuId()));
            }
            else {
                esModel.setHasStock(false);
            }
            //TODO hotScore应该后期再进行计算，此处暂时设为0
            esModel.setHotScore(0L);
            //根据brandId和catalogId查询出相应的信息并进行封装,如果不想在循环中查询数据库，可以借助list和map进行批量查询再封装
            BrandEntity brand = brandService.getById(esModel.getBrandId());
            esModel.setBrandName(brand.getName());
            esModel.setBrandImg(brand.getLogo());
            CategoryEntity category = categoryService.getById(esModel.getCatalogId());
            esModel.setCatalogName(category.getName());
            esModel.setAttrs(attrList);
            return esModel;
        }).collect(Collectors.toList());
        //调用远程search服务将数据上传到es中
        R result = R.ok();
        if (esModelList.size() > 0) {
            result = searchFeignService.uploadProductInfoToEs(esModelList);
        }
        Integer code = (Integer) result.get("code");
        if (code.equals(0)) {
            //上架成功后，更新一下状态
            SpuInfoEntity spuInfoEntity = getById(spuId);
            spuInfoEntity.setPublishStatus(ProductConstant.PublishStatusEnum.PUBLISH_STATUS_UP.getCode());
            updateById(spuInfoEntity);
        }
        return code.equals(0);
    }

    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        SkuInfoEntity skuInfoEntity = skuInfoService.getById(skuId);
        if(skuInfoEntity == null) return null;
        Long spuId = skuInfoEntity.getSpuId();
        return getById(spuId);
    }

}