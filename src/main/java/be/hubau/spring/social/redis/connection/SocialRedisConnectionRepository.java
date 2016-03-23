package be.hubau.spring.social.redis.connection;

import be.hubau.spring.social.redis.entity.RedisConnection;
import be.hubau.spring.social.redis.repository.SocialConnectionRepository;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.social.connect.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@SuppressWarnings("unchecked")
public class SocialRedisConnectionRepository implements ConnectionRepository {

    private final ConnectionFactoryLocator connectionFactoryLocator;
    private final TextEncryptor textEncryptor;
    private final SocialConnectionRepository socialConnectionRepository;
    private final String userId;

    public SocialRedisConnectionRepository(final ConnectionFactoryLocator connectionFactoryLocator, final TextEncryptor textEncryptor, final SocialConnectionRepository socialConnectionRepository, final String userId) {
        this.connectionFactoryLocator = connectionFactoryLocator;
        this.textEncryptor = textEncryptor;
        this.socialConnectionRepository = Objects.requireNonNull(socialConnectionRepository, "socialConnectionRepository is required");
        this.userId = Objects.requireNonNull(userId, "userId is required");
    }

    public MultiValueMap<String, Connection<?>> findAllConnections() {
        Iterable<RedisConnection> allConnections = socialConnectionRepository.findByProviderUserId(userId);

        final MultiValueMap<String, Connection<?>> connections = new LinkedMultiValueMap<>();
        Set<String> registeredProviderIds = connectionFactoryLocator.registeredProviderIds();
        for (String registeredProviderId : registeredProviderIds) {
            connections.put(registeredProviderId, Collections.<Connection<?>>emptyList());
        }

        allConnections.forEach((redisConnection) -> connections.add(redisConnection.getProviderId(), connectionMapper.mapConnection(redisConnection)));

        return connections;
    }

    @Override
    public List<Connection<?>> findConnections(String providerId) {
        return StreamSupport.stream(socialConnectionRepository.findByProviderId(providerId).spliterator(), false)
                .map(connectionMapper::mapConnection)
                .collect(Collectors.toList());
    }

    @Override
    public <A> List<Connection<A>> findConnections(Class<A> apiType) {
        List<?> connections = findConnections(getProviderId(apiType));
        return (List<Connection<A>>) connections;
    }

    @Override
    public MultiValueMap<String, Connection<?>> findConnectionsToUsers(MultiValueMap<String, String> providerUserIds) {
        return null;
    }

    @Override
    public Connection<?> getConnection(ConnectionKey connectionKey) {
        try {
            return connectionMapper.mapConnection(socialConnectionRepository.findOneByUserIdAndProviderIdAndProviderUserId(userId, connectionKey.getProviderId(), connectionKey.getProviderUserId()));
        } catch (EmptyResultDataAccessException e) {
            throw new NoSuchConnectionException(connectionKey);
        }
    }

    @Override
    public <A> Connection<A> getConnection(Class<A> apiType, String providerUserId) {
        String providerId = getProviderId(apiType);
        return (Connection<A>) getConnection(new ConnectionKey(providerId, providerUserId));
    }

    @Override
    public <A> Connection<A> getPrimaryConnection(Class<A> apiType) {
        String providerId = getProviderId(apiType);
        Connection<A> connection = (Connection<A>) findPrimaryConnection(providerId);
        if (connection == null) {
            throw new NotConnectedException(providerId);
        }
        return connection;
    }

    @Override
    public <A> Connection<A> findPrimaryConnection(Class<A> apiType) {
        String providerId = getProviderId(apiType);
        return (Connection<A>) findPrimaryConnection(providerId);
    }

    @Override
    public void addConnection(Connection<?> connection) {
        try {
            ConnectionData data = connection.createData();
            RedisConnection redisConnection = new RedisConnection(data.getProviderUserId(), userId, data.getProviderId(), data.getDisplayName(), data.getProfileUrl(), data.getImageUrl(), encrypt(data.getAccessToken()), encrypt(data.getSecret()), encrypt(data.getRefreshToken()), data.getExpireTime());
            socialConnectionRepository.save(redisConnection);
        } catch (Exception e) {
            throw new DuplicateConnectionException(connection.getKey());
        }
    }

    @Override
    public void updateConnection(Connection<?> connection) {
        ConnectionData data = connection.createData();
        RedisConnection redisConnection = socialConnectionRepository.findOneByUserIdAndProviderIdAndProviderUserId(userId, data.getProviderId(), data.getProviderUserId());

        redisConnection.setDisplayName(data.getDisplayName());
        redisConnection.setImageUrl(data.getImageUrl());
        redisConnection.setProfileUrl(data.getProfileUrl());
        redisConnection.setAccessToken(encrypt(data.getAccessToken()));
        redisConnection.setSecret(encrypt(data.getSecret()));
        redisConnection.setRefreshToken(encrypt(data.getRefreshToken()));
        redisConnection.setExpireTime(data.getExpireTime());

        socialConnectionRepository.save(redisConnection);
    }

    @Override
    public void removeConnections(String providerId) {
        socialConnectionRepository.deleteByUserIdAndProviderId(userId, providerId);
    }

    @Override
    public void removeConnection(ConnectionKey connectionKey) {
        socialConnectionRepository.deleteByUserIdAndProviderIdAndProviderUserId(userId, connectionKey.getProviderId(), connectionKey.getProviderUserId());
    }

    private final RedisConnectionMapper connectionMapper = new RedisConnectionMapper();

    private final class RedisConnectionMapper {

        Connection<?> mapConnection(final RedisConnection redisConnection) {
            ConnectionData connectionData = mapConnectionData(redisConnection);
            ConnectionFactory<?> connectionFactory = connectionFactoryLocator.getConnectionFactory(connectionData.getProviderId());
            return connectionFactory.createConnection(connectionData);
        }

        private ConnectionData mapConnectionData(final RedisConnection redisConnection) {
            return new ConnectionData(redisConnection.getProviderId(), redisConnection.getProviderUserId(), redisConnection.getDisplayName(), redisConnection.getProfileUrl(), redisConnection.getImageUrl(),
                    decrypt(redisConnection.getAccessToken()), decrypt(redisConnection.getSecret()), decrypt(redisConnection.getRefreshToken()), redisConnection.getExpireTime());
        }

        private String decrypt(String encryptedText) {
            return encryptedText == null ? null : textEncryptor.decrypt(encryptedText);
        }
    }

    private Connection<?> findPrimaryConnection(String providerId) {
        List<Connection<?>> connections = StreamSupport.stream(socialConnectionRepository.findByUserIdAndProviderId(userId, providerId).spliterator(), false)
                .map(connectionMapper::mapConnection).collect(Collectors.toList());
        if (connections.size() > 0) {
            return connections.get(0);
        } else {
            return null;
        }
    }

    private <A> String getProviderId(Class<A> apiType) {
        return connectionFactoryLocator.getConnectionFactory(apiType).getProviderId();
    }

    private String encrypt(String text) {
        return text == null ? null : textEncryptor.encrypt(text);
    }
}
