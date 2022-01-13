package pk.training.basit.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2TokenType;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import pk.training.basit.jpa.entity.UserPrincipal;
import pk.training.basit.service.JwtCustomizer;

@Service
public class JwtCustomizerServiceImpl implements JwtCustomizer {

	@Override
	public void customizeToken(JwtEncodingContext context) {
		
		AbstractAuthenticationToken token = null;
    	
    	Authentication authenticataion = SecurityContextHolder.getContext().getAuthentication();
    	
    	if (authenticataion instanceof OAuth2ClientAuthenticationToken ) {
    		token = (OAuth2ClientAuthenticationToken) authenticataion;
    	} 
    	
    	if (token != null) {
    		
    		if (token.isAuthenticated() && OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
    			
    			Authentication authentication = context.getPrincipal();
    			
    			if (authentication != null) {
    				
    				if (authentication instanceof UsernamePasswordAuthenticationToken) {
    					UserPrincipal principal = (UserPrincipal)authentication.getPrincipal();
        				Long userId = principal.getId();
        				Set<String> authorities = principal.getAuthorities().stream()
        						.map(GrantedAuthority::getAuthority)
        						.collect(Collectors.toSet());
        				
        				Map<String, Object> userAttributes = new HashMap<>();
        				userAttributes.put("userId", userId);
        				
        				Set<String> contextAuthorizedScopes = context.getAuthorizedScopes();
        				
        				JwtClaimsSet.Builder jwtClaimSetBuilder = context.getClaims();
        				
        				if (CollectionUtils.isEmpty(contextAuthorizedScopes)) {
        					jwtClaimSetBuilder.claim(OAuth2ParameterNames.SCOPE, authorities);
        				}
        				
        				jwtClaimSetBuilder.claims(claims ->
        					userAttributes.entrySet().stream()
        					.forEach(entry -> claims.put(entry.getKey(), entry.getValue()))
        				);
    				}
    				
    				if (authentication instanceof OAuth2ClientAuthenticationToken) {
    					OAuth2ClientAuthenticationToken OAuth2ClientAuthenticationToken = (OAuth2ClientAuthenticationToken)authentication;
    					Map<String, Object> additionalParameters = OAuth2ClientAuthenticationToken.getAdditionalParameters();
    					
    					// customize the token according to your need for this kind of authentication
    					if (!CollectionUtils.isEmpty(additionalParameters)) {
    						
    					}
    					
    				}
    					
    			}
    		}
    	}
		
	}
	
}
