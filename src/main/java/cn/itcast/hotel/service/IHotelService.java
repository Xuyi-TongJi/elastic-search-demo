package cn.itcast.hotel.service;

import cn.itcast.hotel.lang.PageResult;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.RequestParameter;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * @author xuyitjuseu
 */
public interface IHotelService extends IService<Hotel> {

    /**
     * search方法
     * @param param 查询参数映射
     * @return 公共返回对象,包含总条数和数据列表
     */
    PageResult search(RequestParameter param);

    /**
     * 多条件组合查询
     * @param param 查询参数映射
     * @return 公共返回对象，包含总条数和数据列表
     */
    PageResult booleanQuery(RequestParameter param);

    /**
     * 算分查询，isAD=true的文档将会被置顶
     * @param param 查询参数映射
     * @return 公共返回对象
     */
    PageResult functionScoreQuery(RequestParameter param);
}
