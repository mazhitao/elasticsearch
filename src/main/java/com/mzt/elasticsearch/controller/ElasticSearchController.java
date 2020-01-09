package com.mzt.elasticsearch.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.mzt.elasticsearch.entity.SearchResult;
import com.mzt.elasticsearch.service.ElasticSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @ClassName ElasticSearchController
 * @Description TODO
 * @Author mazhitao
 * @Date 2020/1/9 15:20
 * @Version 1.0
 **/
@RestController
@RequestMapping("/elastic")
public class ElasticSearchController {

    @Autowired
    ElasticSearchService elasticSearchService;

    @PostMapping("/createEsDoc")
    public Object createEsDoc(){
        elasticSearchService.createEsDoc();
        return "插入成功";
    }

    @GetMapping("/deleteEsDoc")
    public Object deleteEsDoc(){
        elasticSearchService.deleteEsDoc();
        return "删除成功";
    }

    /**
     * 根据关键字查询寻源的数据
     * @param param
     * @return
     */
    @PostMapping("/searchByCondition")
    public PageInfo<SearchResult> searchByCondition(@RequestBody String param){
        try{
            JSONObject paramJson = JSON.parseObject(param);
            PageInfo<SearchResult>  info = elasticSearchService.searchByCondition(paramJson) ;
            return info;
        }catch(Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
