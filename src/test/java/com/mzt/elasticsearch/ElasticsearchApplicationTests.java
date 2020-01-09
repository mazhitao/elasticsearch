package com.mzt.elasticsearch;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Date;


@SpringBootTest
class ElasticsearchApplicationTests {

	@Test
	void contextLoads() {
	}

	@Autowired
	Client client;

	@Test
	public void testEs(){
		JSONObject object = new JSONObject();
		object.put("id","1234567891111");
		object.put("createTime", DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
		object.put("userName","王五");
		object.put("userId","zhangsan");
		object.put("address","北京市锐捷网络");
		String jsonStr = JSON.toJSONString(object);
		IndexResponse indexResponse = client.prepareIndex("test", "_doc","123456").setSource(jsonStr, XContentType.JSON).get();
		System.out.println(indexResponse.status().getStatus());
	}

	@Test
	public void testEsdelete(){
		DeleteResponse deleteResponse = client.prepareDelete("test", "_doc", "123456").execute().actionGet();
		System.out.println(deleteResponse);
		System.out.println(deleteResponse.status().getStatus());
	}

	@Autowired
	RestHighLevelClient restHighLevelClient;

	@Test
	public void testRestEs(){
		JSONObject object = new JSONObject();
		object.put("id","1234567891111");
		object.put("createTime", DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
		object.put("userName","王五");
		object.put("userId","zhangsan");
		object.put("address","北京市房山区");
		IndexRequest indexRequest = new IndexRequest("test") ;
		indexRequest.id("1234561111111");
		String jsonStr = JSON.toJSONString(object);
		indexRequest.type("_doc");
		IndexRequest res = indexRequest.source(jsonStr, XContentType.JSON);
		try{
			IndexResponse index = restHighLevelClient.index(res,RequestOptions.DEFAULT);
			System.out.println(index);
		}catch (IOException es){
			es.printStackTrace();
		}
	}

	@Test
	public void testRestEsdelete() {
		DeleteRequest deleteRequest = new DeleteRequest("test", "_doc", "1234561111111");
		try {
			DeleteResponse delete = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
			System.out.println(delete);
			System.out.println(delete.status().getStatus());
		} catch (IOException ioex) {
			System.out.println(ioex.getMessage());
		}
	}


}
