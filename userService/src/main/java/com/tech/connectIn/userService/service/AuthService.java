package com.tech.connectIn.userService.service;

import com.tech.connectIn.userService.dto.LoginRequestDto;
import com.tech.connectIn.userService.dto.LoginResponse;
import com.tech.connectIn.userService.dto.SignupRequestDto;
import com.tech.connectIn.userService.dto.UserDto;

public interface AuthService {
    UserDto signup(SignupRequestDto signupRequestDto);

    LoginResponse login(LoginRequestDto loginRequestDto);
}
