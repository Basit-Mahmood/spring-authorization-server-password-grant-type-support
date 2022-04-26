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
import org.springframework.security.oauth2.server.authorization.config.ClientSettings;
import org.springframework.stereotype.Component;

import pk.training.basit.configuration.token.OAuth2TokenSettings;

@Component
public class AuthorizationCodeOAuth2RegisteredClient extends AbstractOAuth2RegisteredClient {

	private static final Logger LOGGER = LogManager.getLogger(AuthorizationCodeOAuth2RegisteredClient.class);
	
	private static final String AUTHORIZATION_CODE_CLIENT =  "authorization.code";
	
	public AuthorizationCodeOAuth2RegisteredClient(Environment env, OAuth2TokenSettings oauth2TokenSettings) {
		super(env, oauth2TokenSettings);
		LOGGER.debug("In AuthorizationCodeOAuth2RegisteredClient");
	}
	
	@Override
	public RegisteredClient getRegisteredClient() {
		
		LOGGER.debug("In AuthorizationCodeOAuth2RegisteredClient.getRegisteredClient()");

		String authorizationCodeClientRedirectUri = getClientProperty(AUTHORIZATION_CODE_CLIENT, REDIRECT_URI);
		String authorizationCodeClientScope = getClientProperty(AUTHORIZATION_CODE_CLIENT, SCOPE);
		
		Set<String> scopeSet = Stream.of(authorizationCodeClientScope.split(",", -1)).collect(Collectors.toSet());
		ClientSettings clientSettings = ClientSettings.builder().requireAuthorizationConsent(true).build();
		
		RegisteredClient.Builder registeredClientBuilder = getRegisteredClientBuilder();
		RegisteredClient authorizationCodeRegisteredClient = registeredClientBuilder.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
			.redirectUri(authorizationCodeClientRedirectUri)
			.scopes(scopes -> scopes.addAll(scopeSet))
			.clientSettings(clientSettings)
			.build();
		
		return authorizationCodeRegisteredClient;
		
	}
	
	@Override
	public String getId() {
		return "2";
	}
	
	@Override
	public String getClientId() {
		String authorizationCodeClientId = getClientProperty(AUTHORIZATION_CODE_CLIENT, ID);
		return authorizationCodeClientId;
	}
	
	@Override
	public String getClientName() {
		String authorizationCodeClientName = getClientProperty(AUTHORIZATION_CODE_CLIENT, NAME);
		return authorizationCodeClientName;
	}
	
	@Override
	public String getClientSecret() {
		String authorizationCodeClientSecret = getClientProperty(AUTHORIZATION_CODE_CLIENT, SECRET);
		return authorizationCodeClientSecret;
	}
	
	

}
