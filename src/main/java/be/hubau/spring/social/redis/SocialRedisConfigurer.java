package be.hubau.spring.social.redis;

import be.hubau.spring.social.redis.repository.SocialConnectionRepository;
import be.hubau.spring.social.redis.connection.SocialRedisUsersConnectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.social.config.annotation.SocialConfigurerAdapter;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.stereotype.Component;

@Component
public class SocialRedisConfigurer extends SocialConfigurerAdapter {

    @Autowired
    private SocialConnectionRepository socialConnectionRepository;

    public UsersConnectionRepository getUsersConnectionRepository(ConnectionFactoryLocator connectionFactoryLocator) {
        return new SocialRedisUsersConnectionRepository(connectionFactoryLocator, Encryptors.noOpText(), socialConnectionRepository);
    }
}
