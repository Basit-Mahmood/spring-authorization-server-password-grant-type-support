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
import org.springframework.security.oauth2.server.authorization.config.TokenSettings;
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

		String authorizationCodeClientId = getClientProperty(AUTHORIZATION_CODE_CLIENT, ID);
		String authorizationCodeClientName = getClientProperty(AUTHORIZATION_CODE_CLIENT, NAME);
		String authorizationCodeClientSecret = getClientProperty(AUTHORIZATION_CODE_CLIENT, SECRET);
		String authorizationCodeClientRedirectUri = getClientProperty(AUTHORIZATION_CODE_CLIENT, REDIRECT_URI);
		String authorizationCodeClientScope = getClientProperty(AUTHORIZATION_CODE_CLIENT, SCOPE);
		
		Set<String> scopeSet = Stream.of(authorizationCodeClientScope.split(",", -1)).collect(Collectors.toSet());
		
		TokenSettings tokenSetting = getTokenSettings();
		ClientSettings clientSettings = ClientSettings.builder().requireAuthorizationConsent(true).build();
		
		RegisteredClient authorizationCodeRegisteredClient = RegisteredClient.withId("2")
			.clientId(authorizationCodeClientId)
			.clientName(authorizationCodeClientName)
			.clientSecret(authorizationCodeClientSecret)
			.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
			.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
			.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
			.redirectUri(authorizationCodeClientRedirectUri)
			.scopes(scopes -> scopes.addAll(scopeSet))
			.tokenSettings(tokenSetting)
			.clientSettings(clientSettings)
			.build();
		
		return authorizationCodeRegisteredClient;
		
	}

}
