package com.betoniarka.biblioteka.security;

import com.betoniarka.biblioteka.appuser.AppUser;
import java.util.Collection;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@RequiredArgsConstructor
public class AppUserDetails implements UserDetails {

  private final AppUser appUser;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    String roleName = appUser.getRole().name();
    return List.of(new SimpleGrantedAuthority("ROLE_" + roleName));
  }

  @Override
  public String getPassword() {
    return appUser.getPassword();
  }

  @Override
  public String getUsername() {
    return appUser.getUsername();
  }
}
