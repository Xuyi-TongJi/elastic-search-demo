package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.lang.PageResult;
import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.RequestParameter;
import cn.itcast.hotel.service.IHotelService;
import cn.itcast.hotel.util.HotelSearchUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author xuyitjuseu
 */
@Service
public class HotelServiceImpl extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {

    private final RestHighLevelClient restClient;

    public HotelServiceImpl(RestHighLevelClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public PageResult search(RequestParameter param) {
        // build request
        SearchRequest request = HotelSearchUtil.getMatchSearchRequest(param);
        // send request
        try {
            SearchResponse response = restClient.search(request, RequestOptions.DEFAULT);
            return HotelSearchUtil.getPageResult(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PageResult booleanQuery(RequestParameter param) {
        SearchRequest request = new SearchRequest(HotelSearchUtil.INDEX_NAME);
        // DSL
        request.source().query(HotelSearchUtil.getBooleanQuery(param));
        try {
            SearchResponse response = restClient.search(request, RequestOptions.DEFAULT);
            return HotelSearchUtil.getPageResult(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public PageResult functionScoreQuery(RequestParameter param) {
        SearchRequest request = new SearchRequest(HotelSearchUtil.INDEX_NAME);
        // 获得算分查询构建器
        request.source().query(HotelSearchUtil.getFunctionScoreQueryBuilder(param));
        try {
            SearchResponse response = restClient.search(request, RequestOptions.DEFAULT);
            return HotelSearchUtil.getPageResult(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
