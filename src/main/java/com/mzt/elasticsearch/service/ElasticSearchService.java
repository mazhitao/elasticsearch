package com.mzt.elasticsearch.service;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.mzt.elasticsearch.entity.SearchResult;

public interface ElasticSearchService {

    void createEsDoc();

    void deleteEsDoc();

    PageInfo<SearchResult> searchByCondition(JSONObject param) ;
}
