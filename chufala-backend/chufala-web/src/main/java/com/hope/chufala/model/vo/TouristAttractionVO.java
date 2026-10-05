package com.hope.chufala.model.vo;

/**
 * 景点 VO（手写 getter/setter 版本）。
 *
 * <p>当前仅被 perf.ListBenchmark 用作性能基准测试的数据载体，未参与接口返回；
 * 接口返回的景点结构请参考 {@link AttractionInfoVO}。
 *
 * @author 谢光湘
 */
public class TouristAttractionVO {
        /** 景点 ID */
        private Long id;
        /** 景点名称 */
        private String name;
        /** 位置描述 */
        private String location;
        /** 评分 */
        private Double rating;
        /** 景点描述 */
        private String description;

        /** 无参构造，供反射/框架实例化使用 */
        public TouristAttractionVO() {}

        /** 全参构造 */
        public TouristAttractionVO(Long id, String name, String location, Double rating, String description) {
            this.id = id;
            this.name = name;
            this.location = location;
            this.rating = rating;
            this.description = description;
        }

        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }

        public Double getRating() { return rating; }
        public void setRating(Double rating) { this.rating = rating; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        @Override
        public String toString() {
            return String.format("Attraction{id=%d, name='%s', location='%s', rating=%.1f}",
                    id, name, location, rating);
        }
    }
