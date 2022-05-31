package pk.training.basit.oauth2.customizer.token.claims.impl;

import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsContext;

import pk.training.basit.oauth2.customizer.token.claims.OAuth2TokenClaimsCustomizer;

public class OAuth2TokenClaimsCustomizerImpl implements OAuth2TokenClaimsCustomizer {

	@Override
	public void customizeTokenClaims(OAuth2TokenClaimsContext context) {
		System.out.println();
		
	}
	
}
