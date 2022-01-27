package pk.training.basit.configuration;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import pk.training.basit.service.OAuth2RegisteredClientService;

@Configuration
@PropertySource("classpath:oauth2-registered-client.properties")
public class OAuth2RegisteredClientConfiguration {

	private static final Logger LOGGER = LogManager.getLogger(OAuth2RegisteredClientConfiguration.class);

	@Autowired
	private OAuth2RegisteredClientService oauth2RegisteredClientService;
	
	@Bean
	public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
		
		LOGGER.debug("in registeredClientRepository");

		List<RegisteredClient> registeredClients = oauth2RegisteredClientService.getOAuth2RegisteredClient();
	
		JdbcRegisteredClientRepository registeredClientRepository = new JdbcRegisteredClientRepository(jdbcTemplate);
		registeredClients.forEach(registeredClient -> {
			registeredClientRepository.save(registeredClient);
		});
		
		return registeredClientRepository;
	}
	
}
