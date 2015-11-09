package de.tot.microservices.accounts;

import de.tot.microservices.services.accounts.AccountsServer;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.init.DatabasePopulatorUtils;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

/**
 * The accounts web-application. This class has two uses:
 * <ol>
 * <li>Provide configuration and setup for {@link AccountsServer} ... or</li>
 * <li>Run as a stand-alone Spring Boot web-application for testing (in which
 * case there is <i>no</i> microservice registration</li>
 * </ol>
 * <p>
 * To execute as a microservice, run {@link AccountsServer} instead.
 * 
 * @author Paul Chapman
 */
@PropertySource("classpath:db-config.properties")
@SpringBootApplication
@EntityScan("de.tot.microservices.accounts")
@EnableJpaRepositories("de.tot.microservices.accounts")
public class AccountsWebApplication {

    @Autowired
    private Environment env;

	protected Logger logger = Logger.getLogger(AccountsWebApplication.class
			.getName());

	/**
	 * Run the application using Spring Boot and an embedded servlet engine.
	 * 
	 * @param args
	 *            Program arguments - ignored.
	 */
	public static void main(String[] args) {
		SpringApplication.run(AccountsWebApplication.class, args);
	}

	/**
	 * Creates an in-memory "rewards" database populated with test data for fast
	 * testing
	 */
	@Bean
	public DataSource dataSource() {
		logger.info("dataSource() invoked");

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getRequiredProperty("spring.datasource.driverClassName"));
        dataSource.setUrl(env.getRequiredProperty("spring.datasource.url"));
        dataSource.setUsername(env.getRequiredProperty("spring.datasource.username"));
        dataSource.setPassword(env.getRequiredProperty("spring.datasource.password"));

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("testdb/schema.sql"));
        populator.addScript(new ClassPathResource("testdb/data.sql"));
        DatabasePopulatorUtils.execute(populator, dataSource);

/*
        DataSource dataSource = (new EmbeddedDatabaseBuilder())
				.addScript("classpath:testdb/schema.sql")
				.addScript("classpath:testdb/data.sql").build();
*/
		logger.info("dataSource = " + dataSource);

		// Sanity check
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);


		List<Map<String, Object>> accounts = jdbcTemplate
				.queryForList("SELECT number FROM T_ACCOUNT");
		logger.info("System has " + accounts.size() + " accounts");

		// Populate with random balances
		Random rand = new Random();

		for (Map<String, Object> item : accounts) {
			String number = (String) item.get("number");
			BigDecimal balance = new BigDecimal(rand.nextInt(10000000) / 100.0)
					.setScale(2, BigDecimal.ROUND_HALF_UP);
			jdbcTemplate.update(
					"UPDATE T_ACCOUNT SET balance = ? WHERE number = ?",
					balance, number);
		}

		return dataSource;
	}
}
