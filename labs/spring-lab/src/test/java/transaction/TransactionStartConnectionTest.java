package transaction;

import java.sql.Connection;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TransactionStartConnectionTest {

    @Test
    void transactionStartsBeforeTargetAndReusesConnection() {
        try (AnnotationConfigApplicationContext context =
                     new AnnotationConfigApplicationContext(
                             TransactionTestConfiguration.class
                     )) {
            ObservationService service =
                    context.getBean(ObservationService.class);

            Boolean expectedActiveBefore = false;
            // TODO: @Transactional 메서드 호출 전 true 또는 false
            Boolean expectedActiveInside = true;
            // TODO: target 메서드 본문 안에서 true 또는 false
            Boolean expectedResourceBoundInside = true;
            // TODO: target 메서드 본문 안에서 true 또는 false
            Boolean expectedSameConnectionInside = true;
            // TODO: 같은 트랜잭션 안에서 얻은 두 Connection이 같으면 true
            Boolean expectedActiveAfter = false;
            // TODO: @Transactional 메서드가 반환된 뒤 true 또는 false

            boolean activeBefore =
                    TransactionSynchronizationManager
                            .isActualTransactionActive();

            TransactionObservation observation = service.observe();

            boolean activeAfter =
                    TransactionSynchronizationManager
                            .isActualTransactionActive();

            assertEquals(expectedActiveBefore, activeBefore);
            assertEquals(
                    expectedActiveInside,
                    observation.activeInside()
            );
            assertEquals(
                    expectedResourceBoundInside,
                    observation.resourceBoundInside()
            );
            assertEquals(
                    expectedSameConnectionInside,
                    observation.sameConnectionInside()
            );
            assertEquals(expectedActiveAfter, activeAfter);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableTransactionManagement(proxyTargetClass = true)
    static class TransactionTestConfiguration {

        @Bean
        EmbeddedDatabase dataSource() {
            return new EmbeddedDatabaseBuilder()
                    .generateUniqueName(true)
                    .setType(EmbeddedDatabaseType.H2)
                    .build();
        }

        @Bean
        PlatformTransactionManager transactionManager(
                DataSource dataSource
        ) {
            return new DataSourceTransactionManager(dataSource);
        }

        @Bean
        ObservationService observationService(DataSource dataSource) {
            return new ObservationService(dataSource);
        }
    }

    static class ObservationService {

        private final DataSource dataSource;

        ObservationService(DataSource dataSource) {
            this.dataSource = dataSource;
        }

        @Transactional
        public TransactionObservation observe() {
            boolean activeInside =
                    TransactionSynchronizationManager
                            .isActualTransactionActive();
            boolean resourceBoundInside =
                    TransactionSynchronizationManager
                            .hasResource(dataSource);

            Connection first =
                    DataSourceUtils.getConnection(dataSource);
            Connection second =
                    DataSourceUtils.getConnection(dataSource);

            new JdbcTemplate(dataSource).queryForObject(
                    "select 1",
                    Integer.class
            );

            return new TransactionObservation(
                    activeInside,
                    resourceBoundInside,
                    first == second
            );
        }
    }

    record TransactionObservation(
            boolean activeInside,
            boolean resourceBoundInside,
            boolean sameConnectionInside
    ) {
    }
}
