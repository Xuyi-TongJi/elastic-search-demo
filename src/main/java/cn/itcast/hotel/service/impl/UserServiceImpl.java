package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.pojo.RequestParameter;
import cn.itcast.hotel.service.IHotelService;
import cn.itcast.hotel.service.IUserService;
import cn.itcast.hotel.util.HotelSearchUtil;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * @author xuyitjuseu
 */
@Service
public class UserServiceImpl implements IUserService {

    private final RestHighLevelClient restClient;

    public UserServiceImpl(RestHighLevelClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Map<String, List<String>> filter(RequestParameter requestParameter, Map<String, String> paramMap) {
        // if requestParam != null set requestParam
        SearchRequest request;
        if (requestParameter != null) {
            request = HotelSearchUtil.getMatchSearchRequest(requestParameter);
        } else {
            request = new SearchRequest(HotelSearchUtil.INDEX_NAME);
        }
        request.source().size(0);
        Set<String> keySet = paramMap.keySet();
        for (String key : keySet) {
            String value = paramMap.get(key);
            request.source().aggregation(AggregationBuilders.terms(value)
                    .field(key).size(100));
        }
        // send request
        try {
            SearchResponse response = restClient.search(request, RequestOptions.DEFAULT);
            // resolve response
            Map<String, List<String>> result = new HashMap<>(3);
            for (String key : keySet) {
                List<String> list = new ArrayList<>();
                Terms terms = response.getAggregations().get(paramMap.get(key));
                List<? extends Terms.Bucket> buckets = terms.getBuckets();
                for (Terms.Bucket bucket : buckets) {
                    String bucketKey = bucket.getKeyAsString();
                    list.add(bucketKey);
                }
                result.put(key, list);
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
