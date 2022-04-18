package com.zjw.gulimall.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zjw.common.to.SkuEsModel;
import com.zjw.gulimall.config.ElasticSearchConfig;
import com.zjw.gulimall.constant.EsConstant;
import com.zjw.gulimall.service.ElasticSearchService;
import com.zjw.gulimall.vo.SearchParam;
import com.zjw.gulimall.vo.SearchResult;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.Nested;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import sun.awt.AWTCharset;

import javax.annotation.Resource;
import java.beans.Encoder;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-21 16:35
 * @Modifier:
 */
@Service
public class ElasticSearchServiceImpl implements ElasticSearchService
{
    @Resource
    private RestHighLevelClient client;

    @Override
    public SearchResult search(SearchParam searchParam) {
        SearchResult searchResult = null;
        //1.根据searchParam封装dsl语句
        SearchRequest searchRequest = initSearchStatement(searchParam);

        //2.将dsl语句用于全文检索
        try {
            SearchResponse searchResponse = client.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);
            searchResult = wrapSearchResult(searchResponse, searchParam);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        //3.根据全文检索的结果封装成检索结果
        return searchResult;
    }

    private SearchResult wrapSearchResult(SearchResponse searchResponse, SearchParam searchParam) {
        SearchResult searchResult = new SearchResult();
        SearchHit[] hits = searchResponse.getHits().getHits();
        List<SkuEsModel> productList = Arrays.stream(hits).map(h -> {
            return JSON.parseObject(h.getSourceAsString(), new TypeReference<SkuEsModel>()
            {
            });
        }).collect(Collectors.toList());
        if(productList.size() > 0){
            searchResult.setProducts(productList);
        }
        searchResult.setPageNum(searchParam.getPageNum());
        long totalHit = searchResponse.getHits().getTotalHits().value;
        searchResult.setTotal(totalHit);
        int totalPageNum = (int) (totalHit % EsConstant.PAGE_SIZE == 0 ? totalHit / EsConstant.PAGE_SIZE : totalHit / EsConstant.PAGE_SIZE + 1);
        searchResult.setTotalPages(totalPageNum);
        List<Integer> pageNav = new ArrayList<>();
        for(int i = 0; i < totalPageNum; i++){
            pageNav.add(i + 1);
        }
        searchResult.setPageNavs(pageNav);

        //分析聚合结果
        Aggregations aggregations = searchResponse.getAggregations();
        Terms catalog_agg = aggregations.get("catalog_agg");
        List<? extends Terms.Bucket> catalogBuckets = catalog_agg.getBuckets();
        List<SearchResult.CatalogVo> catalogVoList = catalogBuckets.stream().map(cb -> {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            catalogVo.setCatalogId((Long) cb.getKey());
            Terms catalog_name_agg = cb.getAggregations().get("catalog_name_agg");

            if (catalog_name_agg.getBuckets() != null && catalog_name_agg.getBuckets().size() > 0) {
                catalogVo.setCatalogName(catalog_name_agg.getBuckets().get(0).getKey().toString());
            }
            return catalogVo;
        }).collect(Collectors.toList());
        searchResult.setCatalogs(catalogVoList);

        //封装品牌
        Terms brand_agg = aggregations.get("brand_agg");
        List<? extends Terms.Bucket> brandBuckets = brand_agg.getBuckets();
        List<SearchResult.BrandVo> brandVoList = brandBuckets.stream().map(bb -> {
            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            brandVo.setBrandId((Long) bb.getKeyAsNumber());
            Terms brand_img_agg = bb.getAggregations().get("brand_img_agg");
            if (brand_img_agg != null) {
                brandVo.setBrandImg(brand_img_agg.getBuckets().get(0).getKey().toString());
            }
            Terms brand_name_agg = bb.getAggregations().get("brand_name_agg");
            if (brand_name_agg != null) {
                brandVo.setBrandName(brand_name_agg.getBuckets().get(0).getKey().toString());
            }
            return brandVo;
        }).collect(Collectors.toList());
        searchResult.setBrands(brandVoList);

        //封装属性
        Nested attr_agg = aggregations.get("attr_agg");
        Terms attr_id_agg = attr_agg.getAggregations().get("attr_id_agg");
        List<? extends Terms.Bucket> attrBuckets = attr_id_agg.getBuckets();
        List<SearchResult.AttrVo> attrVoList = attrBuckets.stream().map(ab -> {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            attrVo.setAttrId((Long) ab.getKeyAsNumber());
            Terms attr_name_agg = ab.getAggregations().get("attr_name_agg");
            attrVo.setAttrName(attr_name_agg.getBuckets().get(0).getKey().toString());
            Terms attr_value_agg = ab.getAggregations().get("attr_value_agg");
            List<String> valueList = attr_value_agg.getBuckets().stream().map(avb -> avb.getKey().toString()).collect(Collectors.toList());
            attrVo.setAttrValue(valueList);
            return attrVo;
        }).collect(Collectors.toList());
        searchResult.setAttrs(attrVoList);
        searchResult.setAttrIds(searchResult.getAttrs().stream().map(SearchResult.AttrVo::getAttrId).collect(Collectors.toList()));

        //封装面包屑导航
        List<String> queryAttrStrList = searchParam.getAttrs();
        String searchQueryString = searchParam.get_queryString();
        if(queryAttrStrList != null && queryAttrStrList.size() > 0){
            List<SearchResult.NavVo> navVoList = queryAttrStrList.stream().map(attrStr -> {
                SearchResult.NavVo navVo = new SearchResult.NavVo();
                String[] s = attrStr.split("_");
                navVo.setNavValue(s[1]);
                SearchResult.AttrVo attrVo = attrVoList.stream().filter(a -> a.getAttrId().equals(Long.valueOf(s[0]))).collect(Collectors.toList()).get(0);
                navVo.setName(attrVo.getAttrName());
                String encode = s[0];
                try {
                    encode = URLEncoder.encode(attrStr, "utf-8");
                }
                catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String removedQueryStr = searchQueryString.replace("attrs=" + encode, "");
                navVo.setLink("http://search.gulimall.com/list.html?" + removedQueryStr);
                return navVo;
            }).collect(Collectors.toList());
            searchResult.setNavs(navVoList);
        }
        return searchResult;
    }


    /**
     *  //封装dsl语句
     * @param searchParam
     * @return
     *  * 前端检索字段
     *  * keyword=小米&
     *  * brandId=1&
     *  * catalog3Id=1&
     *  * hasStock=0/1&
     *
     *  * skuPrice=400_1900/400_/_1900
     *  * sort=saleCount_desc/asc&
     *
     *  * attrs=1_3G:4G:5G&
     *  * attrs=2_骁龙845&
     *  * attrs=4_高清屏
     */
    private SearchRequest initSearchStatement(SearchParam searchParam){
        if(searchParam == null) return null;
        SearchRequest searchRequest = new SearchRequest(EsConstant.SaveIndexEnum.Index_TYPE_PRODUCT.getMessage());
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //构建查询条件
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        if(StringUtils.isNotBlank(searchParam.getKeyword())){
            boolQueryBuilder.must(QueryBuilders.matchQuery("skuTitle", searchParam.getKeyword()));
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle");
            builder.preTags("<b style='color:red'>");
            builder.postTags("</b>");
            sourceBuilder.highlighter(builder);
        }
        if(searchParam.getCatalog3Id() != null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("catalogId", searchParam.getCatalog3Id()));
        }
        List<Long> brandIdList = searchParam.getBrandId();
        if(brandIdList != null && brandIdList.size() > 0){
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", brandIdList));
        }
        if(searchParam.getHasStock() != null){
            boolQueryBuilder.filter(QueryBuilders.termQuery("hasStock", searchParam.getHasStock() == 1));
        }
        //构建价格范围查询
        String skuPriceStr = searchParam.getSkuPrice();
        if(StringUtils.isNotBlank(skuPriceStr)){
            //skuPrice=400_1900/400_/_1900
            String[] s = skuPriceStr.split("_");
            if(skuPriceStr.startsWith("_")){
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("skuPrice").lt(s[0]));
            }else if(skuPriceStr.endsWith("_")){
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("skuPrice").gt(s[0]));
            }else {
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("skuPrice").gt(s[0]).lt(s[1]));
            }
        }
        List<String> attrsStrList = searchParam.getAttrs();
        if(attrsStrList != null && attrsStrList.size() > 0){
            for(String attrStr : attrsStrList){
                BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
                String[] s = attrStr.split("_");
                String[] attrValues = s[1].split(":");
                boolBuilder.must(QueryBuilders.termsQuery("attrs.attrId", s[0]));
                boolBuilder.must(QueryBuilders.termsQuery("attrs.attrValue", attrValues));
                boolQueryBuilder.filter(QueryBuilders.nestedQuery("attrs", boolBuilder, ScoreMode.None));
            }
        }
        sourceBuilder.query(boolQueryBuilder);

        //构建排序和分页条件
        //构建排序条件
        String sortStr = searchParam.getSort();
        if(StringUtils.isNotBlank(sortStr)){
            String[] s = sortStr.split("_");
            sourceBuilder.sort(s[0], SortOrder.fromString(s[1]));
        }
        sourceBuilder.from((searchParam.getPageNum() - 1) * EsConstant.PAGE_SIZE);
        sourceBuilder.size(EsConstant.PAGE_SIZE);


        //构建聚合条件
        TermsAggregationBuilder brand_agg = AggregationBuilders.terms("brand_agg").field("brandId");
        brand_agg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName"));
        brand_agg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg"));
        sourceBuilder.aggregation(brand_agg);

        TermsAggregationBuilder catalog_agg = AggregationBuilders.terms("catalog_agg").field("catalogId");
        catalog_agg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName"));
        sourceBuilder.aggregation(catalog_agg);

        NestedAggregationBuilder attr_agg = AggregationBuilders.nested("attr_agg", "attrs");
        TermsAggregationBuilder attr_id_agg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName"));
        attr_id_agg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue"));
        attr_agg.subAggregation(attr_id_agg);
        sourceBuilder.aggregation(attr_agg);

        searchRequest.source(sourceBuilder);
        return searchRequest;
    }

}
