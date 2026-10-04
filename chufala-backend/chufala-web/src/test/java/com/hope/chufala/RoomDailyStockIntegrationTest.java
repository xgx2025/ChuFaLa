package com.hope.chufala;

import com.hope.chufala.mapper.RoomDailyStockMapper;
import com.hope.chufala.service.impl.RoomServiceImpl;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.RunScript;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoomDailyStockIntegrationTest {
    private static final Long ROOM_TYPE_ID = 1L;
    private static final LocalDate FIRST_NIGHT = LocalDate.of(2026, 10, 5);

    private JdbcDataSource dataSource;
    private SqlSessionFactory sessionFactory;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:daily_stock_" + UUID.randomUUID()
                + ";MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000");
        dataSource.setUser("sa");

        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE room (id BIGINT PRIMARY KEY, stock INT NOT NULL)");
            statement.execute("CREATE TABLE hotel_order (room_type_id BIGINT, order_status VARCHAR(20), "
                    + "check_in DATE, check_out DATE, room_count INT)");
            statement.execute("CREATE TABLE room_daily_stock (room_type_id BIGINT NOT NULL, stay_date DATE NOT NULL, "
                    + "available_stock INT NOT NULL CHECK (available_stock >= 0), "
                    + "PRIMARY KEY (room_type_id, stay_date))");
            statement.execute("INSERT INTO room (id, stock) VALUES (1, 2)");
        }

        Configuration configuration = new Configuration(new Environment("test", new JdbcTransactionFactory(), dataSource));
        String resource = "com/hope/chufala/mapper/RoomDailyStockMapper.xml";
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
        }
        sessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    @Test
    void bookingsOnDifferentNightsDoNotConsumeEachOthersStock() {
        assertEquals(1, reserve(FIRST_NIGHT, 2));
        assertEquals(1, reserve(FIRST_NIGHT.plusDays(1), 2));
        assertEquals(0, available(FIRST_NIGHT));
        assertEquals(0, available(FIRST_NIGHT.plusDays(1)));
        assertEquals(2, available(FIRST_NIGHT.plusDays(2)));
    }

    @Test
    void stayAvailabilityUsesTheLeastStockAcrossAllNights() {
        assertEquals(1, reserve(FIRST_NIGHT, 1));
        assertEquals(1, reserve(FIRST_NIGHT.plusDays(1), 2));

        try (SqlSession session = sessionFactory.openSession()) {
            RoomServiceImpl service = new RoomServiceImpl();
            ReflectionTestUtils.setField(service, "roomDailyStockMapper", session.getMapper(RoomDailyStockMapper.class));
            assertEquals(0, service.getAvailableStock(ROOM_TYPE_ID, FIRST_NIGHT, FIRST_NIGHT.plusDays(2)));
            assertEquals(1, service.getAvailableStock(ROOM_TYPE_ID, FIRST_NIGHT, FIRST_NIGHT.plusDays(1)));
        }
    }

    @Test
    void existingOrderOccupancyIsIncludedWhenDateIsFirstInitialized() throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("INSERT INTO hotel_order VALUES (1, '待支付', DATE '2026-10-05', DATE '2026-10-07', 1)");
        }

        assertEquals(1, available(FIRST_NIGHT));
        assertEquals(1, reserve(FIRST_NIGHT, 1));
        assertEquals(0, available(FIRST_NIGHT));
        assertEquals(1, available(FIRST_NIGHT.plusDays(1)));
        assertEquals(2, available(FIRST_NIGHT.plusDays(2)));
    }

    @Test
    void migrationRestoresBaseCapacityAndKeepsExistingBookings() throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("UPDATE room SET stock = 0 WHERE id = 1");
            statement.execute("INSERT INTO hotel_order VALUES (1, '未支付', DATE '2026-10-05', DATE '2026-10-07', 1)");
            statement.execute("INSERT INTO hotel_order VALUES (1, '已支付', DATE '2026-10-10', DATE '2026-10-11', 1)");
            statement.execute("INSERT INTO hotel_order VALUES (1, '已取消', DATE '2026-10-05', DATE '2026-10-06', 1)");
        }
        // H2 的 MySQL 模式不支持 START TRANSACTION，事务边界由测试连接提供。
        String migration = Files.readString(Path.of("..", "db", "migration", "20261004_room_daily_stock.sql"))
                .replace("START TRANSACTION;", "").replace("COMMIT;", "");
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            RunScript.execute(connection, new StringReader(migration));
            connection.commit();
            RunScript.execute(connection, new StringReader(migration));
            connection.commit();
        }

        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            try (var result = statement.executeQuery("SELECT stock FROM room WHERE id = 1")) {
                result.next();
                assertEquals(2, result.getInt(1));
            }
            try (var result = statement.executeQuery("SELECT COUNT(*) FROM hotel_order WHERE order_status = '待支付'")) {
                result.next();
                assertEquals(1, result.getInt(1));
            }
        }
        assertEquals(1, available(FIRST_NIGHT));
        assertEquals(1, available(FIRST_NIGHT.plusDays(1)));
        assertEquals(2, available(FIRST_NIGHT.plusDays(2)));
    }

    @Test
    void failedSecondNightRollsBackFirstNight() {
        assertEquals(1, reserve(FIRST_NIGHT.plusDays(1), 2));
        try (SqlSession session = sessionFactory.openSession(false)) {
            RoomDailyStockMapper mapper = session.getMapper(RoomDailyStockMapper.class);
            mapper.initializeStock(ROOM_TYPE_ID, FIRST_NIGHT);
            assertEquals(1, mapper.decreaseStock(ROOM_TYPE_ID, FIRST_NIGHT, 1));
            mapper.initializeStock(ROOM_TYPE_ID, FIRST_NIGHT.plusDays(1));
            assertEquals(0, mapper.decreaseStock(ROOM_TYPE_ID, FIRST_NIGHT.plusDays(1), 1));
            session.rollback();
        }
        assertEquals(2, available(FIRST_NIGHT));
        assertEquals(0, available(FIRST_NIGHT.plusDays(1)));
    }

    @Test
    void concurrentBookingsOnSameNightNeverMakeStockNegative() throws Exception {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("UPDATE room SET stock = 1 WHERE id = 1");
        }
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Integer> first = executor.submit(() -> concurrentReserve(ready, start));
            Future<Integer> second = executor.submit(() -> concurrentReserve(ready, start));
            ready.await(5, TimeUnit.SECONDS);
            start.countDown();
            assertEquals(1, first.get(15, TimeUnit.SECONDS) + second.get(15, TimeUnit.SECONDS));
            assertEquals(0, available(FIRST_NIGHT));
        } finally {
            executor.shutdownNow();
        }
    }

    private int concurrentReserve(CountDownLatch ready, CountDownLatch start) throws Exception {
        ready.countDown();
        start.await();
        return reserve(FIRST_NIGHT, 1);
    }

    private int reserve(LocalDate stayDate, int roomCount) {
        try (SqlSession session = sessionFactory.openSession(false)) {
            RoomDailyStockMapper mapper = session.getMapper(RoomDailyStockMapper.class);
            mapper.initializeStock(ROOM_TYPE_ID, stayDate);
            int changed = mapper.decreaseStock(ROOM_TYPE_ID, stayDate, roomCount);
            if (changed == 1) {
                session.commit();
            } else {
                session.rollback();
            }
            return changed;
        }
    }

    private int available(LocalDate stayDate) {
        try (SqlSession session = sessionFactory.openSession()) {
            return session.getMapper(RoomDailyStockMapper.class).selectAvailableStock(ROOM_TYPE_ID, stayDate);
        }
    }
}
