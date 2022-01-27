package pk.training.basit.config.oauth2.client;

import org.springframework.security.oauth2.client.registration.ClientRegistration;

public interface OAuth2Client {

	ClientRegistration getClientRegistration();
	
}
