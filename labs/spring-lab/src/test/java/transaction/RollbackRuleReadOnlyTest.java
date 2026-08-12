package transaction;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RollbackRuleReadOnlyTest.Config.class)
class RollbackRuleReadOnlyTest {

    @Autowired
    private TxService txService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanUp() {
        jdbcTemplate.update("delete from tx03_record");
    }

    @Test
    void rollback_result_depends_on_exception_type_and_rule() {
        assertThatThrownBy(() -> txService.checkedDefault(101L))
                .isInstanceOf(CheckedTxException.class);

        assertThatThrownBy(() -> txService.runtimeDefault(102L))
                .isInstanceOf(RuntimeTxException.class);

        assertThatThrownBy(() -> txService.checkedRollbackFor(103L))
                .isInstanceOf(CheckedTxException.class);

        // 학습자가 작성하고 검토한 예상값
        assertThat(countById(101L)).isEqualTo(1);
        assertThat(countById(102L)).isEqualTo(0);
        assertThat(countById(103L)).isEqualTo(0);
    }

    @Test
    void read_only_effect_depends_on_write_path_and_resource() {
        ReadOnlyObservation d1 = txService.jpaPersistReadOnly(201L);

        assertThat(d1.transactionReadOnly()).isTrue();
        assertThat(d1.insideCount()).isEqualTo(0);
        assertThat(countById(201L)).isEqualTo(0);

        ReadOnlyObservation d2 = txService.jdbcInsertReadOnly(202L);

        assertThat(d2.transactionReadOnly()).isTrue();
        assertThat(d2.updateCount()).isEqualTo(1);
        assertThat(d2.insideCount()).isEqualTo(1);
        assertThat(countById(202L)).isEqualTo(1);
    }

    private int countById(long id) {
        return jdbcTemplate.queryForObject(
                "select count(*) from tx03_record where id = ?",
                Integer.class,
                id
        );
    }

    @Configuration
    @EnableTransactionManagement
    static class Config {

        @Bean
        DataSource dataSource() {
            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setDriverClassName("org.h2.Driver");
            dataSource.setUrl(
                    "jdbc:h2:mem:tx03-read-only;DB_CLOSE_DELAY=-1"
            );
            dataSource.setUsername("sa");
            dataSource.setPassword("");
            return dataSource;
        }

        @Bean
        JdbcTemplate jdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }

        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory(
                DataSource dataSource
        ) {
            HibernateJpaVendorAdapter vendorAdapter =
                    new HibernateJpaVendorAdapter();

            LocalContainerEntityManagerFactoryBean factory =
                    new LocalContainerEntityManagerFactoryBean();

            factory.setDataSource(dataSource);
            factory.setJpaVendorAdapter(vendorAdapter);
            factory.setPackagesToScan(
                    RollbackRuleReadOnlyTest.class.getPackageName()
            );
            factory.setJpaPropertyMap(Map.of(
                    "hibernate.hbm2ddl.auto", "create-drop",
                    "hibernate.show_sql", "true",
                    "hibernate.format_sql", "true"
            ));

            return factory;
        }

        @Bean
        PlatformTransactionManager transactionManager(
                EntityManagerFactory entityManagerFactory,
                DataSource dataSource
        ) {
            JpaTransactionManager transactionManager =
                    new JpaTransactionManager(entityManagerFactory);
            transactionManager.setDataSource(dataSource);
            return transactionManager;
        }

        @Bean
        EntityManager entityManager(
                EntityManagerFactory entityManagerFactory
        ) {
            return SharedEntityManagerCreator.createSharedEntityManager(
                    entityManagerFactory
            );
        }

        @Bean
        TxService txService(
                EntityManager entityManager,
                JdbcTemplate jdbcTemplate
        ) {
            return new TxService(entityManager, jdbcTemplate);
        }
    }

    static class TxService {

        private final EntityManager entityManager;
        private final JdbcTemplate jdbcTemplate;

        TxService(
                EntityManager entityManager,
                JdbcTemplate jdbcTemplate
        ) {
            this.entityManager = entityManager;
            this.jdbcTemplate = jdbcTemplate;
        }

        @Transactional
        public void checkedDefault(long id)
                throws CheckedTxException {
            persistAndFlush(id, "checked-default");
            throw new CheckedTxException();
        }

        @Transactional
        public void runtimeDefault(long id) {
            persistAndFlush(id, "runtime-default");
            throw new RuntimeTxException();
        }

        @Transactional(rollbackFor = CheckedTxException.class)
        public void checkedRollbackFor(long id)
                throws CheckedTxException {
            persistAndFlush(id, "checked-rollback-for");
            throw new CheckedTxException();
        }

        @Transactional(readOnly = true)
        public ReadOnlyObservation jpaPersistReadOnly(long id) {
            entityManager.persist(
                    new TxRecord2(id, "jpa-read-only")
            );

            int insideCount = countById(id);

            return new ReadOnlyObservation(
                    null,
                    insideCount,
                    TransactionSynchronizationManager
                            .isCurrentTransactionReadOnly()
            );
        }

        @Transactional(readOnly = true)
        public ReadOnlyObservation jdbcInsertReadOnly(long id) {
            int updateCount = jdbcTemplate.update(
                    "insert into tx03_record(id, label) values (?, ?)",
                    id,
                    "jdbc-read-only"
            );

            int insideCount = countById(id);

            return new ReadOnlyObservation(
                    updateCount,
                    insideCount,
                    TransactionSynchronizationManager
                            .isCurrentTransactionReadOnly()
            );
        }

        private void persistAndFlush(long id, String label) {
            entityManager.persist(new TxRecord2(id, label));
            entityManager.flush();
        }

        private int countById(long id) {
            return jdbcTemplate.queryForObject(
                    "select count(*) from tx03_record where id = ?",
                    Integer.class,
                    id
            );
        }
    }

    record ReadOnlyObservation(
            Integer updateCount,
            int insideCount,
            boolean transactionReadOnly
    ) {
    }

    static class CheckedTxException extends Exception {
    }

    static class RuntimeTxException extends RuntimeException {
    }
}

@Entity
@Table(name = "tx03_record")
class TxRecord2 {

    @Id
    private Long id;

    private String label;

    protected TxRecord2() {
    }

    TxRecord2(Long id, String label) {
        this.id = id;
        this.label = label;
    }
}