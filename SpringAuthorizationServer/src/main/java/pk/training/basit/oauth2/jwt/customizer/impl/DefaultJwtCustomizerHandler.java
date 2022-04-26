package pk.training.basit.oauth2.jwt.customizer.impl;

import org.springframework.security.oauth2.server.authorization.JwtEncodingContext;

import pk.training.basit.oauth2.jwt.customizer.JwtCustomizerHandler;

public class DefaultJwtCustomizerHandler implements JwtCustomizerHandler {

	@Override
	public void customize(JwtEncodingContext jwtEncodingContext) {
		// does not modify any thing in context

	}

}
