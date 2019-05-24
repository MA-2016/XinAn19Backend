package org.luncert.uaa.config;

import java.util.Arrays;
import java.util.HashSet;

import org.luncert.uaa.model.mysql.OAuthClient;
import org.luncert.uaa.model.mysql.OAuthUser;
import org.luncert.uaa.repos.mysql.OAuthClientRepos;
import org.luncert.uaa.repos.mysql.OAuthUserRepos;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true) // 启用方法级别的权限认证
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * create UserDetailsService TODO: implement roles
     * 
     * @param oauthUserRepos
     * @param passwordEncoder
     * @return
     */
    @Bean
    public UserDetailsService userDetailsService(OAuthUserRepos oauthUserRepos) {
        return username -> {
            OAuthUser user = oauthUserRepos.findByUsername(username);
            if (user == null) {
                throw new UsernameNotFoundException("invalid username");
            }
            return User.withUsername(username).password(user.getPassword()).roles("").build();
        };
    }

    @Bean
    public ClientDetailsService clientDetailsService(OAuthClientRepos oauthClientRepos) {
        return clientId -> {
            OAuthClient client = oauthClientRepos.findByClientId(clientId);
            if (client == null) {
                throw new ClientRegistrationException("invalid clientId");
            }
            BaseClientDetails clientDetails = new BaseClientDetails();
            clientDetails.setClientId(client.getClientId());
            clientDetails.setClientSecret(client.getClientSecret());
            clientDetails.setRegisteredRedirectUri(
                new HashSet<>(Arrays.asList(client.getRedirectUrl().split(","))));
            clientDetails.setAuthorizedGrantTypes(
                Arrays.asList(client.getGrantType().split(",")));
            clientDetails.setScope(
                Arrays.asList(client.getScope().split(",")));
            return clientDetails;
        };
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
    }

    @Bean
    public TokenStore tokenStore(RedisConnectionFactory redisConnectionFactory) {
        return new RedisTokenStore(redisConnectionFactory);
    }

    @Override
	protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeRequests()
            .antMatchers(HttpMethod.GET, "/article").permitAll()
            .antMatchers(HttpMethod.GET, "/article/all").authenticated()
            .anyRequest().authenticated()
            .and()
            .formLogin();
	}

}