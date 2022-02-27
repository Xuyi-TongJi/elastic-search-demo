package cn.itcast.hotel.util;

import cn.itcast.hotel.lang.PageResult;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.pojo.RequestParameter;
import com.alibaba.fastjson.JSON;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;

import java.util.ArrayList;
import java.util.List;

/**
 * ES查询工具类
 * @author xuyitjuseu
 */
public class HotelSearchUtil {

    public static final String INDEX_NAME = "hotel2";

    /**
     * 封装PageResult
     * @return pageResult
     */
    public static PageResult getPageResult(SearchResponse response) {
        PageResult pageResult = new PageResult();
        SearchHits hits = response.getHits();
        List<HotelDoc> list = new ArrayList<>();
        pageResult.setTotal(hits.getTotalHits().value);
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit searchHit : searchHits) {
            HotelDoc hotelDoc = JSON.parseObject(searchHit.getSourceAsString(), HotelDoc.class);
            // 获取排序值
            Object[] sortValues = searchHit.getSortValues();
            if (sortValues.length > 0) {
                Object distance = sortValues[0];
                hotelDoc.setDistance(distance);
            }
            list.add(hotelDoc);
        }
        pageResult.setHotels(list);
        return pageResult;
    }

    /**
     * 获得匹配查询的查询请求类
     * @param param 查询参数
     * @return 查询请求SearchRequest
     */
    public static SearchRequest getMatchSearchRequest(RequestParameter param) {
        // Request
        SearchRequest request = new SearchRequest(INDEX_NAME);
        // DSL
        String key = param.getKey();
        if (null == key || "".equals(key)) {
            request.source().query(QueryBuilders.matchAllQuery());
        } else {
            request.source().query(QueryBuilders.matchQuery("all", key));
        }
        // page
        int page = param.getPage() == null ? 1 : param.getPage();
        int size = param.getSize() == null ? 10 : param.getSize();
        request.source().from((page - 1) * size).size(size);
        // sort by distance of input location
        String location = param.getLocation();
        if (location != null && !"".equals(location)) {
            request.source().sort(SortBuilders.geoDistanceSort("location", new GeoPoint(location))
                    .order(SortOrder.ASC).unit(DistanceUnit.KILOMETERS));
        }
        return request;
    }

    /**
     * 构建复合查询
     * @param param 查询参数
     * @return 复合查询构建器，该构建器亦可用作算分查询的原始查询
     */
    public static BoolQueryBuilder getBooleanQuery(RequestParameter param) {
        // 原始查询
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        String city = param.getCity();
        if (city != null && !"".equals(city)) {
            query.must(QueryBuilders.termQuery("city", city));
        }
        String brand = param.getBrand();
        if (brand != null && !"".equals(brand)) {
            query.must(QueryBuilders.termQuery("brand", brand));
        }
        String starName = param.getStarName();
        if (starName != null && !"".equals(starName)) {
            query.must(QueryBuilders.termQuery("starName", starName));
        }
        String minPrice = param.getMinPrice();
        String maxPrice = param.getMaxPrice();
        if (minPrice != null && !"".equals(minPrice)) {
            query.filter(QueryBuilders.rangeQuery("price").gte(minPrice));
        }
        if (maxPrice != null && !"".equals(maxPrice)) {
            query.filter(QueryBuilders.rangeQuery("price").lte(maxPrice));
        }
        return query;
    }

    /**
     * 构建算分查询，算分查询中包含原始查询（这里使用复合【布尔】查询）以及算分函数
     * @param param param
     * @return FunctionScoreQueryBuilder构建类
     */
    public static FunctionScoreQueryBuilder getFunctionScoreQueryBuilder(RequestParameter param) {
        // 原始查询
        BoolQueryBuilder boolQueryBuilder = getBooleanQuery(param);
        // 原始查询+算分函数
        return QueryBuilders.functionScoreQuery(boolQueryBuilder, new FunctionScoreQueryBuilder.FilterFunctionBuilder[]{
                // 算分函数：过滤条件（参与重新算分的文档）+ 具体的计算函数
                new FunctionScoreQueryBuilder.FilterFunctionBuilder(QueryBuilders.termQuery("isAd", true),
                        ScoreFunctionBuilders.weightFactorFunction(10))
        });
    }
}
