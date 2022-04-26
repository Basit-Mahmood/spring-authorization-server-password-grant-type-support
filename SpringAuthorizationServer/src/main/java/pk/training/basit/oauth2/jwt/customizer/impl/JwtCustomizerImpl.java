package pk.training.basit.oauth2.jwt.customizer.impl;

import org.springframework.security.oauth2.server.authorization.JwtEncodingContext;

import pk.training.basit.oauth2.jwt.customizer.JwtCustomizer;
import pk.training.basit.oauth2.jwt.customizer.JwtCustomizerHandler;

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
