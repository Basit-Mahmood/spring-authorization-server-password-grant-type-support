package pk.training.basit.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import pk.training.basit.configuration.registered.client.OAuth2RegisteredClient;
import pk.training.basit.service.OAuth2RegisteredClientService;

@Service
public class OAuth2RegisteredClientServiceImpl implements OAuth2RegisteredClientService {

	private final List<OAuth2RegisteredClient> inMemoryOAuth2RegisteredClients;
	
	public OAuth2RegisteredClientServiceImpl(List<OAuth2RegisteredClient> oauth2RegisteredClients) {
		this.inMemoryOAuth2RegisteredClients = oauth2RegisteredClients;
	}

	@Override
	public List<RegisteredClient> getOAuth2RegisteredClient() {
		
		List<RegisteredClient> registeredClients = Collections.emptyList();
		if (!CollectionUtils.isEmpty(inMemoryOAuth2RegisteredClients)) {
			registeredClients = inMemoryOAuth2RegisteredClients.stream()
				.map(oauth2RegisteredClient -> oauth2RegisteredClient.getRegisteredClient())
				.collect(Collectors.toList());
		}
		
		return registeredClients;
	}

}
