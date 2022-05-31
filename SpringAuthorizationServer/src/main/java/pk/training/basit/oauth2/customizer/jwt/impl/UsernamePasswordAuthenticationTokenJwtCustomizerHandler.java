package pk.training.basit.oauth2.customizer.jwt.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.util.CollectionUtils;

import pk.training.basit.jpa.entity.UserPrincipal;
import pk.training.basit.oauth2.customizer.jwt.JwtCustomizerHandler;

public class UsernamePasswordAuthenticationTokenJwtCustomizerHandler extends AbstractJwtCustomizerHandler {

	public UsernamePasswordAuthenticationTokenJwtCustomizerHandler(JwtCustomizerHandler jwtCustomizerHandler) {
		super(jwtCustomizerHandler);
	}

	@Override
	protected void customizeJwt(JwtEncodingContext jwtEncodingContext) {
		
		Authentication authentication = jwtEncodingContext.getPrincipal();
		UserPrincipal userPrincipal = (UserPrincipal)authentication.getPrincipal();
		Long userId = userPrincipal.getId();
		Set<String> authorities = userPrincipal.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.collect(Collectors.toSet());
		
		Map<String, Object> userAttributes = new HashMap<>();
		userAttributes.put("userId", userId);
		
		Set<String> contextAuthorizedScopes = jwtEncodingContext.getAuthorizedScopes();
		
		JwtClaimsSet.Builder jwtClaimSetBuilder = jwtEncodingContext.getClaims();
		
		if (CollectionUtils.isEmpty(contextAuthorizedScopes)) {
			jwtClaimSetBuilder.claim(OAuth2ParameterNames.SCOPE, authorities);
		}
		
		jwtClaimSetBuilder.claims(claims ->
			userAttributes.entrySet().stream()
			.forEach(entry -> claims.put(entry.getKey(), entry.getValue()))
		);
		
	}

	@Override
	protected boolean supportCustomizeContext(Authentication authentication) {
		return authentication != null && authentication instanceof UsernamePasswordAuthenticationToken;
	}
	
}
