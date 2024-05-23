package com.likelion.yourside.user.controller;

import com.likelion.yourside.user.dto.UserSignUpRequestDto;
import com.likelion.yourside.user.dto.UserLoginRequestDto;
import com.likelion.yourside.user.service.UserService;
import com.likelion.yourside.util.response.CustomAPIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/user")
public class UserController {
    private final UserService userService;

    // 유효성 검사 필요 시 @Valid 넣기
    @PostMapping("/signup")
    public ResponseEntity<CustomAPIResponse<?>> signUp(@RequestBody UserSignUpRequestDto userSignUpDto) {
        return userService.signUp(userSignUpDto);
    }
    // 유효성 검사 필요 시 @Valid 넣기
    @PostMapping("/login")
    public ResponseEntity<CustomAPIResponse<?>> login(@RequestBody UserLoginRequestDto userLoginDto) {
        return userService.login(userLoginDto);
    }
    @GetMapping("/exists")
    public ResponseEntity<CustomAPIResponse<?>> checkDuplicationUsername(@RequestParam("username") String username) {
        return userService.checkDuplicationUsername(username);
    }
    @GetMapping("/search/username")
    public ResponseEntity<CustomAPIResponse<?>> checkUsername(@RequestParam("name") String name, @RequestParam("email") String email) {
        return userService.checkUsername(name, email);
    }

}
