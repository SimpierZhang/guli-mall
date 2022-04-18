package com.zjw.gulimall.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-14 22:51
 * @Modifier:
 */
@Configuration
public class ElasticSearchConfig
{
    @Value("${elasticsearch.host}")
    private String host;
    @Value("${elasticsearch.port}")
    private String port;

    //配置一下elasticSearch的通用配置
    public static final RequestOptions COMMON_OPTIONS;

    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();

        COMMON_OPTIONS = builder.build();
    }

    //注入RestHighLevelClient用于之后的调用
    @Bean
    public RestHighLevelClient esRestClient(){
        RestClientBuilder builder = null;
        //此处还可以使用多个elasticSearch，集群
        builder = RestClient.builder(new HttpHost(host, Integer.parseInt(port)));
        RestHighLevelClient client = new RestHighLevelClient(builder);
        return client;
    }
}
