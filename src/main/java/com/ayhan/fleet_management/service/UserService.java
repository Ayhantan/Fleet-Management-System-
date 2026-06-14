package com.ayhan.fleet_management.service;

import com.ayhan.fleet_management.dto.RegisterRequestDto;
import com.ayhan.fleet_management.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    User register(RegisterRequestDto requestDto);
}
