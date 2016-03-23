package be.hubau.spring.social.redis;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(SocialRedisAutoConfiguration.class)
public @interface EnableSocialRedis {
}
