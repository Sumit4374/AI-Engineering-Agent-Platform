package sumit.ai.ai_engineering.infrastructure.configuration;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayConfiguration {

    @Bean
    public Flyway flyway(DataSource dataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .load();
        flyway.repair();
        flyway.migrate();
        return flyway;
    }

    @Bean
    public static BeanFactoryPostProcessor dependsOnFlywayPostProcessor() {
        return beanFactory -> {
            String[] entityManagerFactoryNames = beanFactory.getBeanNamesForType(jakarta.persistence.EntityManagerFactory.class, true, false);
            for (String name : entityManagerFactoryNames) {
                beanFactory.getBeanDefinition(name).setDependsOn("flyway");
            }
        };
    }
}
