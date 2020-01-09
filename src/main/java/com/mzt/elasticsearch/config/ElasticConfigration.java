package com.mzt.elasticsearch.config;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetAddress;

/**
 * @ClassName ElasticConfigration
 * @Description TODO
 * @Author mazhitao
 * @Date 2019/12/18 14:45
 * @Version 1.0
 **/
@Configuration
public class ElasticConfigration {
    @Value("${elasticsearch.host}")
    private String esHost;

    @Value("${elasticsearch.port}")
    private int esPort;

    @Value("${spring.data.elasticsearch.cluster-name}")
    private String esClusterName;

   private TransportClient client;

    @PostConstruct
    public void initialize() throws Exception {
        Settings esSettings = Settings.builder()
                .put("cluster.name", esClusterName)
//                .put("xpack.security.user","elastic:RJ0p-0p-0p-")
                .put("client.transport.sniff", true)
                .build() ;
//                  .put("client.transport.sniff", true).build();
        client = new PreBuiltTransportClient(esSettings);

        String[] esHosts = esHost.trim().split(",");
        for (String host : esHosts) {
            client.addTransportAddress(new TransportAddress(InetAddress.getByName(host), esPort));


        }
    }


    @Bean
    public Client client() {
        return client;
    }

    @PreDestroy
    public void destroy() {
        if (client != null) {
            client.close();
        }
    }
}
