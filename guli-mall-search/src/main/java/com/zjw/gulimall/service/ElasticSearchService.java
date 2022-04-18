package com.zjw.gulimall.service;

import com.zjw.gulimall.vo.SearchParam;
import com.zjw.gulimall.vo.SearchResult;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-21 16:34
 * @Modifier:
 */
public interface ElasticSearchService
{
    SearchResult search(SearchParam searchParam);
}
