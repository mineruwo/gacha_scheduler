package com.gacha.gachascheduler.config;

import com.gacha.gachascheduler.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    private UserEntity user;
    private Map<String, Object> attributes;

    public CustomOAuth2User(UserEntity user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getName() {
        return user.getGoogleId();
    }

    public String getEmail() {
        return user.getEmail();
    }

    public String getProfilePictureUrl() {
        return user.getProfilePictureUrl();
    }

    public Long getUserId() {
        return user.getId();
    }

    public String getUserCode() {
        return user.getUserCode();
    }

    public String getRole() {
        return user.getRole().name();
    }
}
