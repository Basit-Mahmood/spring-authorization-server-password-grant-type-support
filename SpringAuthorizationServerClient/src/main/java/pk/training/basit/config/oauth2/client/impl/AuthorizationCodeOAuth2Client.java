package pk.training.basit.config.oauth2.client.impl;

import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.stereotype.Component;

@Component
public class AuthorizationCodeOAuth2Client extends AbstractOAuth2Client {

	public AuthorizationCodeOAuth2Client(Environment env) {
		super(env);
	}
	
	@Override
	public ClientRegistration getClientRegistration() {
		return ClientRegistration.withRegistrationId("authorization-code-client-name")
            .clientId("authorization-code-client-id")
            .clientSecret("secret2")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/authorized")
            .scope("message.read", "message.write")
            .authorizationUri(oauth2AuthorizationUri)
            .tokenUri(oauth2TokenUri)
            .build();
	}

}
