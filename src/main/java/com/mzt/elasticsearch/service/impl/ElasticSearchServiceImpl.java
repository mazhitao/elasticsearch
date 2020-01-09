package com.mzt.elasticsearch.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.mzt.elasticsearch.entity.SearchResult;
import com.mzt.elasticsearch.service.ElasticSearchService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @ClassName ElasticSearchServiceImpl
 * @Description TODO
 * @Author mazhitao
 * @Date 2020/1/9 15:21
 * @Version 1.0
 **/
@Service
public class ElasticSearchServiceImpl implements ElasticSearchService {

    @Autowired
    Client client;

    @Override
    public void createEsDoc() {
        JSONObject object = new JSONObject();
        object.put("id","1234567891111");
        object.put("createTime", DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
        object.put("userName","张三");
        object.put("userId","zhangsan");
        object.put("address","北京市海淀区");
        String jsonStr = JSON.toJSONString(object);
        IndexResponse indexResponse = client.prepareIndex("test", "_doc","123456").setSource(jsonStr, XContentType.JSON).get();
        System.out.println(indexResponse.status().getStatus());
    }

    @Override
    public void deleteEsDoc() {
        DeleteResponse deleteResponse = client.prepareDelete("test", "_doc", "123456").execute().actionGet();
        System.out.println(deleteResponse);
        System.out.println(deleteResponse.status().getStatus());
    }

    @Override
    public PageInfo<SearchResult> searchByCondition(JSONObject param) {
        String searchKey = param.getString("searchKey");
        BoolQueryBuilder keyWordBooleanQuery = QueryBuilders.boolQuery();
        QueryStringQueryBuilder queryStringQueryBuilder =queryStringQueryBuilder(searchKey);
        if(StringUtils.isBlank(searchKey)) {
            // 查询匹配所有文件
            keyWordBooleanQuery.must(QueryBuilders.matchAllQuery()) ;
        }else {
            BoolQueryBuilder contextBoolQuery= QueryBuilders.boolQuery().should(queryStringQueryBuilder)
                    .should(QueryBuilders.matchPhraseQuery("content", searchKey).slop(100).boost(20) );
            keyWordBooleanQuery.must(contextBoolQuery) ;
        }
        this.createSearchCondition(param, keyWordBooleanQuery);
        HighlightBuilder highlightBuilder = this.highlightBuilder();
        // 搜索query
        String pageSize = this.getValue(param,  "pageSize");
        String pageNumber = this.getValue(param,  "pageNum");
        String indexES = this.getValue(param,"productType");
        List<String> indexList = getAllIndex();
        if(StringUtils.isNotBlank(indexES)){
            if(indexList.contains(indexES)){
                indexList.clear();
                indexList.add(indexES);
            }
        }
        int from = (Integer.parseInt(pageNumber)-1)*Integer.parseInt(pageSize);
        int size=Integer.parseInt(pageSize) ;

        SearchRequestBuilder query = client.prepareSearch(indexList.toArray(new String[indexList.size()])).setTypes("_doc").setFrom(from).setSize(size)
                .highlighter(highlightBuilder).setQuery(keyWordBooleanQuery);
        // 执行搜索
        SearchResponse response = query.execute().actionGet();
        // 处理结果
        PageInfo<SearchResult> pageInfo= createPageInfo(response, pageNumber);
        return pageInfo;
    }
    private List<String> getAllIndex(){
        ActionFuture<IndicesStatsResponse> isr = client.admin().indices().stats(new IndicesStatsRequest().all());
        Set<String> set = isr.actionGet().getIndices().keySet();
        List<String> list = new ArrayList<>(set);
        return list;
    }

    private void createSearchCondition(JSONObject param,BoolQueryBuilder keyWordBooleanQuery) {
        JSONObject attrParam =  param.getJSONObject("attrParam");
        // 获取json的所有key
        Set<String> strings = attrParam.keySet();
        for(String key : strings){
            String value = attrParam.getString(key);
            if(StringUtils.isNotBlank(value)){
                MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery(key,value);
                keyWordBooleanQuery.must(  matchQueryBuilder  ) ;
            }
        }
        String supplierName = param.getString("productProvider");
        if(StringUtils.isNotBlank(supplierName)){
            MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("supplierName",supplierName);
            keyWordBooleanQuery.must(  matchQueryBuilder  ) ;
        }
    }

    private QueryStringQueryBuilder queryStringQueryBuilder(String searchKey) {
        String escapedSearch = "" + QueryParser.escape(searchKey) + "";
        QueryStringQueryBuilder queryStringQueryBuilder = QueryBuilders.queryStringQuery(escapedSearch).field("content")
                .field("productDesc")
                .field("note")
                .minimumShouldMatch("75%")
                .tieBreaker( (float)0.2) ;  //.phraseSlop(0)  analyzer("ik_smart") useDisMax(true)  .boost(2)
        return queryStringQueryBuilder;
    }

    /**按照分页的格式返回对象 */
    private PageInfo<SearchResult> createPageInfo(SearchResponse response,String pageNumber) {
        SearchHits searchHits = response.getHits(); // 处理结果
        long total = searchHits.getTotalHits() ;
        List<SearchResult> searchResultList=handResult(searchHits);
        PageInfo<SearchResult> pageInfo = new PageInfo<SearchResult>(searchResultList);
        pageInfo.setTotal(total);
        pageInfo.setPageNum(Integer.parseInt(pageNumber));
        return pageInfo;
    }

    /**处理查询结果*/
    private List<SearchResult> handResult(SearchHits searchHits) {
        // 处理结果
//        LOGGER.info( "执行完成： " +   JSON.toJSONString(searchHits));
        SearchHit[] hitArray = searchHits.getHits();
        List<SearchResult>  searchResultList = new ArrayList<>();
        for (SearchHit hit : hitArray) {
            Map<String, Object> map = hit.getSourceAsMap() ;
//            LOGGER.info( "查询结果： "  + JSON.toJSONString(map));
            //获取高亮字段
            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField abstractContextField = highlightFields.get("content");
//            HighlightField contentField = highlightFields.get("docContext");  //content
            String abstarctHighlightText=getHighlightTextByFiled(abstractContextField);
//            String contextHighlightText=getHighlightTextByFiled(contentField);

            SearchResult sr= new SearchResult();
            String docContext=String.valueOf(map.get("content") );
            if(StringUtils.isBlank(docContext) ||"null".equals(docContext)) {
                docContext=String.valueOf(map.get("productDesc") );
            }
            sr.setContext(docContext);
            sr.setId(String.valueOf(map.get("id") ));
            sr.setFileName(String.valueOf(map.get("fileName")));
            sr.setFilePath(String.valueOf(map.get("fileId") ));
            sr.setHighlightText(abstarctHighlightText);
            if(StringUtils.isBlank(abstarctHighlightText) && StringUtils.isNotBlank(docContext) ) {
                int doclength = docContext.length() ;
                if(doclength>500) {
                    sr.setHighlightText(docContext.substring(0,500) );
                }else {
                    sr.setHighlightText(docContext );
                }
            }
            sr.setTitle(String.valueOf(map.get("title") ));
            sr.setResId(String.valueOf(map.get("resId")));
            sr.setSupplierName(String.valueOf(map.get("supplierName")));
            searchResultList.add(sr);
        }

        return searchResultList;


    }

    /**解析查询结果中高亮显示的内容*/
    private String getHighlightTextByFiled( HighlightField highlightField) {
        if(highlightField!=null){
            Text[] fragments = highlightField.fragments();
            String name = "";
            for (Text text : fragments) {
                name+=text;
            }
//            LOGGER.info( "高亮数据： " + name);
            return name;
        }
        return "" ;
    }

    /**设置高亮显示的格式*/
    private HighlightBuilder highlightBuilder() {
        HighlightBuilder  highlightBuilder=new HighlightBuilder();
        highlightBuilder.field("content").field("productDesc").field("note").highlighterType("unified")  ;   //  plain  unified
        highlightBuilder.requireFieldMatch(false);     //如果要多个字段高亮,这项要为false
        highlightBuilder.preTags("<span style='color:red'>");   //高亮设置
        highlightBuilder.postTags("</span>");
        //下面这两项,如果你要高亮如文字内容等有很多字的字段,必须配置,不然会导致高亮不全,文章内容缺失等
        highlightBuilder.fragmentSize(30); //最大高亮分片数   highlightBuilder.fragmentSize(800000);
        highlightBuilder.numOfFragments(5); //从第一个分片获取高亮片段
        highlightBuilder.boundaryMaxScan(500) ;
        return highlightBuilder;
    }


    private String getValue(JSONObject param,String key) {
        String value = param.getString(key);
        if(StringUtils.isNotBlank(value)) {
            return value;
        }
        return "";
    }
}
