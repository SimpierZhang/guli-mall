package com.zjw.gulimall.web;

import com.zjw.gulimall.service.ElasticSearchService;
import com.zjw.gulimall.vo.SearchParam;
import com.zjw.gulimall.vo.SearchResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Author: Zjw
 * @Description:
 * @Create 2021-08-19 22:09
 * @Modifier:
 */
@Controller
public class SearchController
{
    @Resource
    private ElasticSearchService searchService;

    @GetMapping(value = {"/", "/list.html"})
    public String getList(SearchParam searchParam, Model model, HttpServletRequest request){
        String queryString = request.getQueryString();
        searchParam.set_queryString(queryString);
        SearchResult result = searchService.search(searchParam);
        model.addAttribute("result", result);
        return "list";
    }
}
