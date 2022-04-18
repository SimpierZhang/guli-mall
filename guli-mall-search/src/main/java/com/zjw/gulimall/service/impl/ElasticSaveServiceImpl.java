package com.zjw.gulimall.service.impl;

import com.alibaba.fastjson.JSON;
import com.zjw.common.to.SkuEsModel;
import com.zjw.common.utils.R;
import com.zjw.gulimall.config.ElasticSearchConfig;
import com.zjw.gulimall.constant.EsConstant;
import com.zjw.gulimall.service.ElasticSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-15 17:28
 * @Modifier:
 */
@Slf4j
@Service
public class ElasticSaveServiceImpl implements ElasticSaveService
{
    @Resource
    private RestHighLevelClient client;

    @Override
    public boolean uploadProductInfoToEs(List<SkuEsModel> modelList) {
        //采用bulk进行批量保存
        BulkRequest bulkRequest = new BulkRequest(EsConstant.SaveIndexEnum.Index_TYPE_PRODUCT.getMessage());
        modelList.forEach(model -> {
            IndexRequest indexRequest = new IndexRequest();
            indexRequest.id(model.getSkuId().toString());
            String modelStr = JSON.toJSONString(model);
            indexRequest.source(modelStr, XContentType.JSON);
            bulkRequest.add(indexRequest);
        });
        try {
            BulkResponse bulkResponse = client.bulk(bulkRequest, ElasticSearchConfig.COMMON_OPTIONS);
            for (BulkItemResponse bulkItemResponse : bulkResponse) {
                if (bulkItemResponse.isFailed()) {
                    BulkItemResponse.Failure failure =
                            bulkItemResponse.getFailure();
                    log.error(failure.toString());
                }
            }
            return !bulkResponse.hasFailures();
        }catch (Exception e){
            log.error("上传es失败>>{}", e);
            throw new RuntimeException("es上传失败", e);
        }
    }
}
