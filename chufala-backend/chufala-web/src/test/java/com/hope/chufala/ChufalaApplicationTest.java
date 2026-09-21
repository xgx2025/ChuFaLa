package com.hope.chufala;

import cn.hutool.core.util.IdUtil;
import com.hope.chufala.common.util.EmailVerificationCodeUtils;
import com.hope.chufala.common.util.Gcj02DistanceCalculator;
import com.hope.chufala.model.vo.AttractionInfoVO;
import com.hope.chufala.model.vo.HotelInfoVO;
import com.hope.chufala.model.vo.TravelItineraryVO;
import com.hope.chufala.service.IAttractionService;
import com.hope.chufala.mq.MessageProducer;
import com.hope.chufala.agent.AgentService;
import com.hope.chufala.service.impl.HotelServiceImpl;

import com.hope.chufala.agent.tool.AttractionTools;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.GraphStateException;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.UrlResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;

import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootTest
public class ChufalaApplicationTest {
    @Autowired
    private MessageProducer messageProducer;

    @Autowired
    private IAttractionService attractionService;
    @Autowired
    private AgentService agentService;
    @Autowired
    private HotelServiceImpl hotelService;

    @Resource(name = "doubao")
    private ChatClient chatClient;

    @Autowired
    private AttractionTools attractionTools;
    @Autowired
    private EmailVerificationCodeUtils emailVerificationCodeUtils;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void contextProductMessage(){
        messageProducer.sendMessage("order.hotel.direct","order.hotel.key","xgx：hello world");
    }

    @Test
    void textArray(){
        int[] arr = new int[10];
        Class<?> clazz = arr.getClass();
        System.out.println(clazz.getTypeName());
    }

    @Test
    void testQueryAttraction(){
        List<AttractionInfoVO> list=attractionService.queryAttractionByCity("张家界");
        System.out.println(list);
    }

//    @Test
//    void testPlan(){
//        //获取当前日期
//        LocalDate now = LocalDate.now();
//        UserPlanDTO userPlanDTO = new UserPlanDTO();
//        userPlanDTO.setDestination("张家界");
//        userPlanDTO.setPeople("2");
//        userPlanDTO.setStartDate(now.toString());
//        userPlanDTO.setDayNum("3");
//        try {
//            TravelItineraryVO travelItinerary = agentService.planTravel(userPlanDTO,new SseEmitter(60L));
//            log.info("测试结果：{}",travelItinerary);
//        } catch (GraphStateException e) {
//            throw new RuntimeException(e);
//        }
//    }

    @Test
    void testStructuredOutput(){
        BeanOutputConverter<TravelItineraryVO> converter = new BeanOutputConverter<>(TravelItineraryVO.class);
        String format = converter.getFormat();
        System.out.println(format);

    }

    @Test
    void testGetNow(){
        LocalDate now = LocalDate.now();
        System.out.println(now.toString());
    }

    @Test
    void testCalculateDistance(){
        Double[] position1 = new Double[2];
        Double[] position2 = new Double[2];
        position1[0] = 110.488;
        position1[1] = 29.117;
        position2[0] = 110.535;
        position2[1] = 29.122;
        Double distance = Gcj02DistanceCalculator.calculateDistance(position1, position2);
        DecimalFormat df = new DecimalFormat("#.00");
        System.out.println(df.format(distance)+"km");
    }

    @Test
    void testQueryAttractionPosition(){
        Double[] position = attractionService.queryAttractionPositionById(1L);
        log.info("结果为");
        for(Double value : position) {
            System.out.print(value + " ");
        }
    }

    @Test
    void testCandidateHotel(){
        List<HotelInfoVO> hotelInfoList = hotelService.findHotelSimpleByCity("张家界");
        System.out.println(hotelInfoList);
    }

    @Test //图像识别
    void testImageRecognition(){
        String response = chatClient
                .prompt()
                .user("图片中展示的是哪里：\n" +
                        "https://chufala.oss-cn-shenzhen.aliyuncs.com/ec165819-b89a-4f8c-8c28-c6b42a400c8a.jpg")
                .call()
                .content();
        System.out.println("识别结果："+response);
    }

    @Test
    void testImageRecognition2(){
        UrlResource resource = null;
        try {
             resource = new UrlResource("https://chufala.oss-cn-shenzhen.aliyuncs.com/ec165819-b89a-4f8c-8c28-c6b42a400c8a.jpg");
        } catch (MalformedURLException e) {
            log.error("图片地址错误",e);
            throw new RuntimeException(e);
        }
        UrlResource finalResource = resource;
        String response = chatClient
                .prompt()
                .user(u->u.text("请识别下面图片中的景点名称：").media(MediaType.IMAGE_JPEG,finalResource))
                .call()
                .content();
        System.out.println("识别结果："+response);
    }

    @Test
    void testAttractionTool(){
        String result = attractionTools.searchAttractionsByKeyword("七十二");
        System.out.println("工具调用结果："+result);
    }

    @Test
    void testSendEmail(){
        emailVerificationCodeUtils.generateAndSendCode("test@example.com","123456");
    }
    @Test
    void testRedis(){
        stringRedisTemplate.opsForValue().set("test:key1","hello");
        stringRedisTemplate.opsForValue().set("test:key2","world");
        stringRedisTemplate.expire("test:key1",30L,TimeUnit.MINUTES);
        stringRedisTemplate.expire("test:key2",30L,TimeUnit.MINUTES);
        String s1 = stringRedisTemplate.opsForValue().get("test:key1");
        String s2 = stringRedisTemplate.opsForValue().get("test:key2");
        System.out.println("s1="+s1);
        System.out.println("s2="+s2);
        stringRedisTemplate.opsForHash().put("test:user:1","name","xgx");
        stringRedisTemplate.opsForHash().put("test:user:1","age","23");
        stringRedisTemplate.opsForHash().put("test:user:1","email","test@example.com");
        stringRedisTemplate.expire("test:user:1",30L, TimeUnit.MINUTES);
        String name = Optional.ofNullable(stringRedisTemplate.opsForHash().get("test:user:1","name")).map(Object::toString).orElse("");
        System.out.println("name="+name);
    }

    @Test
    void testGenerateId(){
        System.out.println(IdUtil.getSnowflakeNextId());
    }
    //116.016802,40.356188
}
