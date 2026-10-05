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
 * <p>加载真实 Mapper XML，在 H2（MySQL 兼容模式）上验证 selectByOrderIdForUpdate
 * 的悲观锁行为，包括并发场景下的锁等待。
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
                    + "status VARCHAR(32), money DECIMAL(10, 2), trade_no VARCHAR(64))");
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
    void terminalRecordWinsOverNewerPendingDuplicate() throws Exception {
        try (SqlSession session = sessionFactory.openSession(false)) {
            try (Statement statement = session.getConnection().createStatement()) {
                statement.execute("UPDATE pay_record SET status = 'SUCCESS', trade_no = 'trade-1' WHERE id = 1");
                statement.execute("INSERT INTO pay_record VALUES (2, 101, 'WAIT_PAY', 10.00, NULL)");
            }
            session.commit();
        }
        try (SqlSession session = sessionFactory.openSession(false)) {
            PayRecordMapper mapper = session.getMapper(PayRecordMapper.class);
            assertEquals(1L, mapper.selectByOrderId(101L).getId());
            assertEquals(2, mapper.selectByOrderIdForUpdate(101L).size());
            assertEquals(1L, mapper.selectByOrderIdForUpdate(101L).get(0).getId());
        }
    }

    @Test
    void secondCallbackWaitsForFirstCallbackToCommit() throws Exception {
        try (SqlSession setup = sessionFactory.openSession(false);
             Statement statement = setup.getConnection().createStatement()) {
            statement.execute("INSERT INTO pay_record VALUES (2, 101, 'WAIT_PAY', 10.00, NULL)");
            setup.commit(true);
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try (SqlSession first = sessionFactory.openSession(false)) {
            var lockedRecords = first.getMapper(PayRecordMapper.class).selectByOrderIdForUpdate(101L);
            assertEquals(2, lockedRecords.size());
            PayRecord locked = lockedRecords.get(0);
            assertEquals("WAIT_PAY", locked.getStatus());
            try (Statement statement = first.getConnection().createStatement()) {
                statement.execute("UPDATE pay_record SET status = 'SUCCESS', trade_no = 'trade-1' WHERE id = 2");
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
}
