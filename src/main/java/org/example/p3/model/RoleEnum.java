package org.example.p3.model;

import org.springframework.security.core.GrantedAuthority;

public enum RoleEnum implements GrantedAuthority {
    USER, ADMIN;
    @Override
    public String getAuthority()
    {
        return name();
    }
}
