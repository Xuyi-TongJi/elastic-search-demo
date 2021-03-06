package cn.itcast.hotel;

import cn.itcast.hotel.constant.HotelConstants;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.HotelDoc;
import cn.itcast.hotel.service.IHotelService;
import com.alibaba.fastjson.JSON;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.SuggestBuilders;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class HotelDemoApplicationTests {

    RestHighLevelClient rc;

    @Autowired
    IHotelService hotelService;

    @BeforeEach
    void setUp() {
        this.rc = new RestHighLevelClient(RestClient.builder(HttpHost.create("http://122.112.168.7:9200")));
    }

    /**
     * ????????????
     * @throws IOException IOException
     */
    @Test
    void testCreateMapping() throws IOException {
        // ??????request??????
        CreateIndexRequest request = new CreateIndexRequest("hotel2");
        // ??????MAPPING
        request.source(HotelConstants.MAPPING, XContentType.JSON);
        // ???????????????????????????rc.indices()
        rc.indices().create(request, RequestOptions.DEFAULT);
    }

    @Test
    void testGetMapping() {
        GetIndexRequest request = new GetIndexRequest("hotel2");
        try {
            assertTrue(rc.indices().exists(request, RequestOptions.DEFAULT));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ??????????????????
     *
     * @throws IOException IOException
     */
    @Test
    void testIndex() throws IOException {
        Hotel hotel = hotelService.getById(61083L);
        IndexRequest indexRequest = new IndexRequest("hotel2").id(hotel.getId().toString());
        // java object -> ??????
        HotelDoc hotelDoc = new HotelDoc(hotel);
        // ??????  -> JSON
        indexRequest.source(JSON.toJSONString(hotelDoc), XContentType.JSON);
        rc.index(indexRequest, RequestOptions.DEFAULT);
    }

    /**
     * ??????id????????????
     */
    @Test
    void testGet() {
        GetRequest request = new GetRequest("hotel2", "38812");
        GetResponse response = null;
        try {
            response = rc.get(request, RequestOptions.DEFAULT);
            String sourceAsString = response.getSourceAsString();
            System.out.println(sourceAsString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ???????????????????????????
     * ???????????????????????????????????????????????????(id????????????)
     */
    @Test
    void testUpdate() {
        UpdateRequest request = new UpdateRequest("hotel2", "433576");
        request.doc("isAD", true);
        try {
            rc.update(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ????????????
     */
    @Test
    void testDelete() {
        DeleteRequest request = new DeleteRequest("hotel2", "61083");
        try {
            rc.delete(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ????????????
     */
    @Test
    void testBulk() {
        BulkRequest request = new BulkRequest();
        List<Hotel> hotelList
                = hotelService.list();
        for (Hotel hotel: hotelList) {
            HotelDoc hotelDoc = new HotelDoc(hotel);
            request.add(new IndexRequest("hotel").id(hotelDoc.getId().toString())
                    .source(JSON.toJSONString(hotelDoc), XContentType.JSON));
        }
        try {
            rc.bulk(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void match(AbstractQueryBuilder<? extends AbstractQueryBuilder<?>> queryBuilders) {
        SearchRequest request = new SearchRequest("hotel2");
        // DSL parameter
        request.source().query(queryBuilders);
        try {
            SearchResponse response = rc.search(request, RequestOptions.DEFAULT);
            // resolve response
            resolveResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void resolveResponse(SearchResponse response) {
        SearchHits hits = response.getHits();
        long total = hits.getTotalHits().value;
        SearchHit[] hitList = hits.getHits();
        System.out.println("??????" + total + "?????????");
        for (SearchHit hit: hitList) {
            String json = hit.getSourceAsString();
            System.out.println(json);
        }
    }

    /**
     * match_all??????
     */
    @Test
    void testMatchAll() {
        MatchAllQueryBuilder queryBuilders = QueryBuilders.matchAllQuery();
        match(queryBuilders);
    }

    /**
     * match?????????????????????text
     */
    @Test
    void matchQuery() {
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("all", "7???");
        match(matchQueryBuilder);
        MultiMatchQueryBuilder multiMatchQueryBuilder
                = QueryBuilders.multiMatchQuery("??????", "name", "business");
        match(multiMatchQueryBuilder);
    }

    /**
     * term?????????????????????keyword???
     * range???????????????????????????
     */
    @Test
    void termQuery() {
        match(QueryBuilders.termQuery("city", "??????"));
        match(QueryBuilders.rangeQuery("price").gte(100).lte(150));
    }

    /**
     * ????????????
     */
    @Test
    void boolQuery() {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        boolQueryBuilder.must(QueryBuilders.termQuery("city", "??????"));
        boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").lte(250));
        match(boolQueryBuilder);
    }

    /**
     * ??????
     */
    @Test
    void sort() {
        SearchRequest request = new SearchRequest("hotel2");
        // suppose page, size from front
        int page = 1, size = 5;
        request.source().query(QueryBuilders.matchAllQuery());
        // ??????
        request.source().sort("price", SortOrder.ASC);
        request.source().from((page - 1) * size).size(5);
        try {
            SearchResponse response = rc.search(request, RequestOptions.DEFAULT);
            // resolve response
            resolveResponse(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ????????????
     */
    @Test
    void highlight() {
        SearchRequest request = new SearchRequest("hotel2");
        request.source().query(QueryBuilders.matchQuery("all", "7???"));
        // ???????????????????????????????????????
        request.source().highlighter(new HighlightBuilder().field("name").requireFieldMatch(false));
        try {
            SearchResponse response = rc.search(request, RequestOptions.DEFAULT);
            // resolve response
            SearchHits hits = response.getHits();
            long total = hits.getTotalHits().value;
            SearchHit[] hitList = hits.getHits();
            System.out.println("??????" + total + "?????????");
            for (SearchHit hit: hitList) {
                String json = hit.getSourceAsString();
                HotelDoc hotelDoc = JSON.parseObject(json, HotelDoc.class);
                // ????????????
                Map<String, HighlightField> highlightFieldMap = hit.getHighlightFields();
                if (!CollectionUtils.isEmpty(highlightFieldMap)) {
                    HighlightField highlightField = highlightFieldMap.get("name");
                    if (highlightField != null) {
                        String name = highlightField.getFragments()[0].string();
                        hotelDoc.setName(name);
                        System.out.println(hotelDoc.getName());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ????????????
     */
    @Test
    void testAggregation() {
        SearchRequest request = new SearchRequest("hotel2");
        // ?????????????????????
        request.source().size(0);
        request.source().aggregation(AggregationBuilders.terms("brandAgg").field("brand").size(20));
        try {
            SearchResponse response = rc.search(request, RequestOptions.DEFAULT);
            // resolve response
            Terms brandTerms = response.getAggregations().get("brandAgg");
            List<? extends Terms.Bucket> buckets = brandTerms.getBuckets();
            for (Terms.Bucket bucket: buckets) {
                String key = bucket.getKeyAsString();
                long docCount = bucket.getDocCount();
                System.out.println(key + "  " + docCount);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ??????????????????
     */
    @Test
    void testSuggest() {
        SearchRequest request = new SearchRequest("hotel");
        request.source().suggest(new SuggestBuilder().addSuggestion(
                // addSuggestion???????????????????????????
                "suggestions",
                SuggestBuilders.completionSuggestion("suggestion")
                        .prefix("hz").skipDuplicates(true).size(10)
        ));
        try {
            SearchResponse response = rc.search(request, RequestOptions.DEFAULT);
            // resolve response
            Suggest suggest = response.getSuggest();
            // ??????????????????(addSuggestion?????????????????????)
            CompletionSuggestion suggestion = suggest.getSuggestion("suggestions");
            for (CompletionSuggestion.Entry.Option option : suggestion.getOptions()) {
                String text = option.getText().string();
                System.out.println(text);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
