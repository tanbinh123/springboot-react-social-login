package com.mycompany.movieapi.security.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.movieapi.model.User;
import com.mycompany.movieapi.security.CustomUserDetails;
import com.mycompany.movieapi.security.WebSecurityConfig;
import com.mycompany.movieapi.service.UserService;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

@Component
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;
    private final ObjectMapper objectMapper;

    public CustomOAuth2UserService(UserService userService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        return getOAuth2UserInfo(userRequest, oAuth2User);
    }

    private CustomUserDetails getOAuth2UserInfo(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String providerName = userRequest.getClientRegistration().getRegistrationId();
        if (providerName.equalsIgnoreCase(OAuth2Provider.GITHUB.name())) {
            GithubOAuth2User githubOAuth2User = objectMapper.convertValue(oAuth2User.getAttributes(), GithubOAuth2User.class);

            Optional<User> userOptional = userService.getUserByUsername(githubOAuth2User.getLogin());
            User user;
            if (userOptional.isEmpty()) {
                user = new User();
                user.setUsername(githubOAuth2User.getLogin());
                user.setName(githubOAuth2User.getName());
                user.setEmail(githubOAuth2User.getEmail());
                user.setRole(WebSecurityConfig.USER);
                user.setImageUrl(githubOAuth2User.getAvatarUrl());
                user.setProvider(OAuth2Provider.GITHUB);
                user.setProviderId(githubOAuth2User.getId());
                user = userService.saveUser(user);
            } else {
                user = userOptional.get();
                user.setEmail(githubOAuth2User.getEmail());
                user.setImageUrl(githubOAuth2User.getAvatarUrl());
            }

            CustomUserDetails customUserDetails = new CustomUserDetails();
            customUserDetails.setId(user.getId());
            customUserDetails.setUsername(githubOAuth2User.getLogin());
            customUserDetails.setName(githubOAuth2User.getName());
            customUserDetails.setEmail(githubOAuth2User.getEmail());
            customUserDetails.setAttributes(oAuth2User.getAttributes());
            customUserDetails.setAuthorities(Collections.singletonList(new SimpleGrantedAuthority(user.getRole())));
            return customUserDetails;
        } else {
            throw new InternalAuthenticationServiceException(String.format("The OAuth2 provider %s is not supported yet", providerName));
        }
    }
}
