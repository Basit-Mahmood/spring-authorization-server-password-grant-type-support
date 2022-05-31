package pk.training.basit.configuration;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.ProviderSettings;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.security.oauth2.server.authorization.web.authentication.DelegatingAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2AuthorizationCodeAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2ClientCredentialsAuthenticationConverter;
import org.springframework.security.oauth2.server.authorization.web.authentication.OAuth2RefreshTokenAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import pk.training.basit.configuration.federated.identity.FederatedIdentityConfigurer;
import pk.training.basit.configuration.jose.Jwks;
import pk.training.basit.jackson2.mixin.AuditDeletedDateMixin;
import pk.training.basit.jackson2.mixin.LongMixin;
import pk.training.basit.jackson2.mixin.UserAuthorityMixin;
import pk.training.basit.jackson2.mixin.UserPrincipalMixin;
import pk.training.basit.jpa.audit.AuditDeletedDate;
import pk.training.basit.jpa.entity.UserAuthority;
import pk.training.basit.jpa.entity.UserPrincipal;
import pk.training.basit.oauth2.authentication.OAuth2ResourceOwnerPasswordAuthenticationConverter;
import pk.training.basit.oauth2.authentication.OAuth2ResourceOwnerPasswordAuthenticationProvider;
import pk.training.basit.oauth2.customizer.jwt.JwtCustomizer;
import pk.training.basit.oauth2.customizer.jwt.JwtCustomizerHandler;
import pk.training.basit.oauth2.customizer.jwt.impl.JwtCustomizerImpl;
import pk.training.basit.oauth2.customizer.token.claims.OAuth2TokenClaimsCustomizer;
import pk.training.basit.oauth2.customizer.token.claims.impl.OAuth2TokenClaimsCustomizerImpl;

@Configuration(proxyBeanMethods = false)
public class AuthorizationServerConfiguration {

	private static final String CUSTOM_CONSENT_PAGE_URI = "/oauth2/consent";
	
	@Value("${oauth2.token.issuer}") 
	private String tokenIssuer;
	
	@Bean
	@Order(Ordered.HIGHEST_PRECEDENCE)
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
		
		OAuth2AuthorizationServerConfigurer<HttpSecurity> authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer<>();
		
		/**
		http.apply(authorizationServerConfigurer.withObjectPostProcessor(new ObjectPostProcessor<OAuth2TokenEndpointFilter>() {
					@Override
					public <O extends OAuth2TokenEndpointFilter> O postProcess(O oauth2TokenEndpointFilter) {
						oauth2TokenEndpointFilter.setAuthenticationConverter(new DelegatingAuthenticationConverter(
								Arrays.asList(
										new OAuth2AuthorizationCodeAuthenticationConverter(),
										new OAuth2RefreshTokenAuthenticationConverter(),
										new OAuth2ClientCredentialsAuthenticationConverter(),
										new OAuth2ResourceOwnerPasswordAuthenticationConverter())));
						return oauth2TokenEndpointFilter;
					}
				})
		);
		*/
		http.apply(authorizationServerConfigurer.tokenEndpoint((tokenEndpoint) -> tokenEndpoint.accessTokenRequestConverter(
			new DelegatingAuthenticationConverter(Arrays.asList(
				new OAuth2AuthorizationCodeAuthenticationConverter(),
				new OAuth2RefreshTokenAuthenticationConverter(),
				new OAuth2ClientCredentialsAuthenticationConverter(),
				new OAuth2ResourceOwnerPasswordAuthenticationConverter()))
		)));
		
		authorizationServerConfigurer.authorizationEndpoint(authorizationEndpoint -> authorizationEndpoint.consentPage(CUSTOM_CONSENT_PAGE_URI));
		
		RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();
		
		http
			.requestMatcher(endpointsMatcher)
			.authorizeRequests(authorizeRequests -> authorizeRequests.anyRequest().authenticated())
			.csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher))
			.apply(authorizationServerConfigurer)
			.and()
			.apply(new FederatedIdentityConfigurer());
		
		SecurityFilterChain securityFilterChain = http.formLogin(Customizer.withDefaults()).build();
		
		/**
		 * Custom configuration for Resource Owner Password grant type. Current implementation has no support for Resource Owner 
		 * Password grant type
		 */
		addCustomOAuth2ResourceOwnerPasswordAuthenticationProvider(http);
		
		return securityFilterChain;
	}
	
	@Bean
	public OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository) {
		JdbcOAuth2AuthorizationService service = new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
		JdbcOAuth2AuthorizationService.OAuth2AuthorizationRowMapper rowMapper = new JdbcOAuth2AuthorizationService.OAuth2AuthorizationRowMapper(registeredClientRepository);
		
		ObjectMapper objectMapper = new ObjectMapper();
		ClassLoader classLoader = JdbcOAuth2AuthorizationService.class.getClassLoader();
		List<Module> securityModules = SecurityJackson2Modules.getModules(classLoader);
		objectMapper.registerModules(securityModules);
		objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());
		
        // You will need to write the Mixin for your class so Jackson can marshall it.
		objectMapper.addMixIn(UserAuthority.class, UserAuthorityMixin.class);
		objectMapper.addMixIn(UserPrincipal.class, UserPrincipalMixin.class);
		objectMapper.addMixIn(AuditDeletedDate.class, AuditDeletedDateMixin.class);
		objectMapper.addMixIn(Long.class, LongMixin.class);
		
		rowMapper.setObjectMapper(objectMapper);
		service.setAuthorizationRowMapper(rowMapper);
		return service;
	}
	
	@Bean
	public OAuth2AuthorizationConsentService authorizationConsentService(JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository) {
		return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
	}
	
	@Bean
	public JWKSource<SecurityContext> jwkSource() {
		RSAKey rsaKey = Jwks.generateRsa();
		JWKSet jwkSet = new JWKSet(rsaKey);
		return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
	}

	@Bean
	public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
		return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
	}

	@Bean
	public ProviderSettings providerSettings() {
		return ProviderSettings.builder().issuer(tokenIssuer).build();
	}
	
	@Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> buildJwtCustomizer() {
		
		JwtCustomizerHandler jwtCustomizerHandler = JwtCustomizerHandler.getJwtCustomizerHandler();
		JwtCustomizer jwtCustomizer = new JwtCustomizerImpl(jwtCustomizerHandler);
        OAuth2TokenCustomizer<JwtEncodingContext> customizer = (context) -> {
        	jwtCustomizer.customizeToken(context);
        };
        
        return customizer;
    }
	
	@Bean
    public OAuth2TokenCustomizer<OAuth2TokenClaimsContext> buildOAuth2TokenClaimsCustomizer() {
		
		OAuth2TokenClaimsCustomizer oauth2TokenClaimsCustomizer = new OAuth2TokenClaimsCustomizerImpl();
        OAuth2TokenCustomizer<OAuth2TokenClaimsContext> customizer = (context) -> {
        	oauth2TokenClaimsCustomizer.customizeTokenClaims(context);
        };
        
        return customizer;
    }
	
	@SuppressWarnings("unchecked")
	private void addCustomOAuth2ResourceOwnerPasswordAuthenticationProvider(HttpSecurity http) {
		
		AuthenticationManager authenticationManager = http.getSharedObject(AuthenticationManager.class);
		OAuth2AuthorizationService authorizationService = http.getSharedObject(OAuth2AuthorizationService.class);
		OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator = http.getSharedObject(OAuth2TokenGenerator.class);
		
		OAuth2ResourceOwnerPasswordAuthenticationProvider resourceOwnerPasswordAuthenticationProvider =
				new OAuth2ResourceOwnerPasswordAuthenticationProvider(authenticationManager, authorizationService, tokenGenerator);
		
		// This will add new authentication provider in the list of existing authentication providers.
		http.authenticationProvider(resourceOwnerPasswordAuthenticationProvider);
		
	}
	
}
