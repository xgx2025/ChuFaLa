package com.hope.chufala;

import com.hope.chufala.mapper.PayRecordMapper;
import com.hope.chufala.model.entity.PayRecord;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 支付记录 Mapper 集成测试（H2 内存库）。
 *
 * <p>加载真实 Mapper XML，在 H2（MySQL 兼容模式）上验证行锁与订单号唯一约束。
 *
 * @author 谢光湘
 */
class PaymentRecordMapperIntegrationTest {
    private SqlSessionFactory sessionFactory;

    @BeforeEach
    void setUp() throws Exception {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:payment_" + UUID.randomUUID()
                + ";MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=5000");
        dataSource.setUser("sa");
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE pay_record (id BIGINT PRIMARY KEY, order_id BIGINT NOT NULL, "
                    + "status VARCHAR(32), money DECIMAL(10, 2), trade_no VARCHAR(64), "
                    + "CONSTRAINT uk_pay_record_order_id UNIQUE (order_id))");
            statement.execute("INSERT INTO pay_record VALUES (1, 101, 'WAIT_PAY', 10.00, NULL)");
        }
        Configuration configuration = new Configuration(new Environment("test", new JdbcTransactionFactory(), dataSource));
        configuration.setMapUnderscoreToCamelCase(true);
        String resource = "com/hope/chufala/mapper/PayRecordMapper.xml";
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
        }
        sessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    @Test
    void orderLookupUsesTheSinglePaymentRecord() throws Exception {
        try (SqlSession session = sessionFactory.openSession(false)) {
            try (Statement statement = session.getConnection().createStatement()) {
                statement.execute("UPDATE pay_record SET status = 'SUCCESS', trade_no = 'trade-1' WHERE id = 1");
            }
            session.commit();
        }
        try (SqlSession session = sessionFactory.openSession(false)) {
            PayRecordMapper mapper = session.getMapper(PayRecordMapper.class);
            assertEquals(1L, mapper.selectByOrderId(101L).getId());
            assertEquals(1, mapper.selectByOrderIdForUpdate(101L).size());
            assertEquals(1L, mapper.selectByOrderIdForUpdate(101L).get(0).getId());
        }
    }

    @Test
    void secondCallbackWaitsForFirstCallbackToCommit() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try (SqlSession first = sessionFactory.openSession(false)) {
            var lockedRecords = first.getMapper(PayRecordMapper.class).selectByOrderIdForUpdate(101L);
            assertEquals(1, lockedRecords.size());
            PayRecord locked = lockedRecords.get(0);
            assertEquals("WAIT_PAY", locked.getStatus());
            try (Statement statement = first.getConnection().createStatement()) {
                statement.execute("UPDATE pay_record SET status = 'SUCCESS', trade_no = 'trade-1' WHERE id = 1");
            }
            Future<String> second = executor.submit(() -> {
                try (SqlSession session = sessionFactory.openSession(false)) {
                    return session.getMapper(PayRecordMapper.class).selectByOrderIdForUpdate(101L).get(0).getStatus();
                }
            });
            assertThrows(TimeoutException.class, () -> second.get(200, TimeUnit.MILLISECONDS));
            // 变更经 JDBC Statement 执行，MyBatis 不知道会话已变脏，需强制提交。
            first.commit(true);
            assertEquals("SUCCESS", second.get(5, TimeUnit.SECONDS));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void concurrentPaymentCreationIsRejectedByOrderUniqueIndex() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try (SqlSession first = sessionFactory.openSession(false)) {
            try (Statement statement = first.getConnection().createStatement()) {
                statement.execute("INSERT INTO pay_record VALUES (2, 202, 'WAIT_PAY', 10.00, NULL)");
            }
            Future<String> second = executor.submit(() -> {
                try (SqlSession session = sessionFactory.openSession(false)) {
                    try (Statement statement = session.getConnection().createStatement()) {
                        statement.execute("INSERT INTO pay_record VALUES (3, 202, 'WAIT_PAY', 10.00, NULL)");
                        session.commit(true);
                        return "inserted";
                    } catch (java.sql.SQLException e) {
                        return e.getSQLState();
                    }
                }
            });
            assertThrows(TimeoutException.class, () -> second.get(200, TimeUnit.MILLISECONDS));
            first.commit(true);
            assertEquals("23505", second.get(5, TimeUnit.SECONDS));
            try (SqlSession check = sessionFactory.openSession();
                 Statement statement = check.getConnection().createStatement();
                 ResultSet rows = statement.executeQuery("SELECT COUNT(*) FROM pay_record WHERE order_id = 202")) {
                rows.next();
                assertEquals(1, rows.getInt(1));
            }
        } finally {
            executor.shutdownNow();
        }
    }
}
