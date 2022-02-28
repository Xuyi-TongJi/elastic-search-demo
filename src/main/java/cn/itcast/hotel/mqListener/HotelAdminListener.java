package cn.itcast.hotel.mqListener;

import cn.itcast.hotel.constant.MqConstants;
import cn.itcast.hotel.service.IHotelService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author xuyitjuseu
 */
@Component
public class HotelAdminListener {

    private final IHotelService hotelService;

    public HotelAdminListener(IHotelService hotelService) {
        this.hotelService = hotelService;
    }

    /**
     * 监听新增或修改的业务，参数列表为消息的内容
     * @param id 被操作的酒店id
     */
    @RabbitListener(queues = MqConstants.ADD_UPDATE_QUEUE)
    public void listenHotelAddUpdate(Long id) {
        hotelService.updateById(id);
    }

    @RabbitListener(queues = MqConstants.DELETE_QUEUE)
    public void listenHotelDelete(Long id) {
        hotelService.deleteById(id);
    }
}
