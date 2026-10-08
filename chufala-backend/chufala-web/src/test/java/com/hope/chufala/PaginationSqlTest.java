package com.hope.chufala;

import com.hope.chufala.util.CursorPaginationUtils;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PaginationSqlTest {
    private static final String MAPPER_ROOT = "com/hope/chufala/mapper/";

    @Test
    void cursorAndPageSizeAreValidated() {
        assertThrows(IllegalArgumentException.class, () -> CursorPaginationUtils.size(51, 10));
        String scope = CursorPaginationUtils.scope("上海", List.of("停车场"));
        String token = CursorPaginationUtils.encode("rating", scope, 4.6, 42L);
        assertEquals(42L, CursorPaginationUtils.decode(token, "rating", scope).id());
        assertNull(CursorPaginationUtils.decode(
                CursorPaginationUtils.encode("rating", scope, null, 43L), "rating", scope).value());
        assertThrows(IllegalArgumentException.class, () -> CursorPaginationUtils.decode(token, "price-asc", scope));
        assertThrows(IllegalArgumentException.class,
                () -> CursorPaginationUtils.decode(token, "rating", CursorPaginationUtils.scope("北京")));
        LocalDateTime bookTime = LocalDateTime.of(2026, 10, 8, 12, 30);
        String orderToken = CursorPaginationUtils.encodeOrder(scope, bookTime, 7L);
        assertEquals(bookTime, CursorPaginationUtils.decodeOrder(orderToken, scope).bookTime());
        assertThrows(IllegalArgumentException.class,
                () -> CursorPaginationUtils.decodeOrder(orderToken, CursorPaginationUtils.scope("北京")));
    }

    @Test
    void hotelCursorSqlUsesSortAndIdWithoutOffset() {
        Configuration configuration = mapper("HotelMapper.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("size", 10);
        params.put("lastId", 42L);
        params.put("lastValue", 4.5);
        params.put("sort", "rating");
        String sql = sql(configuration, "HotelMapper.selectByScoreRankPage", params);
        assertTrue(sql.contains("h.overall_rating"));
        assertTrue(sql.contains("h.id > ?"));
        assertTrue(sql.endsWith("LIMIT ?"));

        params.put("sort", "distance");
        params.put("userLat", 31.2);
        params.put("userLng", 121.5);
        sql = sql(configuration, "HotelMapper.selectByScoreRankPage", params);
        assertTrue(sql.contains("cursorSortValue"));
        assertTrue(sql.contains("ASIN"));
    }

    @Test
    void attractionCountMatchesListCityAndCursorHasLimit() {
        Configuration configuration = mapper("AttractionMapper.xml");
        Map<String, Object> params = new HashMap<>();
        params.put("city", "上海");
        params.put("lastId", 42L);
        params.put("lastValue", 4.5);
        params.put("size", 13);
        String list = sql(configuration, "AttractionMapper.selectAttractionPage", params);
        String count = sql(configuration, "AttractionMapper.selectAttractionCount", params);
        assertTrue(list.contains("a.city = ?"));
        assertTrue(count.contains("city = ?"));
        assertFalse(count.contains("city LIKE"));
        assertTrue(list.endsWith("LIMIT ?"));
        params.put("lastValue", null);
        assertTrue(sql(configuration, "AttractionMapper.selectAttractionPage", params).contains("a.rating IS NULL"));
    }

    @Test
    void equalScoresAndMultipleImagesDoNotDuplicateHotelsAcrossPages() throws Exception {
        Configuration configuration = mapper("HotelMapper.xml");
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:pagination;MODE=MySQL")) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE hotel (id BIGINT PRIMARY KEY, name VARCHAR(50), city VARCHAR(50), "
                        + "address VARCHAR(50), price DOUBLE, original_price DOUBLE, facilities VARCHAR(50), "
                        + "stars INT, overall_rating DOUBLE, review_count BIGINT, longitude DOUBLE, latitude DOUBLE)");
                statement.execute("CREATE TABLE hotel_image (hotel_id BIGINT, sort_order INT, image VARCHAR(50))");
                statement.execute("INSERT INTO hotel (id, overall_rating, price) VALUES "
                        + "(1, 5, 100), (2, 5, 100), (3, 5, 200), (4, 4, 200), (5, NULL, 300)");
                statement.execute("INSERT INTO hotel_image VALUES (1, 1, 'a'), (1, 1, 'b')");
            }
            Map<String, Object> params = new HashMap<>();
            params.put("size", 2);
            params.put("sort", "rating");
            List<Long> first = hotelIds(configuration, params, connection);
            assertEquals(List.of(1L, 2L), first);
            params.put("lastId", 2L);
            params.put("lastValue", 5.0);
            assertEquals(List.of(3L, 4L), hotelIds(configuration, params, connection));
            params.put("lastId", 4L);
            params.put("lastValue", 4.0);
            assertEquals(List.of(5L), hotelIds(configuration, params, connection));
            params.put("lastId", 5L);
            params.put("lastValue", null);
            assertEquals(List.of(), hotelIds(configuration, params, connection));

            params.put("sort", "price-desc");
            params.put("lastId", null);
            params.put("lastValue", null);
            assertEquals(List.of(5L, 4L), hotelIds(configuration, params, connection));
            params.put("lastId", 4L);
            params.put("lastValue", 200.0);
            assertEquals(List.of(3L, 2L), hotelIds(configuration, params, connection));
        }
    }

    private List<Long> hotelIds(Configuration configuration, Map<String, Object> params,
                                Connection connection) throws Exception {
        BoundSql boundSql = configuration.getMappedStatement("com.hope.chufala.mapper.HotelMapper.selectByScoreRankPage")
                .getBoundSql(params);
        try (PreparedStatement statement = connection.prepareStatement(boundSql.getSql())) {
            for (int i = 0; i < boundSql.getParameterMappings().size(); i++) {
                statement.setObject(i + 1, params.get(boundSql.getParameterMappings().get(i).getProperty()));
            }
            List<Long> ids = new ArrayList<>();
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) ids.add(result.getLong("id"));
            }
            return ids;
        }
    }

    private Configuration mapper(String file) {
        Configuration configuration = new Configuration();
        String resource = MAPPER_ROOT + file;
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertNotNull(stream);
            new XMLMapperBuilder(stream, configuration, resource, configuration.getSqlFragments()).parse();
        } catch (Exception e) {
            throw new AssertionError("无法解析分页 Mapper: " + file, e);
        }
        return configuration;
    }

    private String sql(Configuration configuration, String statement, Map<String, Object> params) {
        String namespace = "com.hope.chufala.mapper." + statement;
        BoundSql boundSql = configuration.getMappedStatement(namespace).getBoundSql(params);
        return boundSql.getSql().replaceAll("\\s+", " ").trim();
    }
}
