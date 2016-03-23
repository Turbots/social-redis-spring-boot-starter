package be.hubau.spring.social.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("spring.social.redis")
public class SocialRedisProperties {
}
