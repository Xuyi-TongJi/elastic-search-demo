package cn.itcast.hotel.controller;

import cn.itcast.hotel.lang.PageResult;
import cn.itcast.hotel.pojo.RequestParameter;
import cn.itcast.hotel.service.IHotelService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author xuyitjuseu
 */
@RestController
@RequestMapping("/hotel")
public class HotelController {

    private final IHotelService hotelService;

    public HotelController(IHotelService hotelService) {
        this.hotelService = hotelService;
    }

    @GetMapping("/list")
    public PageResult search(RequestParameter param) {
        return hotelService.search(param);
    }

    @GetMapping("/filter")
    public PageResult booleanSearch(RequestParameter param) {
        return hotelService.booleanQuery(param);
    }

    @GetMapping("/ad")
    public PageResult functionScore(RequestParameter param) {
        return hotelService.functionScoreQuery(param);
    }

    @GetMapping("/suggestion")
    public List<String> suggestion(String key) {
        return hotelService.suggestion(key);
    }
}
