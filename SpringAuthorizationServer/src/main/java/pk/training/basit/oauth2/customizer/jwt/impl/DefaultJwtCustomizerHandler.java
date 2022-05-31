package pk.training.basit.oauth2.customizer.jwt.impl;

import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

import pk.training.basit.oauth2.customizer.jwt.JwtCustomizerHandler;

public class DefaultJwtCustomizerHandler implements JwtCustomizerHandler {

	@Override
	public void customize(JwtEncodingContext jwtEncodingContext) {
		// does not modify any thing in context

	}

}
