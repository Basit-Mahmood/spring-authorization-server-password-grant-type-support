package pk.training.basit.configuration.federated.identity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.springframework.security.oauth2.core.user.OAuth2User;

public class UserRepositoryOAuth2UserHandler implements Consumer<OAuth2User> {

	private final UserRepository userRepository = new UserRepository();

	@Override
	public void accept(OAuth2User user) {
		// Capture user in a local data store on first authentication
		if (this.userRepository.findByName(user.getName()) == null) {
			System.out.println("Saving first-time user: name=" + user.getName() + ", claims=" + user.getAttributes() + ", authorities=" + user.getAuthorities());
			this.userRepository.save(user);
		}
	}

	static class UserRepository {

		private final Map<String, OAuth2User> userCache = new ConcurrentHashMap<>();

		public OAuth2User findByName(String name) {
			return this.userCache.get(name);
		}

		public void save(OAuth2User oauth2User) {
			this.userCache.put(oauth2User.getName(), oauth2User);
		}

	}

}
