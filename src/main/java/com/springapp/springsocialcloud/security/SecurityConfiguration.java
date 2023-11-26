package com.springapp.springsocialcloud.security;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collections;
import java.util.UUID;

import static org.springframework.security.config.Customizer.*;

@Configuration
@Log4j2
public class SecurityConfiguration {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .formLogin(withDefaults())
                //.oauth2Login(withDefaults())
                .oauth2Login(oc -> oc
                        .userInfoEndpoint(ui->ui
                                .userService(oauth2LoginHandler())
                                .oidcUserService(oidcLoginHandler())))
                .authorizeHttpRequests(c -> c.anyRequest().authenticated())
                .build();
    }

    private OAuth2UserService<OidcUserRequest, OidcUser> oidcLoginHandler() {
        return userRequest -> {
            LoginProvider provider = LoginProvider.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase());
            OidcUserService oidcUserService = new OidcUserService();
            OidcUser oidcUser = oidcUserService.loadUser(userRequest);
//            return oidcUser;
            return AppUser
                    .builder()
                    .provider(provider)
                    .username(oidcUser.getEmail())
                    .name(oidcUser.getFullName())
                    .email(oidcUser.getEmail())
                    .userId(oidcUser.getName())
                    .imageUrl(oidcUser.getAttribute("picture"))
                    .password(passwordEncoder().encode(UUID.randomUUID().toString()))
                    .attributes(oidcUser.getAttributes())
                    .authorities(oidcUser.getAuthorities())
                    .build();

        };
    }

    private OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2LoginHandler() {
        return userRequest -> {
            LoginProvider provider = LoginProvider.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase());
            DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
            OAuth2User oAuth2User = delegate.loadUser(userRequest);
            return AppUser
                    .builder()
                    .provider(provider)
                    .username(oAuth2User.getAttribute("login"))
                    .name(oAuth2User.getAttribute("login"))
                    .password(passwordEncoder().encode(UUID.randomUUID().toString()))
                    .imageUrl(oAuth2User.getAttribute("avatar_url"))
                    .userId(oAuth2User.getName())
                    .authorities(oAuth2User.getAuthorities())
                    .attributes(oAuth2User.getAttributes())
                    .build();
        };
    }

    @Bean
    PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService inMemoryUsers(){
       InMemoryUserDetailsManager users = new InMemoryUserDetailsManager();

       var bob = new User("bob", passwordEncoder().encode("1234"), Collections.emptyList());
       var bil = User.builder()
               .username("bil")
               .password(passwordEncoder().encode("1234"))
               .roles("USER")//noun teacher admin
               .authorities("read")//verb read write
               .build();

       users.createUser(bob);
       users.createUser(bil);

       return users;
    }

    @Bean
    ApplicationListener<AuthenticationSuccessEvent> successLogger(){
        return event->{
            log.info("success: {}", event.getAuthentication());
        };
    }
}
