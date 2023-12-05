package com.springapp.springsocialcloud.security;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.*;

@Configuration
@Log4j2
public class SecurityConfiguration {
    //.requestMatchers("/","/images/**","/**.css","/**.js").permitAll()

    @Bean
    @Order(0)
    SecurityFilterChain resources(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/images/**","/**.css","/**.js","/**.ico")
                .authorizeHttpRequests(c->c
                        .anyRequest().permitAll())
                .securityContext(c->c.disable())
                .sessionManagement(c->c.disable())
                .requestCache(c->c.disable())// saved url when the user trying to authenticated
                .build();
    }

    @Bean
    @Order(1)
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2LoginHandler,
                                            OAuth2UserService<OidcUserRequest, OidcUser> oidcLoginHandler) throws Exception {
        return http
                .formLogin(c->c
                        .loginPage("/login")
                        .loginProcessingUrl("/hueta")
                        .usernameParameter("user")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/user")
                )
                .logout(c->c.logoutSuccessUrl("/?logout"))
                .oauth2Login(oc -> oc
                        .loginPage("/login")
                        .defaultSuccessUrl("/user")
                        .userInfoEndpoint(ui->ui
                                .userService(oauth2LoginHandler)
                                .oidcUserService(oidcLoginHandler)))
                .authorizeHttpRequests(c -> c
                        .requestMatchers("/","/login").permitAll()
                        .anyRequest().authenticated())
                .build();
    }

    @Bean
    ApplicationListener<AuthenticationSuccessEvent> successLogger(){
        return event->{
            log.info("success: {}", event.getAuthentication());
        };
    }

    @Bean
    BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}
