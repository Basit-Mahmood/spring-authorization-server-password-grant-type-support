package pk.training.basit.config.oauth2.client.impl;

import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.stereotype.Component;

@Component
public class PasswordOAuth2Client extends AbstractOAuth2Client {

	public PasswordOAuth2Client(Environment env) {
		super(env);
	}
	
	@Override
	public ClientRegistration getClientRegistration() {
		return ClientRegistration.withRegistrationId("password-client-name")
           .clientId("password-client-id")
           .clientSecret("secret3")
           .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
           .authorizationGrantType(AuthorizationGrantType.PASSWORD)
           //.scope("message.read", "message.write")
           .authorizationUri(oauth2AuthorizationUri)
           .tokenUri(oauth2TokenUri)			
           .build();
	}

}
