package com.springapp.springsocialcloud.security;

import jakarta.annotation.PostConstruct;
import lombok.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Value
public class AppUserService implements UserDetailsService {

    PasswordEncoder passwordEncoder;

    Map<String, AppUser> users = new HashMap<>();

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return users.get(username);
    }

    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> oidcLoginHandler() {
        return userRequest -> {
            LoginProvider provider = LoginProvider.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase());
            OidcUserService oidcUserService = new OidcUserService();
            OidcUser oidcUser = oidcUserService.loadUser(userRequest);
            return AppUser
                    .builder()
                    .provider(provider)
                    .username(oidcUser.getEmail())
                    .name(oidcUser.getFullName())
                    .email(oidcUser.getEmail())
                    .userId(oidcUser.getName())
                    .imageUrl(oidcUser.getAttribute("picture"))
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .attributes(oidcUser.getAttributes())
                    .authorities(oidcUser.getAuthorities())
                    .build();

        };
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2LoginHandler() {
        return userRequest -> {
            LoginProvider provider = getProvider(userRequest);
            DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
            OAuth2User oAuth2User = delegate.loadUser(userRequest);
            return AppUser
                    .builder()
                    .provider(provider)
                    .username(oAuth2User.getAttribute("login"))
                    .name(oAuth2User.getAttribute("login"))
                    .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                    .imageUrl(oAuth2User.getAttribute("avatar_url"))
                    .userId(oAuth2User.getName())
                    .authorities(oAuth2User.getAuthorities())
                    .attributes(oAuth2User.getAttributes())
                    .build();
        };
    }

    private LoginProvider getProvider(OAuth2UserRequest userRequest) {
        return LoginProvider.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase());
    }

    @PostConstruct
    private void createHardcodeUsers(){
        var bil = AppUser.builder()
                .username("bil")
                .provider(LoginProvider.APP)
                .password(passwordEncoder.encode("1234"))
                .authorities(List.of(new SimpleGrantedAuthority("read")))//verb read write
                .build();
        var bob = AppUser.builder()
                .username("bob")
                .provider(LoginProvider.APP)
                .password(passwordEncoder.encode("1234"))
                .authorities(List.of(new SimpleGrantedAuthority("read")))//verb read write
                .build();

        createUser(bil);
        createUser(bob);
    }

    private void createUser(AppUser user){
        users.putIfAbsent(user.getUsername(), user);
    }
}
