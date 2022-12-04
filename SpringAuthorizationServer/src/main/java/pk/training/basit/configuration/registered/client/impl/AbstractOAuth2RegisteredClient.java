package pk.training.basit.configuration.registered.client.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import pk.training.basit.configuration.registered.client.OAuth2RegisteredClient;
import pk.training.basit.configuration.token.OAuth2TokenSettings;

public abstract class AbstractOAuth2RegisteredClient implements OAuth2RegisteredClient {

	private static final Logger LOGGER = LogManager.getLogger(AbstractOAuth2RegisteredClient.class);
	
	protected final Environment env;
	protected final OAuth2TokenSettings oauth2TokenSettings;
	
	protected AbstractOAuth2RegisteredClient(Environment env, OAuth2TokenSettings oauth2TokenSettings) {
		this.env = env;
		this.oauth2TokenSettings = oauth2TokenSettings;
	}
	
	protected abstract String getId();
	protected abstract String getClientId();
	protected abstract String getClientName();
	protected abstract String getClientSecret();
	
	protected String getClientProperty(String client, String property) {
		
		LOGGER.debug("in getClientProperty");

		// oauth2.registered.client.authorization.code.id
		String propertyName = String.format("%s.%s.%s", OAUTH2_REGISTERD_CLIENT, client, property);
		String propertyValue = env.getProperty(propertyName);
		return propertyValue;
	}
	
	protected TokenSettings getTokenSettings() {
		return oauth2TokenSettings.getTokenSettings();
	}
	
	protected RegisteredClient.Builder getRegisteredClientBuilder() {
		
		String id = getId();
		String clientId = getClientId();
		String clientName = getClientName();;
		String clientSecret = getClientSecret();
		
		TokenSettings tokenSetting = getTokenSettings();
		
		RegisteredClient.Builder registeredClientBuilder = RegisteredClient.withId(id)
			.clientId(clientId)
			.clientName(clientName)
			.clientSecret(clientSecret)
			.tokenSettings(tokenSetting);
		
		return registeredClientBuilder;
		
		
	}

}
