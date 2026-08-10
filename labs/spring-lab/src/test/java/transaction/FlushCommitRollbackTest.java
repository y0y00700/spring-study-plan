package transaction;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(
        classes = FlushCommitRollbackTest.TestApplication.class,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:tx02;DB_CLOSE_DELAY=-1",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.open-in-view=false",
                "spring.jpa.show-sql=true"
        }
)
class FlushCommitRollbackTest {

    @Autowired
    private Tx02Service service;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void commitsAfterNormalReturn() {
        long id = 101L;

        Observation observation = service.persistFlushAndReturn(id);

        assertThat(observation.beforeFlushSameConnection())
                .isEqualTo(0);

        assertThat(observation.afterFlushSameConnection())
                .isEqualTo(1);

        assertThat(observation.beforeCompletionOtherConnection())
                .isEqualTo(0);

        assertThat(finalCount(id))
                .isEqualTo(1);
    }

    @Test
    void rollsBackAfterRuntimeException() {
        long id = 102L;

        ExpectedRollback exception = assertThrows(
                ExpectedRollback.class,
                () -> service.persistFlushAndFail(id)
        );

        Observation observation = exception.observation();

        assertThat(observation.beforeFlushSameConnection())
                .isEqualTo(0);

        assertThat(observation.afterFlushSameConnection())
                .isEqualTo(1);

        assertThat(observation.beforeCompletionOtherConnection())
                .isEqualTo(0);

        assertThat(finalCount(id))
                .isEqualTo(0);
    }

    private long finalCount(long id) {
        return jdbcTemplate.queryForObject(
                "select count(*) from tx_record where id = ?",
                Long.class,
                id
        );
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @EntityScan(basePackageClasses = TxRecord.class)
    @Import(Tx02Service.class)
    static class TestApplication {
    }
}

@Service
class Tx02Service {

    private final EntityManager entityManager;
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    Tx02Service(
            EntityManager entityManager,
            JdbcTemplate jdbcTemplate,
            DataSource dataSource
    ) {
        this.entityManager = entityManager;
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public Observation persistFlushAndReturn(long id) {
        entityManager.persist(new TxRecord(id));

        long beforeFlush = sameConnectionCount(id);

        entityManager.flush();

        long afterFlush = sameConnectionCount(id);
        long otherConnection = otherConnectionCount(id);

        return new Observation(beforeFlush, afterFlush, otherConnection);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void persistFlushAndFail(long id) {
        entityManager.persist(new TxRecord(id));

        long beforeFlush = sameConnectionCount(id);

        entityManager.flush();

        long afterFlush = sameConnectionCount(id);
        long otherConnection = otherConnectionCount(id);

        Observation observation =
                new Observation(beforeFlush, afterFlush, otherConnection);

        throw new ExpectedRollback(observation);
    }

    private long sameConnectionCount(long id) {
        return jdbcTemplate.queryForObject(
                "select count(*) from tx_record where id = ?",
                Long.class,
                id
        );
    }

    private long otherConnectionCount(long id) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "select count(*) from tx_record where id = ?"
                )
        ) {
            connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getLong(1);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException(exception);
        }
    }
}

@Entity
@Table(name = "tx_record")
class TxRecord {

    @Id
    private Long id;

    protected TxRecord() {
    }

    TxRecord(Long id) {
        this.id = id;
    }
}

record Observation(
        long beforeFlushSameConnection,
        long afterFlushSameConnection,
        long beforeCompletionOtherConnection
) {
}

class ExpectedRollback extends RuntimeException {

    private final Observation observation;

    ExpectedRollback(Observation observation) {
        super("rollback experiment");
        this.observation = observation;
    }

    Observation observation() {
        return observation;
    }
}