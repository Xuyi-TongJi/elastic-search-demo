package cn.itcast.hotel.lang;

import cn.itcast.hotel.pojo.HotelDoc;
import lombok.Data;

import java.util.List;

/**
 * @author xuyitjuseu
 */
@Data
public class PageResult {
    private Long total;
    private List<HotelDoc> hotels;
}
