package pk.training.basit.oauth2.authentication;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClaimAccessor;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

public class OAuth2ResourceOwnerPasswordAuthenticationProvider implements AuthenticationProvider {

	private static final Logger LOGGER = LogManager.getLogger();
	
	private static final String ERROR_URI = "https://datatracker.ietf.org/doc/html/rfc6749#section-5.2";
	private static final OAuth2TokenType ID_TOKEN_TOKEN_TYPE = new OAuth2TokenType(OidcParameterNames.ID_TOKEN);
	private final AuthenticationManager authenticationManager;
	private final OAuth2AuthorizationService authorizationService;
	private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;
	
	/**
	 * Constructs an {@code OAuth2ResourceOwnerPasswordAuthenticationProviderNew} using the provided parameters.
	 *
	 * @param authenticationManager the authentication manager
	 * @param authorizationService the authorization service
	 * @param tokenGenerator the token generator
	 * @since 0.2.3
	 */
	public OAuth2ResourceOwnerPasswordAuthenticationProvider(AuthenticationManager authenticationManager, 
			OAuth2AuthorizationService authorizationService, OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator) {
		Assert.notNull(authorizationService, "authorizationService cannot be null");
		Assert.notNull(tokenGenerator, "tokenGenerator cannot be null");
		this.authenticationManager = authenticationManager;
		this.authorizationService = authorizationService;
		this.tokenGenerator = tokenGenerator;
	}
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		
		OAuth2ResourceOwnerPasswordAuthenticationToken resouceOwnerPasswordAuthentication = (OAuth2ResourceOwnerPasswordAuthenticationToken) authentication;

		OAuth2ClientAuthenticationToken clientPrincipal = getAuthenticatedClientElseThrowInvalidClient(resouceOwnerPasswordAuthentication);
		
		RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();
		
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Retrieved registered client");
		}

		if (!registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.PASSWORD)) {
			throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);
		}

		Authentication usernamePasswordAuthentication = getUsernamePasswordAuthentication(resouceOwnerPasswordAuthentication);
		
		Set<String> authorizedScopes = registeredClient.getScopes();		// Default to configured scopes
		Set<String> requestedScopes = resouceOwnerPasswordAuthentication.getScopes();
		if (!CollectionUtils.isEmpty(requestedScopes)) {
			Set<String> unauthorizedScopes = requestedScopes.stream()
					.filter(requestedScope -> !registeredClient.getScopes().contains(requestedScope))
					.collect(Collectors.toSet());
			if (!CollectionUtils.isEmpty(unauthorizedScopes)) {
				throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_SCOPE);
			}
			
			authorizedScopes = new LinkedHashSet<>(requestedScopes);
		}
		
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Validated token request parameters");
		}

		// @formatter:off
		DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
				.registeredClient(registeredClient)
				.principal(usernamePasswordAuthentication)
				.authorizationServerContext(AuthorizationServerContextHolder.getContext())
				.authorizedScopes(authorizedScopes)
				.authorizationGrantType(AuthorizationGrantType.PASSWORD)
				.authorizationGrant(resouceOwnerPasswordAuthentication);
		// @formatter:on

		// ----- Access token -----
		OAuth2TokenContext tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN).build();
		OAuth2Token generatedAccessToken = this.tokenGenerator.generate(tokenContext);
		if (generatedAccessToken == null) {
			OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
					"The token generator failed to generate the access token.", ERROR_URI);
			throw new OAuth2AuthenticationException(error);
		}
		
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Generated access token");
		}
		
		OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
				generatedAccessToken.getTokenValue(), generatedAccessToken.getIssuedAt(),
				generatedAccessToken.getExpiresAt(), tokenContext.getAuthorizedScopes());
		
		// @formatter:off
		OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
				.principalName(usernamePasswordAuthentication.getName())
				.authorizationGrantType(AuthorizationGrantType.PASSWORD)
				.authorizedScopes(authorizedScopes)
				.attribute(Principal.class.getName(), usernamePasswordAuthentication);
		// @formatter:on
		if (generatedAccessToken instanceof ClaimAccessor) {
			authorizationBuilder.token(accessToken, (metadata) ->
					metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, ((ClaimAccessor) generatedAccessToken).getClaims()));
		} else {
			authorizationBuilder.accessToken(accessToken);
		}
		
		// ----- Refresh token -----
		OAuth2RefreshToken refreshToken = null;
		if (registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN) &&
				// Do not issue refresh token to public client
				!clientPrincipal.getClientAuthenticationMethod().equals(ClientAuthenticationMethod.NONE)) {
			
			tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.REFRESH_TOKEN).build();
			OAuth2Token generatedRefreshToken = this.tokenGenerator.generate(tokenContext);
			if (!(generatedRefreshToken instanceof OAuth2RefreshToken)) {
				OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
						"The token generator failed to generate the refresh token.", ERROR_URI);
				throw new OAuth2AuthenticationException(error);
			}
			
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Generated refresh token");
			}
			
			refreshToken = (OAuth2RefreshToken) generatedRefreshToken;
			authorizationBuilder.refreshToken(refreshToken);
		}
		
		// ----- ID token -----
		OidcIdToken idToken;
		if (requestedScopes.contains(OidcScopes.OPENID)) {
			// @formatter:off
			tokenContext = tokenContextBuilder
					.tokenType(ID_TOKEN_TOKEN_TYPE)
					.authorization(authorizationBuilder.build())	// ID token customizer may need access to the access token and/or refresh token
					.build();
			// @formatter:on
			OAuth2Token generatedIdToken = this.tokenGenerator.generate(tokenContext);
			if (!(generatedIdToken instanceof Jwt)) {
				OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR,
						"The token generator failed to generate the ID token.", ERROR_URI);
				throw new OAuth2AuthenticationException(error);
			}

			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Generated id token");
			}

			idToken = new OidcIdToken(generatedIdToken.getTokenValue(), generatedIdToken.getIssuedAt(),
					generatedIdToken.getExpiresAt(), ((Jwt) generatedIdToken).getClaims());
			authorizationBuilder.token(idToken, (metadata) ->
					metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, idToken.getClaims()));
		} else {
			idToken = null;
		}

		OAuth2Authorization authorization = authorizationBuilder.build();

		this.authorizationService.save(authorization);
		
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Saved authorization");
		}
		
		Map<String, Object> additionalParameters = Collections.emptyMap();
		if (idToken != null) {
			additionalParameters = new HashMap<>();
			additionalParameters.put(OidcParameterNames.ID_TOKEN, idToken.getTokenValue());
		}

		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Authenticated token request");
		}
		
		return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientPrincipal, accessToken, refreshToken, additionalParameters);
	}
	
	@Override
	public boolean supports(Class<?> authentication) {
		boolean supports = OAuth2ResourceOwnerPasswordAuthenticationToken.class.isAssignableFrom(authentication);
		LOGGER.debug("supports authentication=" + authentication + " returning " + supports);
		return supports;
	}
	
	private Authentication getUsernamePasswordAuthentication(OAuth2ResourceOwnerPasswordAuthenticationToken resouceOwnerPasswordAuthentication) {
		
		Map<String, Object> additionalParameters = resouceOwnerPasswordAuthentication.getAdditionalParameters();
		
		String username = (String) additionalParameters.get(OAuth2ParameterNames.USERNAME);
		String password = (String) additionalParameters.get(OAuth2ParameterNames.PASSWORD);
		
		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username, password);
		LOGGER.debug("got usernamePasswordAuthenticationToken=" + usernamePasswordAuthenticationToken);
		
		Authentication usernamePasswordAuthentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
		return usernamePasswordAuthentication;
	}
	
	private OAuth2ClientAuthenticationToken getAuthenticatedClientElseThrowInvalidClient(Authentication authentication) {
		
		OAuth2ClientAuthenticationToken clientPrincipal = null;
		
		if (OAuth2ClientAuthenticationToken.class.isAssignableFrom(authentication.getPrincipal().getClass())) {
			clientPrincipal = (OAuth2ClientAuthenticationToken) authentication.getPrincipal();
		}
		
		if (clientPrincipal != null && clientPrincipal.isAuthenticated()) {
			return clientPrincipal;
		}
		
		throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
	}
	
}
