package com.hope.chufala.perf;

import com.hope.chufala.model.vo.TouristAttractionVO;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ListBenchmark {

    /**
     * 测试集合插入性能（支持动态数据量）
     * @param list 待测试集合实例
     * @param dataSize 测试数据量
     * @return 插入耗时（毫秒）
     */

    public static long testPerformance(List<TouristAttractionVO> list, int dataSize) {
        TouristAttractionVO[] testData = generateTestDataArray(dataSize);

        long startTime = System.nanoTime();

        // 使用数组遍历，消除集合遍历影响
        for (TouristAttractionVO attraction : testData) {
            list.add(attraction);
        }

        long endTime = System.nanoTime();
        return TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
    }

    private static TouristAttractionVO[] generateTestDataArray(int count) {
        TouristAttractionVO[] data = new TouristAttractionVO[count];
        Random random = new Random();
        String[] locations = {"北京", "上海", "杭州", "成都", "西安", "南京", "苏州", "厦门"};
        String[] names = {"故宫", "长城", "西湖", "外滩", "大熊猫基地", "兵马俑", "夫子庙", "鼓浪屿"};

        for (int i = 0; i < count; i++) {
            TouristAttractionVO attraction = new TouristAttractionVO();
            attraction.setId((long) (i + 1));
            attraction.setName(names[random.nextInt(names.length)] + (i + 1));
            attraction.setLocation(locations[random.nextInt(locations.length)]);
            attraction.setRating(3.0 + random.nextDouble() * 2.0);
            attraction.setDescription("美丽的旅游景点，值得一游");
            data[i] = attraction;
        }
        return data;
    }


}