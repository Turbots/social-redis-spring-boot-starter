package be.hubau.spring.social.redis;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.social.config.annotation.SocialConfiguration;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;

@Configuration
@EnableRedisRepositories
@EnableConfigurationProperties(SocialRedisProperties.class)
@ComponentScan(basePackages = "be.hubau.spring.social.redis")
public class SocialRedisAutoConfiguration extends SocialConfiguration {

    @Bean
    public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate redisTemplate = new RedisTemplate<byte[], byte[]>();

        redisTemplate.setConnectionFactory(redisConnectionFactory);

        return redisTemplate;
    }

    @Bean
    @Primary
    @Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
    public ConnectionRepository connectionRepository(UsersConnectionRepository usersConnectionRepository) {
        return usersConnectionRepository.createConnectionRepository(userIdSource().getUserId());
    }
}
