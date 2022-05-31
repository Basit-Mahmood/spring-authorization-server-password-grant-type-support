package pk.training.basit.oauth2.customizer.jwt.impl;

import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

import pk.training.basit.oauth2.customizer.jwt.JwtCustomizer;
import pk.training.basit.oauth2.customizer.jwt.JwtCustomizerHandler;

public class JwtCustomizerImpl implements JwtCustomizer {

	private final JwtCustomizerHandler jwtCustomizerHandler;
	
	public JwtCustomizerImpl(JwtCustomizerHandler jwtCustomizerHandler) {
		this.jwtCustomizerHandler = jwtCustomizerHandler;
	}

	@Override
	public void customizeToken(JwtEncodingContext jwtEncodingContext) {
		jwtCustomizerHandler.customize(jwtEncodingContext);
	}
	
}
