package pk.training.basit.controller.rest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import pk.training.basit.service.JwtService;

@RestController
public class MessagesController {

	@GetMapping("/messages")
	@PreAuthorize("hasAuthority('USER') or hasAuthority('message.read') ")
	public String[] getMessages(@AuthenticationPrincipal Jwt jwt) {
		Long userId = JwtService.getUserId(jwt);
		System.out.println(userId);
		return new String[] {"Message 1", "Message 2", "Message 3"};
	}
	
}
