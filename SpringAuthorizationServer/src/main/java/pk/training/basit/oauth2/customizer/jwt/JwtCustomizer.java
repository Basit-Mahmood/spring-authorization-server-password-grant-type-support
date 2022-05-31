package pk.training.basit.oauth2.customizer.jwt;

import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

public interface JwtCustomizer {

	void customizeToken(JwtEncodingContext context);
	
}
