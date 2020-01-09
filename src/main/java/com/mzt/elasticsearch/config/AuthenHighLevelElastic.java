package com.mzt.elasticsearch.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthenHighLevelElastic {

    @Value("${elasticsearch.rest.host}")
    private String esHost;

    @Value("${elasticsearch.rest.account}")
    private String esAccount;

    @Value("${elasticsearch.rest.password}")
    private String password;

    @Bean
    public RestHighLevelClient client(){
        /*用户认证对象*/
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        /*设置账号密码*/
        credentialsProvider.setCredentials(AuthScope.ANY,new UsernamePasswordCredentials(esAccount, password));
        /*创建rest client对象*/
        // 获取所有ES节点
        String[] esHosts = esHost.trim().split(",");
        int esNum = esHosts.length;
        HttpHost[] hosts = new HttpHost[esNum];
        for(int i = 0 ;i < esNum ;i++){
            String[] esAddress = esHosts[i].trim().split(":");
            hosts[i] = new HttpHost(esAddress[0],Integer.parseInt(esAddress[1]));
        }
        RestClientBuilder builder = RestClient.builder(hosts)
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
                        return httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                });
        RestHighLevelClient client = new RestHighLevelClient(builder);
        return client;
    }


}
