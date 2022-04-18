package com.zjw.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.zjw.common.to.SeckillSkuRedisTo;
import com.zjw.common.utils.R;
import com.zjw.gulimall.product.entity.*;
import com.zjw.gulimall.product.feign.SeckillFeignService;
import com.zjw.gulimall.product.service.*;
import com.zjw.gulimall.product.vo.ItemSaleAttrVo;
import com.zjw.gulimall.product.vo.SkuItemVo;
import com.zjw.gulimall.product.vo.SkuPriceVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.Query;

import com.zjw.gulimall.product.dao.SkuInfoDao;

import javax.annotation.Resource;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Resource
    private SkuImagesService skuImagesService;
    @Resource
    private SpuInfoDescService spuInfoDescService;
    @Resource
    private AttrGroupService attrGroupService;
    @Resource
    private AttrAttrgroupRelationService attrAttrgroupRelationService;
    @Resource
    private AttrService attrService;
    @Resource
    private ProductAttrValueService productAttrValueService;
    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Resource
    private ExecutorService executor;
    @Resource
    private SeckillFeignService seckillFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /**
         * key:
         * catelogId: 0
         * brandId: 0
         * min: 0
         * max: 0
         */
        //加上筛选条件
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if(StringUtils.isNotBlank(key)){
            queryWrapper.eq("sku_id", key).or().like("sku_name", key);
        }
        String catelogId = (String) params.get("catalogId");
        if(StringUtils.isNotBlank(catelogId) && !catelogId.equals("0")){
            queryWrapper.eq("catalog_id", catelogId);
        }
        String brandId = (String) params.get("brandId");
        if(StringUtils.isNotBlank(catelogId) && !brandId.equals("0")){
            queryWrapper.eq("brand_id", brandId);
        }
        String minPrice = (String) params.get("min");
        String maxPrice = (String) params.get("max");
        if(StringUtils.isNotBlank(minPrice) && StringUtils.isNotBlank(maxPrice)){
            queryWrapper.between("price", minPrice, maxPrice);
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );
        return new PageUtils(page);
    }

    //用于商品详情展示
    @Override
    public SkuItemVo getSkuItemBySkuId(Long skuId) {
        //都采用同步的方式太浪费时间，可以采用异步编排的方式降低查询时间
        //2.3.4都要依赖1的结果，而5不需要，因此1和5可以同时进行，2，3，4可以等到1结束后同时进行
        //时间由132>>>80
        long start = System.currentTimeMillis();
        SkuItemVo itemVo = new SkuItemVo();
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            //1.相关的skuInfo>>pms_sku_info
            SkuInfoEntity info = getById(skuId);
            itemVo.setInfo(info);
            return info;
        }, executor);

        CompletableFuture<Void> saleAttrFuture = infoFuture.thenAcceptAsync(info -> {
            //2.相关的sku销售属性组合>>pms_sku_sale_attr_value
            List<ItemSaleAttrVo> itemSaleAttrVos = skuSaleAttrValueService.getItemSaleAttr(info.getSpuId());
            itemVo.setSaleAttr(itemSaleAttrVos);
        }, executor);

        CompletableFuture<Void> descFuture = infoFuture.thenAcceptAsync(info -> {
            //3.商品详情描述图片>>pms_spu_info_desc
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(info.getSpuId());
            itemVo.setDesc(spuInfoDescEntity);
        }, executor);


        CompletableFuture<Void> attrFuture = infoFuture.thenAcceptAsync(info -> {
            //4.sku属性描述>>pms_product_attr_value
            //4.1 根据catalogId查出所有属性组
            List<AttrGroupEntity> groupEntityList = attrGroupService.list(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", info.getCatalogId()));
            List<SkuItemVo.SpuItemAttrGroup> groupAttrs = groupEntityList.stream().map(group -> {
                SkuItemVo.SpuItemAttrGroup attrGroup = new SkuItemVo.SpuItemAttrGroup();
                attrGroup.setGroupName(group.getAttrGroupName());
                //4.2 根据属性组查出所有的属性id
                List<AttrAttrgroupRelationEntity> attrIdListWithGroup
                        = attrAttrgroupRelationService.list(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_group_id", group.getAttrGroupId()));
                List<ProductAttrValueEntity> productAttrValueEntityList = productAttrValueService.list(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", info.getSpuId()).
                        in("attr_id", attrIdListWithGroup.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList())));
                //4.3 查出对应spuId的属性
                List<SkuItemVo.SpuBaseAttrVo> baseAttrVoList = productAttrValueEntityList.stream().map(pa -> {
                    SkuItemVo.SpuBaseAttrVo spuBaseAttrVo = new SkuItemVo.SpuBaseAttrVo();
                    spuBaseAttrVo.setAttrName(pa.getAttrName());
                    spuBaseAttrVo.setAttrValue(pa.getAttrValue());
                    return spuBaseAttrVo;
                }).collect(Collectors.toList());
                attrGroup.setAttrs(baseAttrVoList);
                return attrGroup;
            }).collect(Collectors.toList());
            itemVo.setGroupAttrs(groupAttrs);
        }, executor);

        CompletableFuture<Void> imagesFuture = CompletableFuture.runAsync(() -> {
            //5.sku的图片信息
            List<SkuImagesEntity> skuImagesEntityList = skuImagesService.list(new QueryWrapper<SkuImagesEntity>().eq("sku_id", skuId));
            itemVo.setImages(skuImagesEntityList);
        }, executor);

        //5.查询该商品是否处于秒杀活动，如果处于秒杀活动，还需要封装秒杀信息
        CompletableFuture<Void> seckillInfoFuture = CompletableFuture.runAsync(() -> {
            R r = seckillFeignService.getSeckillInfoBySkuId(skuId);
            if (r.getCode() == 0) {
                itemVo.setSeckillInfoVo(r.getData(new TypeReference<SeckillSkuRedisTo>()
                {
                }));
            }
        });


        //等待所有线程完成之后才返回结果
        CompletableFuture<Void> allFuture = CompletableFuture.allOf(saleAttrFuture, attrFuture, descFuture, imagesFuture, seckillInfoFuture);
        try {
            allFuture.get();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("总耗时：>>" + (System.currentTimeMillis() - start));
        return itemVo;
    }

    @Override
    public List<SkuPriceVo> listPriceByIds(List<Long> idList) {
        if(idList == null || idList.size() <= 0) return null;
        return baseMapper.listPriceByIds(idList);
    }


}