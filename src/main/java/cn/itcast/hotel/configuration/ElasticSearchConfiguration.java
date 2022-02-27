package cn.itcast.hotel.configuration;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author xuyitjuseu
 */
@Configuration
public class ElasticSearchConfiguration {
    @Bean
    public RestHighLevelClient getRestClient() {
        return new RestHighLevelClient(RestClient.builder(HttpHost.create("http://122.112.168.7:9200")));
    }
}
