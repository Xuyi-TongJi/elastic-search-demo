package cn.itcast.hotel.pojo;

import lombok.Data;

/**
 * 请求参数映射类
 * @author xuyitjuseu
 */
@Data
public class RequestParameter {
    private String key;
    private Integer page;
    private Integer size;
    private String sortBy;
    private String city;
    private String brand;
    private String starName;
    private String minPrice;
    private String maxPrice;
    private String location;
}
