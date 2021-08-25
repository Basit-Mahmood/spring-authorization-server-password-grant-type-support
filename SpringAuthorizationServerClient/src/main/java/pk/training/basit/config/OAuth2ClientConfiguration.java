package pk.training.basit.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@Configuration
public class OAuth2ClientConfiguration {

    private static List<String> clients = Arrays.asList("client_credentials", "authorization_code", "password");
		
    @Value("${oauth2.authorization.uri}") 
	private String oauth2AuthorizationUri;
    
    @Value("${oauth2.token.uri}") 
   	private String oauth2TokenUri;
    
	@Bean
	public ClientRegistrationRepository clientRegistrationRepository() {
	        
		List<ClientRegistration> registrations = clients.stream()
	            .map(c -> getRegistration(c))
	            .filter(registration -> registration != null)
	            .collect(Collectors.toList());
		
		return new InMemoryClientRegistrationRepository(registrations);
    }
	    
    private ClientRegistration getRegistration(String client) {
        
    	if (client.equals("client_credentials")) {
    		ClientRegistration clientCredentialsClientRegistration = clientCredentialsClientRegistration();
         	return clientCredentialsClientRegistration;
        }
    	
    	if (client.equals("authorization_code")) {
    		ClientRegistration authorizationCodeClientRegistration = authorizationCodeClientRegistration();
         	return authorizationCodeClientRegistration;
        }
    	
    	if (client.equals("password")) {
    		ClientRegistration passwordClientRegistration = passwordClientRegistration();
         	return passwordClientRegistration;
       }
    	
        return null;
    }
	    
    private ClientRegistration oidcClientRegistration() {
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
    
    private ClientRegistration clientCredentialsClientRegistration() {
    	 return ClientRegistration.withRegistrationId("messaging-client-client-credentials")
            .clientId("client-credentials-messaging-client")
            .clientSecret("secret1")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
            .scope("message.read", "message.write")
            .authorizationUri(oauth2AuthorizationUri)
            .tokenUri(oauth2TokenUri)
            .build();
    }
    
    private ClientRegistration authorizationCodeClientRegistration() {
        return ClientRegistration.withRegistrationId("messaging-client-authorization-code")
            .clientId("authorization-code-messaging-client")
            .clientSecret("secret2")
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .redirectUri("{baseUrl}/authorized")
            //.scope("message.read", "message.write")
            .authorizationUri(oauth2AuthorizationUri)
            .tokenUri(oauth2TokenUri)
            .build();
    }
    
    private ClientRegistration passwordClientRegistration() {
   	 
    	return ClientRegistration.withRegistrationId("messaging-client-password")
           .clientId("password-messaging-client")
           .clientSecret("secret3")
           .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
           .authorizationGrantType(AuthorizationGrantType.PASSWORD)
           //.scope("message.read", "message.write")
           .authorizationUri(oauth2AuthorizationUri)
           .tokenUri(oauth2TokenUri)			
           .build();
   }
	
}
