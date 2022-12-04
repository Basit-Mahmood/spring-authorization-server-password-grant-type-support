package pk.training.basit.configuration.token.impl;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import pk.training.basit.configuration.token.OAuth2TokenSettings;

@Component
public class OAuth2TokenSettingsImpl implements OAuth2TokenSettings {

	private static final Logger LOGGER = LogManager.getLogger(OAuth2TokenSettingsImpl.class);

	private final long accessTokenTime;
	private final String accessTokenTimeUnit;
	private final long refreshTokenTime;
	private final String refreshTokenTimeUnit;

	public OAuth2TokenSettingsImpl(@Value("${oauth2.access.token.time}") long accessTokenTime,
			@Value("${oauth2.access.token.time.unit}") String accessTokenTimeUnit,
			@Value("${oauth2.refresh.token.time}") long refreshTokenTime,
			@Value("${oauth2.refresh.token.time.unit}") String refreshTokenTimeUnit) {

		LOGGER.debug("in OAuth2TokenSettingImpl");

		this.accessTokenTime = accessTokenTime;
		this.accessTokenTimeUnit = accessTokenTimeUnit;
		this.refreshTokenTime = refreshTokenTime;
		this.refreshTokenTimeUnit = refreshTokenTimeUnit;
	}

	@Override
	public TokenSettings getTokenSettings() {

		Duration accessTokenDuration = setTokenTime(accessTokenTimeUnit, accessTokenTime, 5);
		Duration refreshTokenDuration = setTokenTime(refreshTokenTimeUnit, refreshTokenTime, 60);

		TokenSettings.Builder tokenSettingsBuilder = TokenSettings.builder().accessTokenTimeToLive(accessTokenDuration)
				.refreshTokenTimeToLive(refreshTokenDuration);
		TokenSettings tokenSetting = tokenSettingsBuilder.build();
		return tokenSetting;

	}

	private Duration setTokenTime(String tokenTimeUnit, long tokenTime, long durationInMinutes) {

		Duration duration = Duration.ofMinutes(durationInMinutes);

		if (StringUtils.hasText(tokenTimeUnit)) {

			switch (tokenTimeUnit.toUpperCase()) {
			case "M":
			case "MINUTE":
			case "MINUTES":
				duration = Duration.ofMinutes(tokenTime);
				break;
			case "H":
			case "HOUR":
			case "HOURS":
				duration = Duration.ofHours(tokenTime);
				break;
			case "D":
			case "DAY":
			case "DAYS":
				duration = Duration.ofDays(tokenTime);
				break;
			case "W":
			case "WEEK":
			case "WEEKS":
				duration = Duration.of(tokenTime, ChronoUnit.WEEKS);
				break;
			}
		}

		return duration;
	}

}
