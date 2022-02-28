package cn.itcast.hotel.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author xuyitjuseu
 */
@Data
@NoArgsConstructor
public class HotelDoc {
    private Long id;
    private String name;
    private String address;
    private Integer price;
    private Integer score;
    private String brand;
    private String city;
    private String starName;
    private String business;
    private String location;
    private String pic;
    /**
     * 根据距离排序的距离值
     */
    private Object distance;
    /**
     * 重新算分
     */
    private Boolean isAd;
    /**
     * 自动补全
     */
    private List<String> suggestion;

    public HotelDoc(Hotel hotel) {
        this.id = hotel.getId();
        this.name = hotel.getName();
        this.address = hotel.getAddress();
        this.price = hotel.getPrice();
        this.score = hotel.getScore();
        this.brand = hotel.getBrand();
        this.city = hotel.getCity();
        this.starName = hotel.getStarName();
        this.business = hotel.getBusiness();
        this.location = hotel.getLatitude() + ", " + hotel.getLongitude();
        this.pic = hotel.getPic();
        String regex = "/";
        if (this.business.contains(regex)) {
            String[] strings = this.business.split(regex);
            List<String> list = new ArrayList<>();
            list.add(this.brand);
            list.addAll(Arrays.asList(strings));
            this.suggestion = list;
        } else {
            this.suggestion = Arrays.asList(this.brand, this.business);
        }
    }
}
