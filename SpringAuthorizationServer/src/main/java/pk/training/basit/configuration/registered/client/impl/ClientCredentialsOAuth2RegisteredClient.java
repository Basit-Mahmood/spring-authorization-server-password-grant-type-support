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

		String authorizationCodeClientScope = getClientProperty(CLIENT_CREDENTIALS_CLIENT, SCOPE);
		
		Set<String> scopeSet = Stream.of(authorizationCodeClientScope.split(",", -1)).collect(Collectors.toSet());
		
		RegisteredClient.Builder registeredClientBuilder = getRegisteredClientBuilder();
		RegisteredClient clientCredentialsRegisteredClient = registeredClientBuilder.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
			.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
			.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
			.scopes(scopes -> scopes.addAll(scopeSet))
			.build();
		
		return clientCredentialsRegisteredClient;
		
	}
	
	@Override
	public String getId() {
		return "1";
	}
	
	@Override
	public String getClientId() {
		String clientCredentialsClientId = getClientProperty(CLIENT_CREDENTIALS_CLIENT, ID);
		return clientCredentialsClientId;
	}
	
	@Override
	public String getClientName() {
		String clientCredentialsClientName = getClientProperty(CLIENT_CREDENTIALS_CLIENT, NAME);
		return clientCredentialsClientName;
	}
	
	@Override
	public String getClientSecret() {
		String clientCredentialsClientSecret = getClientProperty(CLIENT_CREDENTIALS_CLIENT, SECRET);
		return clientCredentialsClientSecret;
	}

}
