package pk.training.basit.oauth2.jwt.customizer;

import org.springframework.security.oauth2.server.authorization.JwtEncodingContext;

public interface JwtCustomizer {

	void customizeToken(JwtEncodingContext context);
	
}
