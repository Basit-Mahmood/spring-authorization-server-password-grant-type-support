package pk.training.basit.configuration.registered.client.impl;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.config.TokenSettings;
import org.springframework.stereotype.Component;

import pk.training.basit.configuration.token.OAuth2TokenSettings;

@Component
public class ClientCredentialsOAuth2RegisteredClient extends AbstractOAuth2RegisteredClient {

	private static final Logger LOGGER = LogManager.getLogger(ClientCredentialsOAuth2RegisteredClient.class);
	
	private static final String CLIENT_CREDENTIALS_CLIENT = "client.credentials";
	
	public ClientCredentialsOAuth2RegisteredClient(Environment env, OAuth2TokenSettings oauth2TokenSettings) {
		super(env, oauth2TokenSettings);
		LOGGER.debug("In ClientCredentialsOAuth2RegisteredClient");
	}
	
	@Override
	public RegisteredClient getRegisteredClient() {
		
		LOGGER.debug("In ClientCredentialsOAuth2RegisteredClient.getRegisteredClient()");

		String clientCredentialsClientId = getClientProperty(CLIENT_CREDENTIALS_CLIENT, ID);
		String clientCredentialsClientName = getClientProperty(CLIENT_CREDENTIALS_CLIENT, NAME);
		String clientCredentialsClientSecret = getClientProperty(CLIENT_CREDENTIALS_CLIENT, SECRET);
		String authorizationCodeClientScope = getClientProperty(CLIENT_CREDENTIALS_CLIENT, SCOPE);
		
		Set<String> scopeSet = Stream.of(authorizationCodeClientScope.split(",", -1)).collect(Collectors.toSet());
		
		TokenSettings tokenSetting = getTokenSettings();
		
		RegisteredClient clientCredentialsRegisteredClient = RegisteredClient.withId("1")
			.clientId(clientCredentialsClientId)
			.clientName(clientCredentialsClientName)
			.clientSecret(clientCredentialsClientSecret)			
			.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
			.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
			.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
			.scopes(scopes -> scopes.addAll(scopeSet))
			.tokenSettings(tokenSetting)
			.build();
		
		return clientCredentialsRegisteredClient;
		
	}

}
