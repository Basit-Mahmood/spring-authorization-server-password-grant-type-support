package pk.training.basit.service;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.Assert;

public interface JwtService {

	String USER_ID_CLAIM = "userId";
	String EMAIL_CLAIM = "email";
	String PRINCIPAL_USER_TYPE_CLAIM = "principalUserType";
	
	static <T> T getClaim(Jwt jwt, String claim, Class<T> clazz) {
		
		Assert.notNull(jwt, "jwt cannot be null");
		Assert.hasText(claim, "claim cannot be null or empty");
		
		T value = null;
		
		if (jwt.hasClaim(claim)) {
			value = jwt.getClaim(claim);
		}
		
		return value ;
	}
	
	static Long getUserId(Jwt jwt) {
		return getClaim(jwt, USER_ID_CLAIM, Long.class);
	}
	
	static String getEmail(Jwt jwt) {
		return getClaim(jwt, EMAIL_CLAIM, String.class);
	}
	
	static String getPrincipalUserType(Jwt jwt) {
		return getClaim(jwt, PRINCIPAL_USER_TYPE_CLAIM, String.class);
	}
	
}
