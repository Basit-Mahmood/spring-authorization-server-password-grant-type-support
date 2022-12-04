package pk.training.basit.configuration.token;

import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

public interface OAuth2TokenSettings {

	TokenSettings getTokenSettings();
	
}
