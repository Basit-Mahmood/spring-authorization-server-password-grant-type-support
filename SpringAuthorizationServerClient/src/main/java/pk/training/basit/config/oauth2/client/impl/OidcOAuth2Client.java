package pk.training.basit.config.oauth2.client.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.stereotype.Component;

@Component
public class OidcOAuth2Client {

	@Value("${oauth2.token.uri}") 
   	private String oauth2TokenUri;
	
	public ClientRegistration getClientRegistration() {
		return ClientRegistration.withRegistrationId("messaging-client-oidc")
            .clientId("messaging-client")
            .clientSecret("secret")
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
            .scope("openid")
            .authorizationUri("http://127.0.0.1:9000/springauthserver/connect/register")
            .tokenUri(oauth2TokenUri)
            .clientName("messaging-client-oidc")
            .build();
	}
	
}
