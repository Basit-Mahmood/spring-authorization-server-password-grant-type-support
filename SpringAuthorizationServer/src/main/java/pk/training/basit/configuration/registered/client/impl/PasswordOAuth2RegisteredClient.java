package pk.training.basit.configuration.registered.client.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Component;

import pk.training.basit.configuration.token.OAuth2TokenSettings;

@Component
public class PasswordOAuth2RegisteredClient extends AbstractOAuth2RegisteredClient {

	private static final Logger LOGGER = LogManager.getLogger(PasswordOAuth2RegisteredClient.class);
	
	private static final String PASSWORD_CLIENT =  "password";
	
	public PasswordOAuth2RegisteredClient(Environment env, OAuth2TokenSettings oauth2TokenSettings) {
		super(env, oauth2TokenSettings);
		LOGGER.debug("In PasswordOAuth2RegisteredClient");
	}
	
	@Override
	public RegisteredClient getRegisteredClient() {
		
		LOGGER.debug("In PasswordOAuth2RegisteredClient.getRegisteredClient()");

		RegisteredClient.Builder registeredClientBuilder = getRegisteredClientBuilder();
		RegisteredClient passwordRegisteredClient = registeredClientBuilder.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)	
			.authorizationGrantType(AuthorizationGrantType.PASSWORD)
			.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
			.build();
		
		return passwordRegisteredClient;
		
	}
	
	@Override
	public String getId() {
		return "3";
	}
	
	@Override
	public String getClientId() {
		String passwordClientId = getClientProperty(PASSWORD_CLIENT, ID);
		return passwordClientId;
	}
	
	@Override
	public String getClientName() {
		String passwordClientName = getClientProperty(PASSWORD_CLIENT, NAME);
		return passwordClientName;
	}
	
	@Override
	public String getClientSecret() {
		String passwordClientSecret = getClientProperty(PASSWORD_CLIENT, SECRET);
		return passwordClientSecret;
	}

}
