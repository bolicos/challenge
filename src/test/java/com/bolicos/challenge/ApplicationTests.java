package com.bolicos.challenge;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void shouldLoadFullSpringContextWithInfrastructureBeans() {
        assertThat(applicationContext).isNotNull();
        assertThat(applicationContext.getBean(DataSource.class)).isNotNull();
        assertThat(applicationContext.getBean(Flyway.class)).isNotNull();
        assertThat(applicationContext.getBean(EntityManagerFactory.class)).isNotNull();
        assertThat(applicationContext.getBean(JobLauncher.class)).isNotNull();
        assertThat(applicationContext.getBean(MeterRegistry.class)).isNotNull();
    }

}
