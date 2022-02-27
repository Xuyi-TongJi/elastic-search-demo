package cn.itcast.hotel.controller;

import cn.itcast.hotel.pojo.RequestParameter;
import cn.itcast.hotel.service.IUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author xuyitjuseu
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping("/filter")
    public Map<String, List<String>> getFilterMap() {
        HashMap<String, String> map = new HashMap<>(3);
        map.put("city", "cityAgg");
        map.put("starName", "starNameAgg");
        map.put("brand", "brandAgg");
        return userService.filter(null, map);
    }

    @GetMapping("/filterByQuery")
    public Map<String, List<String>> getFilterMapByQuery(RequestParameter param) {
        HashMap<String, String> map = new HashMap<>(3);
        map.put("city", "cityAgg");
        map.put("starName", "starNameAgg");
        map.put("brand", "brandAgg");
        return userService.filter(param, map);
    }
}
