package be.hubau.spring.social.redis.connection;

import be.hubau.spring.social.redis.repository.SocialConnectionRepository;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.ConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SocialRedisUsersConnectionRepository implements UsersConnectionRepository {

    private SocialConnectionRepository socialConnectionRepository;
    private ConnectionFactoryLocator connectionFactoryLocator;
    private TextEncryptor textEncryptor;

    public SocialRedisUsersConnectionRepository(ConnectionFactoryLocator connectionFactoryLocator, TextEncryptor textEncryptor, SocialConnectionRepository socialConnectionRepository) {
        this.connectionFactoryLocator = Objects.requireNonNull(connectionFactoryLocator, "Connection factory locator should be non-null");
        this.textEncryptor = Objects.requireNonNull(textEncryptor, "TextEncryptor should be non-null");
        this.socialConnectionRepository = Objects.requireNonNull(socialConnectionRepository, "Social Connection repository should be non-null");
    }

    @Override
    public List<String> findUserIdsWithConnection(Connection<?> connection) {
        return null;
    }

    @Override
    public Set<String> findUserIdsConnectedTo(String providerId, Set<String> providerUserIds) {
        return null;
    }

    @Override
    public ConnectionRepository createConnectionRepository(String userId) {
        return new SocialRedisConnectionRepository(connectionFactoryLocator, textEncryptor, socialConnectionRepository, userId);
    }
}
