package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.dto.RegisterRequestDto;
import com.ayhan.fleet_management.entity.User;
import com.ayhan.fleet_management.entity.enums.Role;
import com.ayhan.fleet_management.exception.DuplicateResourceException;
import com.ayhan.fleet_management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User register(RegisterRequestDto requestDto) {
        if (userRepository.existsByUsername(requestDto.getUsername())) {
            throw new DuplicateResourceException("Username is already taken");
        }

        if (userRepository.existsByEmail(requestDto.getEmail())) {
            throw new DuplicateResourceException("Email is already in use");
        }

        User user = User.builder()
                .username(requestDto.getUsername())
                .email(requestDto.getEmail())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .role(Role.USER)
                .build();

        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
}
