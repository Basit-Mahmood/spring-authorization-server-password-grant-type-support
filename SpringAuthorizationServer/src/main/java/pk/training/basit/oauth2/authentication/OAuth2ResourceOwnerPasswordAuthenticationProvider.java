package pk.training.basit.oauth2.authentication;

import java.security.Principal;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.OAuth2TokenType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.jwt.JoseHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.server.authorization.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

public class OAuth2ResourceOwnerPasswordAuthenticationProvider implements AuthenticationProvider {

	private static final Logger LOGGER = LogManager.getLogger(OAuth2ResourceOwnerPasswordAuthenticationProvider.class);
	
	private static final StringKeyGenerator DEFAULT_REFRESH_TOKEN_GENERATOR = new Base64StringKeyGenerator(Base64.getUrlEncoder().withoutPadding(), 96);
	
	private final AuthenticationManager authenticationManager;
	private final OAuth2AuthorizationService authorizationService;
	private final JwtEncoder jwtEncoder;
	private OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer = (context) -> {};
	private Supplier<String> refreshTokenGenerator = DEFAULT_REFRESH_TOKEN_GENERATOR::generateKey;
	private ProviderSettings providerSettings;
	
	/**
	 * Constructs an {@code OAuth2ClientCredentialsAuthenticationProvider} using the provided parameters.
	 *
	 * @param authorizationService the authorization service
	 * @param jwtEncoder the jwt encoder
	 */
	public OAuth2ResourceOwnerPasswordAuthenticationProvider(AuthenticationManager authenticationManager, OAuth2AuthorizationService authorizationService,
			JwtEncoder jwtEncoder) {
		Assert.notNull(authorizationService, "authorizationService cannot be null");
		Assert.notNull(jwtEncoder, "jwtEncoder cannot be null");
		this.authenticationManager = authenticationManager;
		this.authorizationService = authorizationService;
		this.jwtEncoder = jwtEncoder;
	}

	public final void setJwtCustomizer(OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer) {
		Assert.notNull(jwtCustomizer, "jwtCustomizer cannot be null");
		this.jwtCustomizer = jwtCustomizer;
	}

	@Autowired(required = false)
	public void setProviderSettings(ProviderSettings providerSettings) {
		this.providerSettings = providerSettings;
	}
	
	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		
		OAuth2ResourceOwnerPasswordAuthenticationToken resouceOwnerPasswordAuthentication = (OAuth2ResourceOwnerPasswordAuthenticationToken) authentication;

		OAuth2ClientAuthenticationToken clientPrincipal = getAuthenticatedClientElseThrowInvalidClient(resouceOwnerPasswordAuthentication);
		
		RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();

		if (!registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.PASSWORD)) {
			throw new OAuth2AuthenticationException(OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);
		}

		Map<String, Object> additionalParameters = resouceOwnerPasswordAuthentication.getAdditionalParameters();
		String username = (String) additionalParameters.get(OAuth2ParameterNames.USERNAME);
		String password = (String) additionalParameters.get(OAuth2ParameterNames.PASSWORD);
		
		try {
			UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username, password);
			LOGGER.debug("got usernamePasswordAuthenticationToken=" + usernamePasswordAuthenticationToken);
			
			Authentication usernamePasswordAuthentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
			Set<String> authorizedScopes = registeredClient.getScopes();		// Default to configured scopes
			
			if (!CollectionUtils.isEmpty(resouceOwnerPasswordAuthentication.getScopes())) {
				Set<String> unauthorizedScopes = resouceOwnerPasswordAuthentication.getScopes().stream()
						.filter(requestedScope -> !registeredClient.getScopes().contains(requestedScope))
						.collect(Collectors.toSet());
				if (!CollectionUtils.isEmpty(unauthorizedScopes)) {
					throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_SCOPE);
				}
				
				authorizedScopes = new LinkedHashSet<>(resouceOwnerPasswordAuthentication.getScopes());
			}
			
			String issuer = this.providerSettings != null ? this.providerSettings.getIssuer() : null;

			JoseHeader.Builder headersBuilder = JwtUtils.headers();
			JwtClaimsSet.Builder claimsBuilder = JwtUtils.accessTokenClaims(
					registeredClient, issuer, usernamePasswordAuthentication.getName(), authorizedScopes);

			JwtEncodingContext context = JwtEncodingContext.with(headersBuilder, claimsBuilder)
					.registeredClient(registeredClient)
					.principal(usernamePasswordAuthentication)
					.authorizedScopes(authorizedScopes)
					.tokenType(OAuth2TokenType.ACCESS_TOKEN)
					.authorizationGrantType(AuthorizationGrantType.PASSWORD)
					.authorizationGrant(resouceOwnerPasswordAuthentication)
					.build();

			this.jwtCustomizer.customize(context);

			JoseHeader headers = context.getHeaders().build();
			JwtClaimsSet claims = context.getClaims().build();
			Jwt jwtAccessToken = this.jwtEncoder.encode(headers, claims);

			// Use the scopes after customizing the token
			authorizedScopes = claims.getClaim(OAuth2ParameterNames.SCOPE);
			
			OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
					jwtAccessToken.getTokenValue(), jwtAccessToken.getIssuedAt(),
					jwtAccessToken.getExpiresAt(), authorizedScopes);

			OAuth2RefreshToken refreshToken = null;
			if (registeredClient.getAuthorizationGrantTypes().contains(AuthorizationGrantType.REFRESH_TOKEN)) {
				refreshToken = generateRefreshToken(registeredClient.getTokenSettings().getRefreshTokenTimeToLive());
			}
			
			OAuth2Authorization.Builder authorizationBuilder = OAuth2Authorization.withRegisteredClient(registeredClient)
					.principalName(usernamePasswordAuthentication.getName())
					.authorizationGrantType(AuthorizationGrantType.PASSWORD)
					.token(accessToken,
							(metadata) ->
									metadata.put(OAuth2Authorization.Token.CLAIMS_METADATA_NAME, jwtAccessToken.getClaims()))
					.attribute(OAuth2Authorization.AUTHORIZED_SCOPE_ATTRIBUTE_NAME, authorizedScopes)
					.attribute(Principal.class.getName(), usernamePasswordAuthentication);

			if (refreshToken != null) {
				authorizationBuilder.refreshToken(refreshToken);
			}
			
			OAuth2Authorization authorization = authorizationBuilder.build();
			
			this.authorizationService.save(authorization);
			
			LOGGER.debug("OAuth2Authorization saved successfully");
			
			Map<String, Object> tokenAdditionalParameters = new HashMap<>();
			claims.getClaims().forEach((key, value) -> {
				if (!key.equals(OAuth2ParameterNames.SCOPE) && 
						!key.equals(JwtClaimNames.IAT) && 
						!key.equals(JwtClaimNames.EXP) &&
						!key.equals(JwtClaimNames.NBF)) {
					tokenAdditionalParameters.put(key, value);
				}
			});
			
			LOGGER.debug("returning OAuth2AccessTokenAuthenticationToken");

			return new OAuth2AccessTokenAuthenticationToken(registeredClient, clientPrincipal, accessToken, refreshToken, tokenAdditionalParameters);
			
		} catch (Exception ex) {
			LOGGER.error("problem in authenticate", ex);
			throw new OAuth2AuthenticationException(new OAuth2Error(OAuth2ErrorCodes.SERVER_ERROR), ex);
		}
		
	}
	
	@Override
	public boolean supports(Class<?> authentication) {
		boolean supports = OAuth2ResourceOwnerPasswordAuthenticationToken.class.isAssignableFrom(authentication);
		LOGGER.debug("supports authentication=" + authentication + " returning " + supports);
		return supports;
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
	
	private OAuth2RefreshToken generateRefreshToken(Duration tokenTimeToLive) {
		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus(tokenTimeToLive);
		return new OAuth2RefreshToken(this.refreshTokenGenerator.get(), issuedAt, expiresAt);
	}
	
}
