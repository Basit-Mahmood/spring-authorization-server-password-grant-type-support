package pk.training.basit.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.annotation.Validated;

import pk.training.basit.jpa.entity.UserPrincipal;

@Validated
public interface UserPrincipalService extends UserDetailsService {

	@Override
    UserPrincipal loadUserByUsername(String username);
	
}
