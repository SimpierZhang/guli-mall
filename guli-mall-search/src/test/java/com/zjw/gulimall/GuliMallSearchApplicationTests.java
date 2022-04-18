package com.zjw.gulimall;

import com.alibaba.fastjson.JSON;
import com.zjw.gulimall.config.ElasticSearchConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

@SpringBootTest
class GuliMallSearchApplicationTests
{

    @Resource
    private RestHighLevelClient client;

    @Test
    void contextLoads() {
    }

    @Test
    public void testIndexSearch() throws IOException {
        //1.设置索引
        IndexRequest indexRequest = new IndexRequest("users");
        //设置要保存的数据的id
        indexRequest.id("1");
        //一般保存数据都是将类变成json字符串然后进行保存
        User user = new User("zjw", 23);
        String userStr = JSON.toJSONString(user);
        //将数据封装到请求中
        indexRequest.source(userStr, XContentType.JSON);
        //2.提交新建数据请求
        IndexResponse indexResponse = client.index(indexRequest, ElasticSearchConfig.COMMON_OPTIONS);
        System.out.println("请求>>>" + indexRequest);
        System.out.println("响应>>>" + indexResponse);
    }


    //#按照年龄聚合，并且求这些年龄段的这些人的平均薪资
    @Test
    public void testAggs() throws IOException {
        SearchRequest searchRequest = new SearchRequest("newbank");
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.matchAllQuery());
        builder.from(0);
        builder.size(5);
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("age_agg").field("age");
        ageAgg.subAggregation(AggregationBuilders.avg("age_balance_agg").field("balance"));
        builder.aggregation(ageAgg);
        searchRequest.source(builder);
        System.out.println(searchRequest.toString());
        SearchResponse searchResponse = client.search(searchRequest, ElasticSearchConfig.COMMON_OPTIONS);

        System.out.println(searchResponse.toString());
        //解析响应
        //1.获取查询数据
        SearchHit[] hits = searchResponse.getHits().getHits();
        for (SearchHit hit : hits) {
            String index = hit.getIndex();
            float score = hit.getScore();
            String sourceAsString = hit.getSourceAsString();
            Account account = JSON.parseObject(sourceAsString, Account.class);
            System.out.println("索引:" + index + ">>得分：" + score + ">>数据：" + account);
        }

        //2.获取aggregations
        Aggregations aggregations = searchResponse.getAggregations();
        Terms age_agg = aggregations.get("age_agg");
        List<? extends Terms.Bucket> buckets = age_agg.getBuckets();
        buckets.forEach(b -> {
            System.out.println("key:" + b.getKey() + ">>count:" + b.getDocCount());
            //如果还有聚合，继续在里面遍历
            Aggregations aggregations1 = b.getAggregations();
            Avg age_balance_agg = aggregations1.get("age_balance_agg");
        });

    }



}

@ToString
@NoArgsConstructor
@AllArgsConstructor
@Data
class User{
    private String name;
    private int age;
}
