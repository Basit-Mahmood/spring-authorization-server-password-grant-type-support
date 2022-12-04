package pk.training.basit.oauth2.customizer.jwt.impl;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;

import pk.training.basit.oauth2.customizer.jwt.JwtCustomizerHandler;

public abstract class AbstractJwtCustomizerHandler implements JwtCustomizerHandler {

	protected JwtCustomizerHandler jwtCustomizerHandler;
	
	public AbstractJwtCustomizerHandler(JwtCustomizerHandler jwtCustomizerHandler) {
		this.jwtCustomizerHandler = jwtCustomizerHandler;
	}

	protected abstract boolean supportCustomizeContext(Authentication authentication);
	protected abstract void customizeJwt(JwtEncodingContext jwtEncodingContext);
	
	@Override
	public void customize(JwtEncodingContext jwtEncodingContext) {
		
		boolean supportCustomizeContext = false;
		AbstractAuthenticationToken token = null;
    	
    	Authentication authenticataion = SecurityContextHolder.getContext().getAuthentication();
    	
    	if (authenticataion instanceof OAuth2ClientAuthenticationToken ) {
    		token = (OAuth2ClientAuthenticationToken) authenticataion;
    	} 
    	
    	if (token != null) {
    		if (token.isAuthenticated() && OAuth2TokenType.ACCESS_TOKEN.equals(jwtEncodingContext.getTokenType())) {
    			Authentication authentication = jwtEncodingContext.getPrincipal();
    			supportCustomizeContext = supportCustomizeContext(authentication);
    		}
    	}
		
    	if (supportCustomizeContext) {
    		customizeJwt(jwtEncodingContext);
    	} else {
    		jwtCustomizerHandler.customize(jwtEncodingContext);
    	}

	}

}
