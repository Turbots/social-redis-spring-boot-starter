package be.hubau.spring.social.redis.repository;

import be.hubau.spring.social.redis.entity.RedisConnection;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SocialConnectionRepository extends CrudRepository<RedisConnection, String> {

    Iterable<RedisConnection> findByProviderUserId(final String providerUserId);

    Iterable<RedisConnection> findByProviderId(final String providerId);

    Iterable<RedisConnection> findByUserIdAndProviderId(final String userId, final String providerId);

    RedisConnection findOneByUserIdAndProviderIdAndProviderUserId(String userId, String providerId, String providerUserId);

    void deleteByUserIdAndProviderId(final String userId, final String providerId);

    void deleteByUserIdAndProviderIdAndProviderUserId(final String userId, final String providerId, final String providerUserId);
}
