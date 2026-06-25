package com.tech.connectIn.userService.controller;

import com.tech.connectIn.userService.dto.LoginRequestDto;
import com.tech.connectIn.userService.dto.LoginResponse;
import com.tech.connectIn.userService.dto.SignupRequestDto;
import com.tech.connectIn.userService.dto.UserDto;
import com.tech.connectIn.userService.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/auth")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserDto>signup(@RequestBody SignupRequestDto signupRequestDto){
        UserDto userDto = authService.signup(signupRequestDto);
        return new ResponseEntity<>(userDto, HttpStatus.CREATED);
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse>login(@RequestBody LoginRequestDto loginRequestDto){
        LoginResponse loginResponse = authService.login(loginRequestDto);
        return new ResponseEntity<>(loginResponse, HttpStatus.OK);
    }
}
