package cn.itcast.hotel.service;

import cn.itcast.hotel.pojo.RequestParameter;

import java.util.List;
import java.util.Map;

/**
 * @author xuyitjuseu
 */
public interface IUserService {

    /**
     * 查询城市，星级，品牌的聚合结果
     * 对数据库的所有数据聚合
     * @param param 查询条件，如何为null则为对所有数据进行聚合
     * @param map 字段-聚合名称映射，聚合名称不做强制要求
     * @return map集合，其中key为字段，value为该字段的聚合结果
     */
    Map<String, List<String>> filter(RequestParameter param, Map<String, String> map);
}
