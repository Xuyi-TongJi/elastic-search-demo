package cn.itcast.hotel.service.impl;

import cn.itcast.hotel.lang.PageResult;
import cn.itcast.hotel.mapper.HotelMapper;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.pojo.RequestParameter;
import cn.itcast.hotel.service.IHotelService;
import cn.itcast.hotel.util.HotelSearchUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * @author xuyitjuseu
 */
@Service
public class HotelServiceImpl extends ServiceImpl<HotelMapper, Hotel> implements IHotelService {

    private final RestHighLevelClient restClient;
    private final HotelMapper hotelMapper;

    public HotelServiceImpl(RestHighLevelClient restClient,
                            HotelMapper hotelMapper) {
        this.restClient = restClient;
        this.hotelMapper = hotelMapper;
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

    @Override
    public List<String> suggestion(String key) {
        SearchRequest request = new SearchRequest(HotelSearchUtil.NEW_INDEX_NAME);
        String name = "suggestions";
        request.source().suggest(new SuggestBuilder().addSuggestion(name,
                SuggestBuilders.completionSuggestion("suggestion").prefix(key).skipDuplicates(true).size(10)));
        try {
            SearchResponse response = restClient.search(request, RequestOptions.DEFAULT);
            // resolve response
            return HotelSearchUtil.resolveSuggestions(response, name);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteById(Long id) {
        DeleteRequest request = new DeleteRequest(HotelSearchUtil.NEW_INDEX_NAME, id.toString());
        try {
            restClient.delete(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            // 全局异常处理处理该异常
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateById(Long id) {
        Hotel hotel = hotelMapper.selectById(id);
        IndexRequest request = new IndexRequest(HotelSearchUtil.NEW_INDEX_NAME).id(id.toString());
        HotelDoc hotelDoc = new HotelDoc(hotel);
        request.source(JSON.toJSONString(hotelDoc), XContentType.JSON);
        try {
            restClient.index(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
