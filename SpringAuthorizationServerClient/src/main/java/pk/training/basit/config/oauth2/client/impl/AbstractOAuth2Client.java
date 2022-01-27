package pk.training.basit.config.oauth2.client.impl;

import org.springframework.core.env.Environment;

import pk.training.basit.config.oauth2.client.OAuth2Client;

public abstract class AbstractOAuth2Client implements OAuth2Client {

	protected String oauth2AuthorizationUri;
	protected String oauth2TokenUri;

	public AbstractOAuth2Client(Environment env) {
		this.oauth2AuthorizationUri = env.getProperty("oauth2.authorization.uri");
		this.oauth2TokenUri = env.getProperty("oauth2.token.uri");
	}

}
