package org.luncert.uaa.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.TimeUnit;

import org.luncert.uaa.model.mysql.OAuthClient;
import org.luncert.uaa.model.mysql.OAuthUser;
import org.luncert.uaa.repos.mysql.OAuthClientRepos;
import org.luncert.uaa.repos.mysql.OAuthUserRepos;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.RandomValueAuthorizationCodeServices;
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
     * TODO: implement roles
     * 注册一个UserDetailsService用于用户身份认证
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

    /**
     * 注册一个ClientDetailsService进行用户clientId和clientSecret验证
     * @param oauthClientRepos
     * @return
     */
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

    /**
     * 注册一个TokenStore以保存token信息
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    public TokenStore tokenStore(RedisConnectionFactory redisConnectionFactory) {
        return new RedisTokenStore(redisConnectionFactory);
    }

    /**
     * 注册一个AuthorizationCodeServices以保存authorization_code的授权码code
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    public AuthorizationCodeServices authorizationCodeServices(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, OAuth2Authentication> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.afterPropertiesSet();

        return new RandomValueAuthorizationCodeServices() {
            protected void store(String code, OAuth2Authentication authentication) {
                redisTemplate.boundValueOps(code)
                    .set(authentication, 10, TimeUnit.MINUTES);
            }

            protected OAuth2Authentication remove(String code) {
                OAuth2Authentication authentication =
                    redisTemplate.boundValueOps(code).get();
                redisTemplate.delete(code);
                return authentication;
            }
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(Collections.singletonList(provider));
    }

    // @Override
    // protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    //     auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
    // }

    // @Override
	// protected void configure(HttpSecurity http) throws Exception {
    //     http.csrf().disable();
    //     http.authorizeRequests()
    //         .antMatchers(HttpMethod.GET, "/article").permitAll()
    //         .antMatchers(HttpMethod.GET, "/article/all").authenticated()
    //         .anyRequest().authenticated()
    //         .and()
    //         .formLogin();
    // }

    @Bean
    public AuthorizationServerConfigurer authorizationServerConfigurer(
        UserDetailsService userDetailsService,
        ClientDetailsService clientDetailsService,
        TokenStore tokenStore, AuthorizationCodeServices authorizationCodeServices,
        AuthenticationManager authenticationManager)
    {
        return new AuthorizationServerConfigurer() {
            public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {

            }

            public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
                clients.withClientDetails(clientDetailsService);
            }

            public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
                endpoints.userDetailsService(userDetailsService);
                endpoints.tokenStore(tokenStore);
                endpoints.authorizationCodeServices(authorizationCodeServices);
                endpoints.authenticationManager(authenticationManager);
            }
        };
    }

}