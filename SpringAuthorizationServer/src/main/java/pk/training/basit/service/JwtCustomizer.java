package pk.training.basit.service;

import org.springframework.security.oauth2.server.authorization.JwtEncodingContext;

public interface JwtCustomizer {

	void customizeToken(JwtEncodingContext context);
	
}
