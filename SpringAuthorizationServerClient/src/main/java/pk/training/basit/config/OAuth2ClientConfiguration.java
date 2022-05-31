package pk.training.basit.config;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

import pk.training.basit.config.oauth2.client.OAuth2Client;

@Configuration
public class OAuth2ClientConfiguration {

	@Autowired
    private List<OAuth2Client> oauth2Clients;
    
	@Bean
	public ClientRegistrationRepository clientRegistrationRepository() {
	        
		List<ClientRegistration> registrations = oauth2Clients.stream()
	            .map(c -> c.getClientRegistration())
	            .filter(registration -> registration != null)
	            .collect(Collectors.toList());
		
		return new InMemoryClientRegistrationRepository(registrations);
    }
	    
}
