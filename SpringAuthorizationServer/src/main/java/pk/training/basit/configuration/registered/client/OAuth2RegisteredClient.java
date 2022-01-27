package pk.training.basit.configuration.registered.client;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

public interface OAuth2RegisteredClient {

	String OAUTH2_REGISTERD_CLIENT = "oauth2.registered.client";
	String ID = "id";
	String NAME = "name";
	String SECRET = "secret";
	String REDIRECT_URI = "redirect.uri";
	String SCOPE = "scope";
	
	RegisteredClient getRegisteredClient();
	
}
