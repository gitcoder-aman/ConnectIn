package com.tech.connectIn.userService.service.impl;

import com.tech.connectIn.userService.dto.LoginRequestDto;
import com.tech.connectIn.userService.dto.LoginResponse;
import com.tech.connectIn.userService.dto.SignupRequestDto;
import com.tech.connectIn.userService.dto.UserDto;
import com.tech.connectIn.userService.entity.User;
import com.tech.connectIn.userService.exception.BadRequestException;
import com.tech.connectIn.userService.exception.ResourceNotFoundException;
import com.tech.connectIn.userService.repository.UserRepository;
import com.tech.connectIn.userService.service.AuthService;
import com.tech.connectIn.userService.service.JwtService;
import com.tech.connectIn.utils.BCryptUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final JwtService jwtService;

    @Override
    public UserDto signup(SignupRequestDto signupRequestDto) {
        log.info("Signup a user with email{}", signupRequestDto.getEmail());

        boolean exists = userRepository.existsByEmail(signupRequestDto.getEmail());
        if (exists) {
            throw new BadRequestException("User already exists");
        }
        User user = modelMapper.map(signupRequestDto, User.class);
        user.setPassword(BCryptUtil.hash(signupRequestDto.getPassword()));

        User save = userRepository.save(user);
        return modelMapper.map(save, UserDto.class);
    }

    @Override
    public LoginResponse login(LoginRequestDto loginRequestDto) {
        log.info("Login request for user with email:{}", loginRequestDto.getEmail());
        User user = userRepository.findByEmail(loginRequestDto.getEmail()).orElseThrow(() -> new BadRequestException("Incorrect email or password"));

        boolean isPasswordMatch = BCryptUtil.match(loginRequestDto.getPassword(),user.getPassword());
        if(!isPasswordMatch){
            throw new BadRequestException("Incorrect email or password");
        }
        return new LoginResponse(user.getEmail(),jwtService.generateAccessToken(user));
    }
}
