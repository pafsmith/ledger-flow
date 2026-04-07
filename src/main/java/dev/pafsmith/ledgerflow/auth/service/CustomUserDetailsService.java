package dev.pafsmith.ledgerflow.auth.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import dev.pafsmith.ledgerflow.user.entity.User;
import dev.pafsmith.ledgerflow.user.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
    User user = userRepository.findById(UUID.fromString(userId))
        .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
    return new org.springframework.security.core.userdetails.User(
        user.getId().toString(),
        user.getPasswordHash(),
        List.of(new SimpleGrantedAuthority("ROLE_USER")));
  }

}
